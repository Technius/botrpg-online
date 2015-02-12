package actors

import akka.actor.ActorRef
import botrpg.common._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.WebSocket.FrameFormatter

case object Connect

case class StartGame(player1: ActorRef, player2: ActorRef)

case class InitGame(game: ActorRef)

case object GetName

trait State

trait Data
