package botrpg.client

import biz.enef.angulate._
import biz.enef.angulate.core.Location
import botrpg.common._
import org.scalajs.dom._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import upickle.default._

trait GameScope extends Scope {

  var game: GameState = js.native

  var log: js.Array[String] = js.native

  var player: js.Function = js.native
  
  var madeMove: js.Function = js.native

  var moveAttack: js.Function = js.native

  var moveDefend: js.Function = js.native

  var moveWait: js.Function = js.native

  var turn: js.Function = js.native

  var leaveGame: js.Function = js.native
}

class GameCtrl(
    $scope: GameScope,
    $location: Location,
    $connection: Connection,
    $game: GameService) extends Controller {

  $scope.log = js.Array()

  $connection.verifyLogin() map { case (socket: WebSocket, name: String) =>
    $game.game map { game =>
      $scope.game = game
      $scope.madeMove = () => $game.madeMove
      socket.onmessage = { ev: MessageEvent =>
        $game.processMessage(read[SocketMessage](ev.data.toString).data)
        $scope.game = $game.game getOrElse $scope.game
        if ($scope.log.length != $game.gameLog.length) {
          $scope.log = $game.gameLog.toSeq.toJSArray
        }
        $scope.$apply()
      }
    } getOrElse {
      $location.path("/lobby")
    }
    $scope.leaveGame = { () =>
      $connection.sendMessage(LeaveGame)
      $location.path("/lobby")
    }
  }

  $scope.player = () => $game.playerData getOrElse null
  $scope.moveAttack = () => $game.makeMove(Attack)
  $scope.moveDefend = () => $game.makeMove(Defend)
  $scope.moveWait = () => $game.makeMove(Wait)
}
