package botrpg.client

import biz.enef.angular.Service
import biz.enef.angular.core.Location
import botrpg.common._
import org.scalajs.dom._
import upickle._

class GameService(
    $connection: Connection,
    $location: Location) extends Service {

  private[this] var _game: Option[GameState] = None

  var madeMove: Boolean = false

  def game = _game

  def playerData = _game flatMap { g =>
    $connection.name flatMap { name =>
      if (name == g.player1.name) Some(g.player1)
      else if (name == g.player2.name) Some(g.player2)
      else None
    }
  }

  def startGame(game: Game, id: String) = {
    updateState(game)
    $location.path(s"/game/$id")
    madeMove = false
  }

  def makeMove(move: Move) = {
    $connection.sendMessage(MakeMove(move))
    madeMove = true
  }

  def processMessage(m: Message) = m match {
    case GameUpdate(state) =>
      updateState(state)
      madeMove = false
    case result: MatchResult =>
      _game = _game map (state => new GameState(state.game, Some(result)))
    case msg => println("Unrecognized message: " + msg)
  }

  def updateState(state: Game) = {
    _game = Some(new GameState(state, _game flatMap(_.result)))
  }
}
