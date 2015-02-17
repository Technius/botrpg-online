package botrpg.common

object Turn {

  def resolve(
      player: Player,
      selfMove: Move,
      otherMove: Move): Player = {

    val stamina = player.stamina - selfMove.staminaCost
    val health = player.health - ((selfMove, otherMove) match {
      case (Defend, Attack) => 0
      case (_, Attack) => 20
      case _ => 0
    })

    player.copy(
      health = math.max(0, health),
      stamina = math.min(100, math.max(0, stamina))
    )
  }
}
