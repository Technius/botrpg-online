package botrpg.client

import botrpg.common._
import scala.annotation.meta.field
import scala.scalajs.js.annotation.JSExport

@JSExport("BotRpg.Game")
class GameState(val game: Game, val result: Option[MatchResult] = None) {
  @JSExport("result") val _result = result map(_.toString) getOrElse null
  @JSExport val player1 = game.player1
  @JSExport val player2 = game.player2
  @JSExport val turn = game.turn
}
