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
      sender() ! (id, game)
    case WatchGame(id) =>
      val originalSender = sender()
      games.toList find (_._1 == id) map (_._2) foreach { game =>
        implicit val timeout = Timeout(1.second)
        val statusFuture = ask(game, GetGameStatus).mapTo[GameStatus]
        statusFuture onSuccess {
          case s: GameStatus => originalSender ! s
        }
      }
    case GetGames =>
      Future.traverse(games.toList) { t =>
        implicit val timeout = Timeout(1.second)
        ask(t._2, GetGameStatus).mapTo[GameStatus] map (t -> _.playing)
      } map (_ filter (_._2) map (_._1)) pipeTo sender()
    case Disconnect(a) => games foreach (_._2 tell (LeaveGame, a))
    case Terminated(a) => games --= games filter (_._2 == a)
  }
}
