package actors

import actors.game._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import botrpg.common._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.collection.mutable.HashMap
import scala.concurrent.Future
import scala.concurrent.duration._

class ConnectionsActor extends Actor {

  val matchmaker = context.actorOf(Props[MatchmakerActor], name = "matchmaker")
  val gameSupervisor = context.actorOf(Props[GameSupervisor], name = "games")

  val connected = HashMap.empty[String, ActorRef]

  def receive = {
    case Connect(name) =>
      val user = sender()
      if (!connected.contains(name)) {
        connected += (name -> user)
        context.watch(user)
      }
    case Terminated(a) =>
      val opt = connected collectFirst {
        case (name, actor) if actor == a => (name, actor)
      }

      opt foreach { case (name, actor) =>
        connected -= name
        matchmaker ! Disconnect(actor)
        gameSupervisor ! Disconnect(actor)
      }
    case FindUser(name) =>
      sender() ! connected.get(name)
  }
}
