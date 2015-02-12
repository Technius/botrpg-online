package actors.game

import actors.{ State, Data, GetName }
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import java.util.UUID
import models._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._
import scala.concurrent.Await

case class QuitGame(p: ActorRef)

case class MakeMove(move: Move)

case object Victory

case object Defeat

class GameActor(
    p1: ActorRef,
    p2: ActorRef,
    matchmaker: ActorRef) extends Actor {

  val id = UUID.randomUUID()

  p1 ! GameReady(id.toString)
  p2 ! GameReady(id.toString)

  var state: Game = {
    implicit val timeout: Timeout = 5 seconds
    val p1NameFut = ask(p1, GetName).mapTo[String]
    val p2NameFut = ask(p2, GetName).mapTo[String]
    val gameFuture = for {
      p1Name <- p1NameFut
      p2Name <- p2NameFut
    } yield Game(p1 = (p1Name, Player(100, 50)), p2 = (p2Name, Player(100, 50)))
    Await.result(gameFuture, 5 seconds)
  }

  var playing: Boolean = true

  var playerMove: Option[(Player, Move)] = None

  def receive = {
    case MakeMove(move) =>
      playerMove map { existingMove =>
        val p1move = if (sender() == state.player1) move else existingMove._2
        val p2move = if (p1move == move) existingMove._2 else move
        processTurn(p1move, p2move)
        playerMove = None
      } getOrElse {
        val player = if (sender() == p1) state.player1 else state.player2
        playerMove = Some((player, move))
      }
    case QuitGame(a) if playing =>
      if (a == p1) {
        p1 ! Defeat
        p2 ! Victory
      } else if (a == p2) {
        p1 ! Victory
        p2 ! Defeat
      }
  }

  def processTurn(move1: Move, move2: Move) = {
    val result = state.copy(
      p1 = (state.p1._1, resolveTurn(state.player1, move1, move2)),
      p2 = (state.p2._1, resolveTurn(state.player2, move2, move1)),
      turn = state.turn + 1
    )

    if (result.player1.health <= 0) {
      p1 ! Defeat
      p2 ! Victory
      playing = false
    } else if (result.player2.health <= 0) {
      p1 ! Victory
      p2 ! Defeat
      playing = false
    }

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
