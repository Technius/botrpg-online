package botrpg.client

import botrpg.common._
import scala.annotation.meta.field
import scala.scalajs.js.annotation.JSExport

@JSExport("BotRpg.Game")
class GameState(
    val game: Game,
    val localResult: Option[MatchResult] = None,
    val result: Option[GameEnd] = None) {
  @JSExport("result") val _result = result map { r =>
    new GameStateResult(r, localResult)
  } getOrElse null
  @JSExport val player1 = game.player1
  @JSExport val player2 = game.player2
  @JSExport val turn = game.turn
}

@JSExport("BotRpg.Game.Result")
class GameStateResult(
    result: GameEnd,
    localResult: Option[MatchResult] = None) {
  @JSExport("self") val resultSelf =
    localResult map (_.toString) getOrElse null
  @JSExport val p1 = result.p1Result.toString
  @JSExport val p2 = result.p2Result.toString
}
