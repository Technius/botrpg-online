package actors

import actors.game._
import akka.actor._
import botrpg.common._
import scala.collection.mutable.ArrayBuffer

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
  }
}
