package botrpg.client

import biz.enef.angular._
import biz.enef.angular.core.Location
import botrpg.common._
import org.scalajs.dom._
import scala.scalajs.js
import upickle._

trait LoginScope extends Scope {
  var name: String = js.native
  var login: js.Function = js.native
}

class LoginCtrl(
    $scope: LoginScope,
    $location: Location,
    $connection: Connection) extends Controller {
  $scope.name = ""
  $scope.login = { () =>
    if (!$scope.name.isEmpty) {
      $connection.openConnection($scope.name).onmessage = { ev: MessageEvent =>
        read[SocketMessage](ev.data.toString).data match {
          case LoggedIn =>
            $scope.$apply {
              $location.path("/lobby")
            }
          case msg =>
            alert("Error: received unknown response: " + msg.toString)
            alert("Oops, failed to log in. Please try again.")
        }
      }
    }
  }

  $connection.name foreach { _ =>
    $location.path("/lobby")
  }
}