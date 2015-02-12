package botrpg.common

sealed trait Message

case class SocketMessage(data: Message)

// Outbound

case object LoggedIn extends Message

case class GameReady(id: String) extends Message

case class WaitingPlayers(waiting: List[String]) extends Message

// Inbound

case object FindGame extends Message 
 
case class LoginReq(name: String) extends Message 
 
case object GetWaiting extends Message 

case object CancelQueue extends Message
 
case object RequestGame extends Message 
