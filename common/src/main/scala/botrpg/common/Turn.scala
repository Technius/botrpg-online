package botrpg.common

import botrpg.common.{ MatchResult => Result }

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

  def resultOpt(player1: Player, player2: Player): Option[(Result, Result)] = {
    val p1Lose = player1.health <= 0
    val p2Lose = player2.health <= 0
    if (p1Lose && p2Lose) Some((Draw, Draw))
    else if (p1Lose) Some((Defeat, Victory))
    else if (p2Lose) Some((Victory, Defeat))
    else None
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
      p1Name: String,
      p2Name: String,
      p1Move: Move,
      p2Move: Move,
      fromSelf: Boolean = true): Seq[String] = {
    val moveTup = (p1Move, p2Move)
    val moveSeq = Seq(moveTup, moveTup.swap)
    val p1MsgFmt = moveMessage(p1Move, p2Move)
    val p2MsgFmt = moveMessage(p2Move, p1Move)
    val p1Msg = formatMessage(p1MsgFmt, p1Name, p2Name, fromSelf)
    val p2Msg = formatMessage(p2MsgFmt, p2Name, p1Name, false)
    if (moveConflicts.intersect(moveSeq).length > 0) Seq(p1Msg)
    else Seq(p1Msg, p2Msg)
  }
}
