package actors

import actors.game._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import botrpg.common._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.duration._

class ConnectionsActor extends Actor {

  val matchmaker = context.actorOf(Props[MatchmakerActor], name = "matchmaker")
  val gameSupervisor = context.actorOf(Props[GameSupervisor], name = "games")

  val connected = ArrayBuffer[ActorRef]()

  def receive = {
    case Connect =>
      val user = sender()
      if (!connected.contains(user)) {
        connected += user
        context.watch(user)
      }
    case Terminated(a) =>
      if (connected.contains(a)) {
        connected -= a
        matchmaker ! Disconnect(a)
        gameSupervisor ! Disconnect(a)
      }
    case FindUser(name) =>
      implicit val timeout: Timeout = 5.seconds
      val originalSender = sender()
      Future.traverse(connected) { user =>
        (user ? GetName).mapTo[String] map (name => user -> name)
      } onSuccess { case users =>
        originalSender ! (users find (_._2 equals name) map (_._1))
      }
  }
}
