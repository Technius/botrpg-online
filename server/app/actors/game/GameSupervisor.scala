package actors.game

import actors._
import akka.actor._
import botrpg.common._
import java.util.UUID

class GameSupervisor extends Actor {

  val games = scala.collection.mutable.ArrayBuffer[ActorRef]()

  def receive = {
    case StartGame(p1, p2) =>
      val id = UUID.randomUUID
      val game = context.actorOf(
        GameActor.props(id, p1, p2), name = id.toString)
      games += game
      context.watch(game)
    case Disconnect(a) => games foreach (_ tell (LeaveGame, a))
    case Terminated(a) => if (games.contains(a)) games -= a
  }
}
