package botrpg.client

import biz.enef.angular._
import biz.enef.angular.core.Location
import botrpg.common._
import org.scalajs.dom._
import scala.scalajs.js
import upickle._

trait GameScope extends Scope {

  var game: GameState = js.native

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

  $connection.verifyLogin() map { case (socket, name) =>
    $game.game map { game =>
      $scope.game = game
      $scope.madeMove = () => $game.madeMove
      socket.onmessage = { ev: MessageEvent =>
        $game.processMessage(read[SocketMessage](ev.data.toString).data)
        $scope.game = $game.game getOrElse $scope.game
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
