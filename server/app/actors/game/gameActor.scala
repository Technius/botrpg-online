package actors.game

import actors.{ State, Data, GetName, InitGame }
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import java.util.UUID
import models._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._
import scala.concurrent.Await

class GameActor(
    p1: ActorRef,
    p2: ActorRef,
    matchmaker: ActorRef) extends Actor {

  val id = UUID.randomUUID()

  val observers = scala.collection.mutable.ArrayBuffer[ActorRef]()

  var state: Game = {
    implicit val timeout: Timeout = 5.seconds
    val p1NameFut = ask(p1, GetName).mapTo[String]
    val p2NameFut = ask(p2, GetName).mapTo[String]
    val gameFuture = for {
      p1Name <- p1NameFut
      p2Name <- p2NameFut
    } yield Game(p1 = (p1Name, Player(100, 50)), p2 = (p2Name, Player(100, 50)))
    Await.result(gameFuture, 5.seconds)
  }

  p1 ! InitGame(self, id.toString, state)
  p2 ! InitGame(self, id.toString, state)

  // Temporary
  observers ++= Seq(p1, p2)

  var playing: Boolean = true

  var playerMove: Option[(Player, Move)] = None

  def receive = {
    case MakeMove(move) if playing =>
      playerMove map { existingMove =>
        val p1move = if (sender() == state.player1) move else existingMove._2
        val p2move = if (p1move == move) existingMove._2 else move
        processTurn(p1move, p2move)
        p1 ! GameUpdate(state)
        p2 ! GameUpdate(state)
        playerMove = None
      } getOrElse {
        val player = if (sender() == p1) state.player1 else state.player2
        playerMove = Some((player, move))
      }
    case LeaveGame =>
      val a = sender()
      if (playing) {
        if (a == p1) winGame(Some(p1))
        else if (a == p2) winGame(Some(p2))
      }
      observers -= a
      if (observers.length == 0) self ! PoisonPill
  }

  def winGame(winnerOpt: Option[ActorRef]) = if (playing) {
    playing = false
    winnerOpt map { winner =>
      if (winner == p1) {
        p1 ! Victory
        p2 ! Defeat
      } else if(winner == p2) {
        p1 ! Defeat
        p2 ! Victory
      } else {
        playing = true
      }
    } getOrElse {
      p1 ! Draw
      p2 ! Draw
    }
  }

  def processTurn(move1: Move, move2: Move) = {
    val result = state.copy(
      p1 = (state.p1._1, resolveTurn(state.player1, move1, move2)),
      p2 = (state.p2._1, resolveTurn(state.player2, move2, move1)),
      turn = state.turn + 1
    )

    val p1Lose = result.player1.health <= 0
    val p2Lose = result.player2.health <= 0

    if (p1Lose && p2Lose) winGame(None)
    else if (p1Lose) winGame(Some(p2))
    else if (p2Lose) winGame(Some(p1))

    state = result
  }

  private[this] def resolveTurn(
      player: Player,
      selfMove: Move,
      otherMove: Move) = {
    val stamina = player.stamina - selfMove.staminaCost
    val health = player.health - ((selfMove, otherMove) match {
      case (Defend, Attack) => 0
      case (_, Attack) => 20
      case _ => 0
    })

    player.copy(
      health = math.max(0, health),
      stamina = math.min(100, math.max(0, stamina))
    )
  }
}

object GameActor {
  def props(p1: ActorRef, p2: ActorRef, matchmaker: ActorRef) =
    Props(new GameActor(p1, p2, matchmaker))
}
