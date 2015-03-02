package botrpg.common

import scala.annotation.meta.field
import scala.scalajs.js.annotation.JSExport

case class Game(player1: Player, player2: Player, turn: Int = 1)

@JSExport("BotRpg.Player")
case class Player(
    @(JSExport @field) name: String,
    @(JSExport @field) health: Int,
    @(JSExport @field) stamina: Int)
