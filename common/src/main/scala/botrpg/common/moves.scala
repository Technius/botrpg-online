package botrpg.common

sealed trait Move {
  val staminaCost: Int = 0
}

case object Attack extends Move {
  override val staminaCost = 10
}

case object Defend extends Move {
  override val staminaCost = 5
}

case object Wait extends Move {
  override val staminaCost = -15
}
