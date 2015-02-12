package models

case class Game(turn: Int = 1, p1: (String, Player), p2: (String, Player)) {

  def player1: Player = p1._2

  def player2: Player = p2._2

  def player1(p1: Player): Game = copy(p1 = (this.p1._1, p1))

  def player2(p2: Player): Game = copy(p2 = (this.p2._1, p2))

  def players(p1: Player, p2: Player) =
    copy(p1 = (this.p1._1, p1), p2 = (this.p2._1, p2))
}

case class Player(health: Int, stamina: Int) {

  def health_=(hp: Int): Player = copy(health = hp)

  def stamina_=(stam: Int): Player = copy(stamina = stam)
}
