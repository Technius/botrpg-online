package botrpg.client

import botrpg.common._
import scala.annotation.meta.field
import scala.scalajs.js.annotation.JSExport

@JSExport("BotRpg.Player")
class PlayerData(
    @(JSExport @field) val name: String,
    @(JSExport @field) val health: Int,
    @(JSExport @field) val stamina: Int) {
}

object PlayerData {
  def fromPlayer(p: (String, Player)) = {
    val (name, Player(health, stamina)) = p
    new PlayerData(name, health, stamina)
  }
}

@JSExport("BotRpg.Game")
class GameState(val game: Game, val result: Option[MatchResult] = None) {
  @JSExport("result") val _result = result map(_.toString) getOrElse null
  @JSExport val player1 = PlayerData.fromPlayer(game.p1)
  @JSExport val player2 = PlayerData.fromPlayer(game.p2)
  @JSExport val turn = game.turn
}
