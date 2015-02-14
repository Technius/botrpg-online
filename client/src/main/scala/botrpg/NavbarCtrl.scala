package botrpg

import biz.enef.angular._
import scalajs.js

trait NavbarScope extends Scope {

  var name: js.Function = js.native

  var logout: js.Function = js.native
}

class NavbarCtrl(
    $scope: NavbarScope,
    $connection: Connection) extends Controller {

  $scope.name = () => $connection.name getOrElse null

  $scope.logout = () => $connection.logout
}
