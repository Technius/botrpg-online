package botrpg.common

sealed trait Message

case class SocketMessage(data: Message)

// Server outbound

case object LoggedIn extends Message

case class GameReady(id: String, game: Game) extends Message

case class GameUpdate(game: Game) extends Message

case class WaitingPlayers(waiting: List[String]) extends Message

sealed trait MatchResult extends Message

case object Victory extends MatchResult

case object Draw extends MatchResult

case object Defeat extends MatchResult

// Server inbound

case class LoginReq(name: String) extends Message 
 
case object GetWaiting extends Message 

case object CancelRequestGame extends Message
 
case object RequestGame extends Message 

case class JoinGame(name: String) extends Message

case object FindGame extends Message 
 
case class MakeMove(move: Move) extends Message

case object LeaveGame extends Message
