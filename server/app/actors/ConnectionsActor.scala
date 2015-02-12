package actors

import actors.game._
import akka.actor._
import botrpg.common._

class ConnectionsActor extends Actor {

  val matchmaker = context.actorOf(Props[MatchmakerActor], name = "matchmaker")

  val connected = scala.collection.mutable.ArrayBuffer[ActorRef]()

  def receive = {
    case Connect =>
      val user = sender()
      if (!connected.contains(user)) {
        connected += user
        context.watch(user)
      }
    case Terminated(user) =>
      connected -= user
      matchmaker ! Disconnect(user)
  }
}
