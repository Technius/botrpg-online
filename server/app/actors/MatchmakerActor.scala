package actors

import actors.game._
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class MatchmakerActor extends Actor {
  val random = new Random
  var pending = scala.collection.mutable.ArrayBuffer[ActorRef]()
  def receive = {
    case GetWaiting =>
      implicit val timeout: Timeout = 5 seconds
      val waiting = pending.toList
      val originalSender = sender()
      val namesFuture = waiting map (a => ask(a, GetName).mapTo[String])
      val combinedFuture = Future.sequence(namesFuture)
      combinedFuture map (WaitingPlayers(_)) pipeTo originalSender
    case RequestGame => pending += sender()
    case FindGame =>
      if (pending.length > 0) {
        val index = random.nextInt(pending.length)
        val matched = pending(index)
        self ! StartGame(sender(), matched)
      } else {
        context.system.scheduler.scheduleOnce(5 seconds) {
          self forward FindGame
        }
      }
    case StartGame(p1, p2) =>
      pending --= Seq(p1, p2)
      println(pending)
      context.actorOf(GameActor.props(p1, p2, self))
  }
}

object MatchmakerActor {
  def props: Props = Props(new MatchmakerActor)
}
