package actors

import actors.game._
import akka.actor._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import botrpg.common._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.collection.mutable.Set
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Random, Success }

class MatchmakerActor extends Actor {
  val random = new Random
  val pending = Set[ActorRef]()
  val lobby = Set[ActorRef]()

  def receive = {
    case JoinLobby =>
      lobby += sender()
      sendRequestUpdate()
    case LeaveLobby => leave(Seq(sender()))
    case Disconnect(user) => self tell (LeaveLobby, user)
    case GetWaiting =>
      val originalSender = sender()
      pendingNames map (WaitingPlayers(_)) pipeTo originalSender
    case CancelRequestGame =>
      pending -= sender()
      sendRequestUpdate()
    case RequestGame =>
      val user = sender()
      pending += user
      val nameFut = pendingNames map (WaitingPlayers(_))
      lobby.toVector foreach (nameFut pipeTo _)
    case FindGame =>
      val choices = pending.toVector
      if (choices.length > 0) {
        val index = random.nextInt(choices.length)
        val matched = choices(index)
        self ! StartGame(sender(), matched)
      } else {
        context.system.scheduler.scheduleOnce(5.seconds) {
          self forward FindGame
        }
      }
    case JoinGame(name) =>
      val originalSender = sender()
      implicit val timeout: Timeout = 5.seconds
      Future.traverse(pending.toSet) { a =>
        ask(a, GetName).mapTo[String] map ((a, _))
      } onComplete {
        case Success(names) =>
          names find (_._2 == name) map {
            case (matched, _) => self ! StartGame(originalSender, matched)
          }
        case fail => println(fail)
      }
    case msg @ StartGame(p1, p2) =>
      leave(Seq(p1, p2))
      context.actorSelection("../games") ! msg
  }

  def leave(users: Seq[ActorRef]) = {
    val len = pending.size
    lobby --= users
    pending --= users
    if (pending.size != len) {
      sendRequestUpdate()
    }
  }

  def sendRequestUpdate() = {
    val nameFut = pendingNames map (WaitingPlayers(_))
    lobby.toList foreach (nameFut pipeTo _)
  }

  def pendingNames = {
    implicit val timeout: Timeout = 5.seconds
    val namesFuture = pending.toList map (ask(_, GetName).mapTo[String])
    Future.sequence(namesFuture)
  }
}
