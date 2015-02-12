package controllers

import actors._
import botrpg.common.SocketMessage
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._

object Connections extends Controller {

  val matchmaker = Akka.system.actorOf(
    MatchmakerActor.props, name = "matchmaker")

  def socket = WebSocket.acceptWithActor[String, String] { request => out =>
    PlayerActor.props(out, matchmaker)
  }
}
