package actors

import akka.actor.ActorRef
import botrpg.common._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.WebSocket.FrameFormatter

case class Connect(name: String)

case class Disconnect(user: ActorRef)

case class FindUser(name: String)

case object JoinLobby

case object LeaveLobby

case class StartGame(player1: ActorRef, player2: ActorRef)

case class InitGame(game: ActorRef, id: String, initialState: Game)

case class GameStatus(playing: Boolean, state: Game)

case object GetGames

case object GetGameStatus

case object GetName
