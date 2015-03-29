package actors.game

import actors._
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import java.util.UUID
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration._

class GameSupervisor extends Actor {

  val games = scala.collection.mutable.ArrayBuffer[(UUID, ActorRef)]()

  def receive = {
    case StartGame(p1, p2) =>
      val id = UUID.randomUUID
      val game = context.actorOf(
        GameActor.props(id, p1, p2), name = id.toString)
      games += (id -> game)
      context.watch(game)
      sender() ! (id -> game)
    case msg @ WatchGame(id) =>
      games.toList find (_._1.toString == id) map (_._2) foreach (_ forward msg)
    case GetGames =>
      Future.traverse(games.toList) { t =>
        implicit val timeout = Timeout(1.second)
        ask(t._2, GetGameStatus).mapTo[GameStatus] map ((t._1, t._2,  _))
      } map (_ filter (_._3.playing)) pipeTo sender()
    case Disconnect(a) => games foreach (_._2 tell (LeaveGame, a))
    case Terminated(a) => games --= games filter (_._2 == a)
  }
}
