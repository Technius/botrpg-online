package botrpg

import biz.enef.angular.Service
import biz.enef.angular.core.Location
import org.scalajs.dom._

import common._

class GameService(
    $connection: Connection,
    $location: Location) extends Service {

  private[this] var _game: Option[Game] = None

  def game = _game

  def startGame(game: Game, id: String) = {
    _game = Some(game)
    $location.path(s"/game/$id")
  }
}
