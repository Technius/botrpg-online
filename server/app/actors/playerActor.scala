package actors

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

class PlayerActor(
    out: ActorRef,
    matchmaker: ActorRef) extends FSM[State, Data] {

  override def receive = {
    case msg: String => super.receive(read[SocketMessage](msg).data)
    case msg => super.receive(msg)
  }

  startWith(NoUser, NoData)

  when(NoUser) {
    case Event(LoginReq(name), NoData) =>
      out ! write(SocketMessage(LoggedIn))
      goto(WithUser) using Matchmaking(name, false)
  }

  when(WithUser) {
    case Event(GetWaiting, _) =>
      implicit val timeout = Timeout(5 seconds)
      val future = ask(matchmaker, GetWaiting).mapTo[WaitingPlayers]
      future map (msg => write(SocketMessage(msg))) pipeTo out
      stay
    case Event(FindGame, n: Name) =>
      matchmaker ! FindGame
      stay using Matchmaking(n.name, true)
    case Event(RequestGame, n: Name) =>
      matchmaker ! RequestGame
      stay using Matchmaking(n.name, false)
    case Event(GetName, n: Name) =>
      sender() ! n.name
      stay
    case Event(InitGame(game), Matchmaking(name, true)) =>
      stay using Playing(name, game)
    case Event(msg, Playing(_, game)) =>
      game ! msg
      stay
  }

  whenUnhandled {
    case Event(a, b) =>
      stay
  }

  initialize()
}

object PlayerActor {
  def props(out: ActorRef, matchmaker: ActorRef) =
    Props(new PlayerActor(out, matchmaker))
}
