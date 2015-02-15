package botrpg.client

import biz.enef.angular._
import botrpg.common._
import org.scalajs.dom._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import upickle._

trait LobbyScope extends Scope {

  var requestingGame: Boolean = js.native

  var waiting: js.Array[String] = js.native

  var getWaiting: js.Function = js.native

  var requestGame: js.Function = js.native

  var cancelRequestGame: js.Function = js.native

  var playGame: js.Function = js.native

  var name: String = js.native
}

class LobbyCtrl(
    $scope: LobbyScope,
    $game: GameService,
    $connection: Connection) extends Controller {
  $connection.verifyLogin foreach { case (socket, name) =>
    $scope.requestingGame = false
    $scope.waiting = js.Array()

    socket.onmessage = { ev: MessageEvent =>
      read[SocketMessage](ev.data.toString).data match {
        case WaitingPlayers(players) => $scope.waiting = players.toJSArray
        case GameReady(id, game) => $game.startGame(game, id)
        case _ =>
      }
      $scope.$apply()
    }

    $scope.getWaiting = () => $connection.sendMessage(GetWaiting)
    $scope.requestGame = () => {
      $connection.sendMessage(RequestGame)
      $scope.requestingGame = true
    }
    $scope.cancelRequestGame = () => {
      $connection.sendMessage(CancelRequestGame)
      $scope.requestingGame = false
    }
    $scope.playGame = { name: String =>
      $connection.sendMessage(JoinGame(name))
    }
    $scope.name = name
  }
}
