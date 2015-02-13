package botrpg

import biz.enef.angular._
import biz.enef.angular.ext.{ Route, RouteProvider }
import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport("BotRpg")
object BotRpg extends JSApp {
  def main(): Unit = {
    val module = Angular.module("BotRpg", Seq("ngRoute"))

    module.serviceOf[Connection]("$connection")
    module.controllerOf[LoginCtrl]("LoginCtrl")
    module.controllerOf[LobbyCtrl]("LobbyCtrl")

    module.config { $routeProvider: RouteProvider =>
      $routeProvider
        .when("/",
          Route(controller = "LoginCtrl", templateUrl = "/assets/templates/login.html"))
        .when("/lobby",
          Route(controller="LobbyCtrl", templateUrl = "/assets/templates/lobby.html"))
//        .when("/play", Route(controller = "", templateUrl = ""))
    }
    
    module.config { $locationProvider: js.Dynamic =>
      $locationProvider.html5Mode(true)
    }
  }
}
