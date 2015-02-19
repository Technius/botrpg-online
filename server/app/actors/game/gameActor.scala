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

class GameActor(id: UUID, p1: ActorRef, p2: ActorRef) extends Actor {

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
        observers foreach (_ ! GameUpdate(state, (p1move, p2move)))
        playerMove = None
      } getOrElse {
        val player = if (sender() == p1) state.player1 else state.player2
        playerMove = Some((player, move))
      }
    case LeaveGame =>
      val a = sender()
      observers -= a
      if (playing) {
        if (a == p1) winGame(Some(p2))
        else if (a == p2) winGame(Some(p1))
      }
      if (observers.length == 0) {
        self ! PoisonPill
      }
  }

  def winGame(winnerOpt: Option[ActorRef]) = if (playing) {
    val result: GameEnd = winnerOpt map { winner =>
      val p1Win = winner == p1
      val p2Win = winner == p2
      require(p1Win || p2Win)
      if (p1Win) GameEnd(Victory, Defeat)
      else GameEnd(Defeat, Victory)
    } getOrElse GameEnd(Draw, Draw)
    observers foreach (_ ! result)
    playing = false
  }

  def processTurn(move1: Move, move2: Move) = {
    val p1Result = Turn.resolve(state.player1, move1, move2)
    val p2Result = Turn.resolve(state.player2, move2, move1)
    val result = state.copy(
      p1 = (state.p1._1, p1Result),
      p2 = (state.p2._1, p2Result),
      turn = state.turn + 1
    )

    val p1Lose = result.player1.health <= 0
    val p2Lose = result.player2.health <= 0

    state = result

    if (p1Lose && p2Lose) winGame(None)
    else if (p1Lose) winGame(Some(p2))
    else if (p2Lose) winGame(Some(p1))
  }
}

object GameActor {
  def props(id: UUID, p1: ActorRef, p2: ActorRef) =
    Props(new GameActor(id, p1, p2))
}
