package botrpg.client

import biz.enef.angulate.Service
import biz.enef.angulate.core.Location
import botrpg.common._
import org.scalajs.dom._
import upickle._

class Connection($location: Location) extends Service {

  private[this] var _name: Option[String] = None

  def name: Option[String] = _name
  
  var socket: Option[WebSocket] = None

  def openConnection(name: String): WebSocket = {
    _name = Some(name)
    socket foreach (_.close())
    val protocol = if (location.protocol == "https:") "wss" else "ws"
    val sock = new WebSocket(s"$protocol://${location.host}/connect")
    sock.onopen = { ev: Event =>
      sendMessage(LoginReq(name))
    }
    sock.onclose = { ev: CloseEvent =>
      socket = None
    }
    socket = Some(sock)
    sock
  }

  def sendMessage(msg: Message) = socket foreach { sock =>
    sock.send(write(SocketMessage(msg)))
  }

  def logout() = socket foreach { sock =>
    sock.close(1000, "logout")
    socket = None
    _name = None
    $location.path("/")
  }

  def verifyLogin(): Option[(WebSocket, String)] = {
    val opt = for {
      sock <- socket
      _name <- name
    } yield (sock, _name)

    if (!opt.isDefined)
      $location.path("/")

    opt
  }
}
