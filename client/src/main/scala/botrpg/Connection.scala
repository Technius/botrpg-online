package botrpg

import biz.enef.angular.Service
import biz.enef.angular.core.Location
import org.scalajs.dom._
import upickle._

import common._

class Connection($location: Location) extends Service {

  private[this] var _name: Option[String] = None

  def name: Option[String] = _name
  
  var socket: Option[WebSocket] = None

  def openConnection(name: String): WebSocket = {
    _name = Some(name)
    socket foreach (_.close())
    val sock = new WebSocket("ws://localhost:9000/connect")
    sock.onopen = { ev: Event =>
      sock.send(write(SocketMessage(LoginReq(name))))
    }
    sock.onclose = { ev: CloseEvent =>
      socket = None
    }
    socket = Some(sock)
    sock
  }

  def verifyLogin = {
    val opt = for {
      sock <- socket
      _name <- name
    } yield (sock, _name)

    if (!opt.isDefined)
      $location.path("/")

    opt
  }
}
