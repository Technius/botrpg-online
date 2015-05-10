package actors.game

import actors.{ GameStatus, GetGameStatus, GetName, InitGame }
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
    } yield Game(
      player1 = Player(p1Name, 100, 50),
      player2 = Player(p2Name, 100, 50)
    )
    Await.result(gameFuture, 5.seconds)
  }

  p1 ! InitGame(self, id.toString, state)
  p2 ! InitGame(self, id.toString, state)

  // Temporary
  observers ++= Seq(p1, p2)

  var playing: Boolean = true

  var playerMove: Option[(ActorRef, Move)] = None

  def receive = {
    case MakeMove(move) if playing =>
      val senderActor = sender()
      val isP1 = senderActor == p1
      val isP2 = senderActor == p2
      if (isP1 || isP2) {
        playerMove map { existingMove =>
          if (existingMove._1 != senderActor) {
            val moves = (move, existingMove._2)
            val (p1move, p2move) = if (isP1) moves else moves.swap
            processTurn(p1move, p2move)
            observers foreach (_ ! GameUpdate(state, (p1move, p2move)))
            playerMove = None
          }
        } getOrElse {
          playerMove = Some(senderActor -> move)
        }
      }
    case GetGameStatus =>
      sender() ! GameStatus(playing, state)
    case WatchGame(_) =>
      if (playing) {
        sender() ! InitGame(self, id.toString, state)
        observers += sender()
      }
    case LeaveGame =>
      val a = sender()
      observers -= a
      if (playing) {
        if (a == p1) winGame(Defeat, Victory)
        else if (a == p2) winGame(Victory, Defeat)
      }
      if (observers.length == 0) {
        self ! PoisonPill
      }
  }

  def winGame(p1Result: MatchResult, p2Result: MatchResult) = if (playing) {
    observers foreach (_ ! GameEnd(p1Result, p2Result))
    playing = false
  }

  def processTurn(move1: Move, move2: Move) = {
    val p1Result = Turn.resolve(state.player1, move1, move2)
    val p2Result = Turn.resolve(state.player2, move2, move1)
    val result = state.copy(
      player1 = p1Result,
      player2 = p2Result,
      turn = state.turn + 1
    )

    state = result

    Turn.resultOpt(result.player1, result.player2) foreach { t =>
      winGame(t._1, t._2)
    }
  }
}

object GameActor {
  def props(id: UUID, p1: ActorRef, p2: ActorRef) =
    Props(new GameActor(id, p1, p2))
}
