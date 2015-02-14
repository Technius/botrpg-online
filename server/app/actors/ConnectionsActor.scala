package actors

import actors.game._
import akka.actor._
import botrpg.common._
import scala.collection.mutable.ArrayBuffer

class ConnectionsActor extends Actor {

  val matchmaker = context.actorOf(Props[MatchmakerActor], name = "matchmaker")

  val connected = ArrayBuffer[ActorRef]()
  val games = ArrayBuffer[ActorRef]()

  def receive = {
    case Connect =>
      val user = sender()
      if (!connected.contains(user)) {
        connected += user
        context.watch(user)
      }
    case StartGame(p1, p2) =>
      val game = context.actorOf(GameActor.props(p1, p2, matchmaker))
      games += game
    case Terminated(user) =>
      connected -= user
      matchmaker ! Disconnect(user)
  }
}
