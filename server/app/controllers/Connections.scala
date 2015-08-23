package controllers

import actors._
import actors.user.UserActor
import akka.actor.{ ActorSystem, Props }
import botrpg.common.SocketMessage
import javax.inject.{ Inject, Singleton }
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._

@Singleton
class Connections @Inject()(system: ActorSystem) extends Controller {

  val connections = system.actorOf(
    Props[ConnectionsActor], name = "system")

  def socket = WebSocket.acceptWithActor[String, String] { request => out =>
    UserActor.props(out, connections)
  }
}
