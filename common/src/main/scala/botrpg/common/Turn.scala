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

  def moveMessage(selfMove: Move, otherMove: Move): String =
    (selfMove, otherMove) match {
      case (Attack, Defend) => "%2$s attack is blocked by %3$s."
      case (Attack, _) => "%1$s attack%6$s %3$s."
      case (Defend, Attack) => "%1$s block%6$s %4$s attack."
      case (Defend, _) => "%1$s defend%6$s."
      case (Wait, _) => "%1$s wait%6$s and regain%5$s stamina."
    }

  def formatMessage(
      message: String,
      selfName: String,
      otherName: String,
      fromSelf: Boolean): String = {
    // 1 = self
    // 2 = self possessive
    // 3 = other
    // 4 = other poseessive
    // 5 = s if fromSelf
    // 6 = s if NOT fromSelf
    val self2 = if (selfName == "You") "Your" else selfName + "'s"
    val other2 = if (otherName == "You") "Your" else otherName + "'s"
    message format (
      selfName, self2,
      otherName, other2,
      if (fromSelf) "" else "s",
      if (!fromSelf) "s" else ""
    )
  }

  val moveConflicts = Seq(Attack -> Defend)

  def log(
      selfName: String,
      otherName: String,
      selfMove: Move,
      otherMove: Move): Seq[String] = {
    val moveTup = (selfMove, otherMove)
    val moveSeq = Seq(moveTup, moveTup.swap)
    val selfMsgFmt = moveMessage(selfMove, otherMove)
    val otherMsgFmt = moveMessage(otherMove, selfMove)
    val selfMsg = formatMessage(selfMsgFmt, selfName, otherName, true)
    val otherMsg = formatMessage(otherMsgFmt, otherName, selfName, false)
    if (moveConflicts.intersect(moveSeq).length > 0) Seq(selfMsg)
    else Seq(selfMsg, otherMsg)
  }
}
