package actors.game

import actors.{ GameStatus, GetGameStatus, GetName, InitGame, StartGame }
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import java.util.UUID
import models._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._
import scala.concurrent.Await

import GameActor.Internal._

class GameActor(id: UUID) extends FSM[State, Data] {

  when(SettingUp) {
    case Event(StartGame(p1, p2), _) =>
      implicit val timeout = Timeout(5.seconds)
      val originalSender = sender()
      val p1NameFut = (p1 ? GetName).mapTo[String]
      val p2NameFut = (p2 ? GetName).mapTo[String]
      val gameFuture = for {
        p1Name <- p1NameFut
        p2Name <- p2NameFut
      } yield Game(
        player1 = Player(p1Name, 100, 50),
        player2 = Player(p2Name, 100, 50)
      )
      gameFuture onSuccess { case game =>
        self ! ((game, p1, p2)) // Xlint workaround
        originalSender ! GameStatus(true, game)
      }
      stay
    case Event((g: Game, p1: ActorRef, p2: ActorRef), _) =>
      goto(Playing) using Match(state = g, p1 = p1, p2 = p2,
        observers = Seq(p1, p2))
  }

  when(Playing) {
    case Event(GetGameStatus, m: Match) =>
      stay replying GameStatus(true, m.state)
    case Event(WatchGame(_), m: Match) =>
      stay using m.copy(observers = m.observers :+ sender()) replying
        InitGame(self, id.toString, m.state)
    case Event(LeaveGame, m: Match) =>
      val a = sender()
      val newState = m.copy(observers = m.observers filter (_ == a))
      if (a == m.p1) {
        goto(Finished) using winGame(Defeat, Victory, m.state, newState)
      } else if (a == m.p2) {
        goto(Finished) using winGame(Victory, Defeat, m.state, newState)
      } else {
        stay using newState
      }
    case Event(MakeMove(move), m: Match) =>
      val senderActor = sender()
      val isP1 = senderActor == m.p1
      val isP2 = senderActor == m.p2
      val p1MoveOpt = if (isP1) m.p1Move orElse Some(move) else m.p1Move
      val p2MoveOpt = if (isP2) m.p2Move orElse Some(move) else m.p2Move
      (p1MoveOpt, p2MoveOpt) match {
        case (Some(p1Move), Some(p2Move)) =>
          val newState = processTurn(m.state, p1Move, p2Move)
          m.observers foreach (_ ! GameUpdate(newState, (p1Move, p2Move)))
          Turn.resultOpt(newState.player1, newState.player2) match {
            case Some((p1Res, p2Res)) =>
              goto(Finished) using winGame(p1Res, p2Res, newState, m)
            case _ =>
              stay using m.copy(state = newState, p1Move = None, p2Move = None)
          }
        case _ =>
          stay using m.copy(p1Move = p1MoveOpt, p2Move = p2MoveOpt)
      }
  }

  when(Finished) {
    case Event(LeaveGame, r: Results) =>
      if (r.observers.isEmpty) stop()
      else stay using r.copy(observers = r.observers filter (_ == sender()))
    case Event(GetGameStatus, r: Results) =>
      stay replying GameStatus(false, r.state)
  }

  onTransition {
    case SettingUp -> Playing =>
      nextStateData match {
        case m: Match =>
          m.p1 ! InitGame(self, id.toString, m.state)
          m.p2 ! InitGame(self, id.toString, m.state)
        case _ =>
      }
    case Playing -> Finished =>
      nextStateData match {
        case Results(p1Res, p2Res, _, observers) =>
          observers foreach (_ ! GameEnd(p1Res, p2Res))
        case _ =>
      }
  }

  def winGame(p1Res: MatchResult, p2Res: MatchResult, state: Game, m: Match) = {
    Results(p1Res, p2Res, state, m.observers)
  }

  def processTurn(state: Game, move1: Move, move2: Move) = {
    val p1Result = Turn.resolve(state.player1, move1, move2)
    val p2Result = Turn.resolve(state.player2, move2, move1)
    state.copy(
      player1 = p1Result,
      player2 = p2Result,
      turn = state.turn + 1
    )
  }

  startWith(SettingUp, Uninitialized)
  initialize()
}

object GameActor {

  def props(id: UUID) =
    Props(new GameActor(id))

  object Internal {
    sealed trait State
    sealed trait Data {
      def observers: Seq[ActorRef]
    }

    case object SettingUp extends State
    case object Playing extends State
    case object Finished extends State

    case object Uninitialized extends Data { override val observers = Seq() }

    case class Match(
      state: Game,
      p1: ActorRef,
      p2: ActorRef,
      p1Move: Option[Move] = None,
      p2Move: Option[Move] = None,
      override val observers: Seq[ActorRef] = Seq()) extends Data

    case class Results(
      p1Result: MatchResult,
      p2Result: MatchResult,
      state: Game,
      override val observers: Seq[ActorRef] = Seq()) extends Data
  }
}
