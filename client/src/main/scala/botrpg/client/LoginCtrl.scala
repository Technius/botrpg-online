package botrpg.client

import biz.enef.angulate._
import biz.enef.angulate.core.Location
import botrpg.common._
import org.scalajs.dom._
import scala.scalajs.js
import upickle.default._

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
          case LoginFail(reason) =>
            $connection.logout()
            alert("Failed to log in: " + reason) // TODO: use HTML
          case msg =>
            $connection.logout()
            alert("Error: received unknown response: " + msg.toString)
        }
        $scope.$apply()
      }
    }
  }

  $connection.name foreach { _ =>
    $location.path("/lobby")
  }
}
