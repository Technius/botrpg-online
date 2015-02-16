package controllers

import actors._
import actors.user.UserActor
import akka.actor.Props
import botrpg.common.SocketMessage
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._

object Connections extends Controller {

  val connections = Akka.system.actorOf(
    Props[ConnectionsActor], name = "system")

  def socket = WebSocket.acceptWithActor[String, String] { request => out =>
    UserActor.props(out, connections)
  }
}
