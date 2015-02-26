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

  val gameLog = scala.collection.mutable.ArrayBuffer[String]()

  var madeMove: Boolean = false

  def game = _game

  def playerData = _game flatMap { g =>
    whichPlayer(g) map (if (_) g.player1 else g.player2)
  }

  def whichPlayer(g: GameState) = $connection.name flatMap { name =>
    if (name == g.player1.name) Some(true)
    else if (name == g.player2.name) Some(false)
    else None
  }

  def startGame(game: Game, id: String) = {
    _game = Some(new GameState(game, None))
    gameLog.clear()
    $location.path(s"/game/$id")
    madeMove = false
  }

  def makeMove(move: Move) = {
    $connection.sendMessage(MakeMove(move))
    madeMove = true
  }

  def processMessage(m: Message) = m match {
    case GameUpdate(state, (p1Move, p2Move)) =>
      val newState = updateState(state)
      val logs = whichPlayer(newState) map { p1OrP2 =>
        val serverPlayers = (state.p1, state.p2)
        val (selfP, otherP) = if (p1OrP2) serverPlayers else serverPlayers.swap
        val otherName = otherP._1
        val (selfMove, otherMove) = if (p1OrP2) {
          (p1Move, p2Move)
        } else {
          (p2Move, p1Move)
        }
        Turn.log("You", otherName, selfMove, otherMove)
      } getOrElse {
        Turn.log(state.p1._1, state.p2._1, p1Move, p2Move, false)
      }
      gameLog ++= logs
      madeMove = false
    case result: GameEnd =>
      _game = _game map { state =>
        val localResult = whichPlayer(state) map { p1OrP2 =>
          if (p1OrP2) result.p1Result
          else result.p2Result
        } getOrElse result.p1Result
        new GameState(state.game, Some(localResult))
      }
    case msg => println("Unrecognized message: " + msg)
  }

  def updateState(state: Game) = {
    val newState = new GameState(state, _game flatMap(_.result))
    _game = Some(newState)
    newState
  }
}
