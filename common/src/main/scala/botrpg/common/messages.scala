package botrpg.common

sealed trait Message

case class SocketMessage(data: Message)

// Server outbound

sealed trait MatchResult extends Message

case object Victory extends MatchResult

case object Draw extends MatchResult

case object Defeat extends MatchResult

case object LoggedIn extends Message

case class GameReady(id: String, game: Game) extends Message

case class GameUpdate(game: Game, moves: (Move, Move)) extends Message

case class LobbyStatus(
    waiting: List[String],
    games: List[GameSummary]) extends Message

case class GameEnd(p1Result: MatchResult, p2Result: MatchResult) extends Message

// Server inbound

case class LoginReq(name: String) extends Message 
 
case object GetLobby extends Message 

case object CancelRequestGame extends Message
 
case object RequestGame extends Message 

case class WatchGame(name: String) extends Message

case class JoinGame(name: String) extends Message

case object FindGame extends Message 
 
case class MakeMove(move: Move) extends Message

case object LeaveGame extends Message
