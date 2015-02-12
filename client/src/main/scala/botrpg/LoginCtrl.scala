package botrpg

import biz.enef.angular._
import biz.enef.angular.core.Location
import org.scalajs.dom._
import scala.scalajs.js
import upickle._

import common._

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
        if (read[SocketMessage](ev.data.toString).data == LoggedIn) {
          $scope.$apply {
            $location.path("/lobby")
          }
        } else {
          alert("Oops, failed to log in. Try again.")
        }
      }
    }
  }

  $connection.name foreach { _ =>
    $location.path("/lobby")
  }
}
