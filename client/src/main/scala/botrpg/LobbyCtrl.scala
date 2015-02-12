package botrpg

import biz.enef.angular._
import org.scalajs.dom._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import upickle._

import common._

trait LobbyScope extends Scope {

  var requestingGame: Boolean = js.native

  var waiting: js.Array[String] = js.native

  var getWaiting: js.Function = js.native

  var requestGame: js.Function = js.native
}

class LobbyCtrl(
    $scope: LobbyScope,
    $connection: Connection) extends Controller {
  $connection.verifyLogin foreach { case (socket, name) =>
    $scope.requestingGame = false
    $scope.waiting = js.Array()

    socket.onmessage = { ev: MessageEvent =>
      read[SocketMessage](ev.data.toString).data match {
        case WaitingPlayers(players) =>
          $scope.waiting = players.toJSArray
        case _ =>
      }
      $scope.$apply()
    }

    $scope.getWaiting = () => socket.send(write(SocketMessage(GetWaiting)))
    $scope.requestGame = () => socket.send(write(SocketMessage(RequestGame)))
  }
}
