package actors.user

import actors._
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import scala.util.Try
import upickle.default._

import UserActor.Internal._

class UserActor(
    out: ActorRef,
    connections: ActorRef) extends FSM[State, Data] {

  val matchmaker = context.actorSelection("/user/system/matchmaker")
  val gameSupervisor = context.actorSelection("/user/system/games")

  override def receive = {
    case msg: String =>
      Try(read[SocketMessage](msg).data) foreach (super.receive(_))
    case msg =>
      super.receive(msg)
  }

  startWith(NoUser, NoData)

  when(NoUser) {
    case Event(LoginReq(name), _) =>
      implicit val timeout = Timeout(5.seconds)
      connections ! FindUser(name)
      goto(AwaitingAuthorization) using LoginRequestData(name)
  }

  when(AwaitingAuthorization) {
    case Event(Some(_), LoginRequestData(name)) =>
      sendMessage(LoginFail(s"$name is already taken! Choose another name."))
      goto(NoUser) using NoData
    case Event(None, LoginRequestData(name)) =>
      sendMessage(LoggedIn)
      connections ! Connect
      matchmaker ! JoinLobby
      goto(WithUser) using Matchmaking(name, false)
  }

  when(WithUser) {
    case Event(GetName, n: Name) =>
      sender() ! n.name
      stay
    case Event(state: GameUpdate, _: Playing) =>
      sendMessage(state)
      stay
    case Event(result: GameEnd, _: Playing) =>
      sendMessage(result)
      stay
    case Event(LeaveGame, Playing(name, game)) =>
      game ! LeaveGame
      matchmaker ! JoinLobby
      stay using Matchmaking(name, false)
    case Event(msg, Playing(_, game)) =>
      game ! msg
      stay
    case Event(GetLobby, _: Matchmaking) =>
      matchmaker ! GetLobby
      stay
    case Event(w: LobbyStatus, _: Matchmaking) =>
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
    case Event(w: WatchGame, n: Matchmaking) =>
      gameSupervisor ! w
      stay using n
    case Event(InitGame(game, id, state), Matchmaking(name, _)) =>
      sendMessage(GameReady(id, state))
      stay using Playing(name, game)
    case Event(j: JoinGame, m: Matchmaking) =>
      matchmaker ! j
      stay using Matchmaking(m.name, false)
  }

  whenUnhandled {
    case Event(a, b) =>
      println(s"USER: unhandled: $a in $b")
      stay
  }

  initialize()

  def sendMessage(msg: Message) = out ! write(SocketMessage(msg))
}

object UserActor {

  def props(out: ActorRef, connections: ActorRef) =
    Props(new UserActor(out, connections))

  object Internal {
    sealed trait State
    sealed trait Data

    case object NoUser extends State
    case object AwaitingAuthorization extends State
    case object WithUser extends State

    sealed trait Name extends Data {
      def name: String
    }
    
    case object NoData extends Data

    case class LoginRequestData(name: String) extends Data

    case class Matchmaking(
      override val name: String, searching: Boolean) extends Name

    case class Playing(
      override val name: String, game: ActorRef) extends Name
  }
}
