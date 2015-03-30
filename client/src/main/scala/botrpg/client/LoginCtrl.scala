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
  var loggingIn: Boolean = js.native
}

class LoginCtrl(
    $scope: LoginScope,
    $location: Location,
    $connection: Connection) extends Controller {
  $scope.name = ""
  $scope.loggingIn = false
  $scope.login = { () =>
    if (!$scope.loggingIn && !$scope.name.isEmpty) {
      $scope.loggingIn = true
      $connection.openConnection($scope.name).onmessage = { ev: MessageEvent =>
        $scope.loggingIn = false
        read[SocketMessage](ev.data.toString).data match {
          case LoggedIn =>
            $location.path("/lobby")
            $scope.$apply()
          case msg =>
            alert("Error: received unknown response: " + msg.toString)
        }
      }
    }
  }

  $connection.name foreach { _ =>
    $location.path("/lobby")
  }
}
