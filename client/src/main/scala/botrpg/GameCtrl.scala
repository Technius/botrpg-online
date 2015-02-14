package botrpg

import biz.enef.angular._
import biz.enef.angular.core.Location
import org.scalajs.dom._
import scala.scalajs.js
import upickle._

import common._

trait GameScope extends Scope {

  var game: js.Function = js.native
}

class GameCtrl(
    $scope: GameScope,
    $location: Location,
    $connection: Connection,
    $game: GameService) extends Controller {

  $connection.verifyLogin() map { case (socket, name) =>
    $game.game map { game =>
      $scope.game = () => game // placeholder- should be reactive
      socket.onmessage = { ev: MessageEvent =>
        read[SocketMessage](ev.data.toString).data match {
          case msg => println(msg)
        }
      }
    } getOrElse {
      $location.path("/lobby")
    }
  } 
}
