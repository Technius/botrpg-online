package botrpg.common

import scala.annotation.meta.field
import scala.scalajs.js.annotation.JSExport

case class Game(player1: Player, player2: Player, turn: Int = 1)

@JSExport("BotRpg.Player")
case class Player(
    @(JSExport @field) name: String,
    @(JSExport @field) health: Int,
    @(JSExport @field) stamina: Int)

@JSExport("BotRpg.GameSummary")
case class GameSummary(
    @(JSExport @field) id: String,
    @(JSExport @field) player1: String,
    @(JSExport @field) player2: String)

object GameSummary {
  def from(pair: (String, Game)): GameSummary = {
    GameSummary(pair._1, pair._2.player1.name, pair._2.player2.name)
  }
}
