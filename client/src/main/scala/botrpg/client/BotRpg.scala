package botrpg.client

import biz.enef.angular._
import biz.enef.angular.ext.{ Route, RouteProvider }
import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

import directive._

@JSExport("BotRpg")
object BotRpg extends JSApp {
  def main(): Unit = {
    val module = Angular.module("BotRpg", Seq("ngRoute"))

    module.serviceOf[Connection]("$connection")
    module.serviceOf[GameService]("$game")
    module.controllerOf[NavbarCtrl]("NavbarCtrl")
    module.controllerOf[LoginCtrl]("LoginCtrl")
    module.controllerOf[LobbyCtrl]("LobbyCtrl")
    module.controllerOf[GameCtrl]("GameCtrl")

    // module.directiveOf[AutoScrollBottomDirective]("scrollToBottom")

    module.config { $routeProvider: RouteProvider =>
      $routeProvider
        .when("/", Route(controller = "LoginCtrl",
          templateUrl = "/assets/templates/login.html"))
        .when("/lobby", Route(controller = "LobbyCtrl",
          templateUrl = "/assets/templates/lobby.html"))
        .when("/game/:gameId", Route(controller = "GameCtrl",
          templateUrl = "/assets/templates/game.html"))
    }
    
    module.config { $locationProvider: js.Dynamic =>
      $locationProvider.html5Mode(true)
    }
  }
}
