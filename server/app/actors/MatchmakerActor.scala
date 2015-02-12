package actors

import actors.game._
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class MatchmakerActor extends Actor {
  val random = new Random
  val pending = ArrayBuffer[ActorRef]()
  val lobby = ArrayBuffer[ActorRef]()

  def receive = {
    case JoinLobby =>
      val user = sender()
      if (!lobby.contains(user)) {
        lobby += user
        sendRequestUpdate()
      }
    case LeaveLobby =>
      val len = pending.length
      leave(Seq(sender()))
      if (pending.length != len) {
        sendRequestUpdate()
      }
    case Disconnect(user) =>
      self tell (LeaveLobby, user)
    case GetWaiting =>
      val originalSender = sender()
      pendingNames map (WaitingPlayers(_)) pipeTo originalSender
    case CancelRequestGame =>
      pending -= sender()
      sendRequestUpdate()
    case RequestGame =>
      val user = sender()
      if (!pending.contains(user)) {
        pending += user
        val nameFut = pendingNames map (WaitingPlayers(_))
        lobby.toList foreach (nameFut pipeTo _)
      }
    case FindGame =>
      if (pending.length > 0) {
        val index = random.nextInt(pending.length)
        val matched = pending(index)
        self ! StartGame(sender(), matched)
        sendRequestUpdate()
      } else {
        context.system.scheduler.scheduleOnce(5.seconds) {
          self forward FindGame
        }
      }
    case msg @ StartGame(p1, p2) =>
      leave(Seq(p1, p2))
      context.actorSelection("..") ! msg
  }

  def leave(users: Seq[ActorRef]) = {
    lobby --= users
    pending --= users
  }

  def sendRequestUpdate() = {
    val nameFut = pendingNames map (WaitingPlayers(_))
    lobby.toList foreach (nameFut pipeTo _)
  }

  def pendingNames = {
    implicit val timeout: Timeout = 5.seconds
    val namesFuture = pending.toList map (a => ask(a, GetName).mapTo[String])
    Future.sequence(namesFuture)
  }
}
