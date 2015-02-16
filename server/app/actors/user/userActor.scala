package actors.user

import actors._
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import upickle._

case object NoUser extends State
case object WithUser extends State

case object NoData extends Data

trait Name {
  def name: String
}

case class Matchmaking(
    override val name: String, searching: Boolean) extends Data with Name

case class Playing(
    override val name: String, game: ActorRef) extends Data with Name

class UserActor(
    out: ActorRef,
    connections: ActorRef) extends FSM[State, Data] {

  val matchmaker = context.actorSelection("/user/system/matchmaker")

  override def receive = {
    case msg: String =>
      val readOpt = try {
        Some(read[SocketMessage](msg).data)
      } catch {
        case _: upickle.Invalid => None
      }
      readOpt foreach (super.receive(_))
    case msg => super.receive(msg)
  }

  startWith(NoUser, NoData)

  when(NoUser) {
    case Event(LoginReq(name), NoData) =>
      sendMessage(LoggedIn)
      connections ! Connect
      matchmaker ! JoinLobby
      goto(WithUser) using Matchmaking(name, false)
  }

  when(WithUser) {
    case Event(state: GameUpdate, _: Playing) =>
      sendMessage(state)
      stay
    case Event(result: MatchResult, _: Playing) =>
      sendMessage(result)
      stay
    case Event(LeaveGame, Playing(name, game)) =>
      game ! LeaveGame
      matchmaker ! JoinLobby
      stay using Matchmaking(name, false)
    case Event(msg, Playing(_, game)) =>
      game ! msg
      stay
    case Event(w: WaitingPlayers, _: Matchmaking) =>
      sendMessage(w)
      stay
    case Event(FindGame, n: Matchmaking) =>
      matchmaker ! FindGame
      stay using Matchmaking(n.name, true)
    case Event(CancelRequestGame, n: Matchmaking) =>
      matchmaker ! CancelRequestGame
      stay using Matchmaking(n.name, false)
    case Event(RequestGame, n: Matchmaking) =>
      matchmaker ! RequestGame
      stay using Matchmaking(n.name, false)
    case Event(GetName, n: Name) =>
      sender() ! n.name
      stay
    case Event(InitGame(game, id, state), Matchmaking(name, _)) =>
      sendMessage(GameReady(id, state))
      stay using Playing(name, game)
    case Event(j: JoinGame, m: Matchmaking) =>
      matchmaker ! j
      stay using Matchmaking(m.name, false)
  }

  whenUnhandled {
    case Event(a, b) =>
      stay
  }

  initialize()

  def sendMessage(msg: Message) = out ! write(SocketMessage(msg))
}

object UserActor {
  def props(out: ActorRef, connections: ActorRef) =
    Props(new UserActor(out, connections))
}
