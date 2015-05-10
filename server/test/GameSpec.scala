import actors._
import actors.game._
import actors.game.GameActor.Internal._
import actors.user._
import akka.actor._
import akka.testkit._
import botrpg.common._
import java.util.UUID
import models._
import scala.language.reflectiveCalls

class GameSpec extends BotRpgSpec("GameSpec") {

  def fixture = new {
    val matchProbe = TestProbe()
    val outProbes = List(TestProbe(), TestProbe())
    val List(player1, player2) = outProbes map (p => TestFSMRef(
      new UserActor(p.ref, matchProbe.ref)))

    import actors.user.UserActor.{ Internal => PI }
    player1.setState(PI.WithUser, PI.Matchmaking("player1", false)) //workaround
    player2.setState(PI.WithUser, PI.Matchmaking("player2", false))
    val game = TestFSMRef(new GameActor(UUID.randomUUID()))
    player1.setState(stateData = PI.Playing("player1", game))
    player2.setState(stateData = PI.Playing("player2", game))

    game ! StartGame(player1, player2)

    lazy val originalState = game.stateData.asInstanceOf[Match]
    lazy val originalGameState = game.stateData.asInstanceOf[Match].state

    def reset() = game.setState(Playing, originalState)
    def gameState = game.stateData.asInstanceOf[Match].state
    def advTurn(p1: Move, p2: Move) = {
      game.tell(MakeMove(p1), player1)
      game.tell(MakeMove(p2), player2)
    }
  }

  "A game" should {
    val f = fixture
    val gameActor = f.game

    "initialize" in {
      gameActor.stateName shouldBe Playing
      gameActor.stateData shouldBe a [Match]
    }

    "start at turn 1" in {
      f.gameState.turn shouldBe 1
    }
    
    "advance to the next turn" in {
      f.advTurn(Wait, Wait)
      f.gameState.turn shouldBe 2
    }

    "end when player1 loses all health" in {
      f.reset()
      gameActor.setState(stateData = f.originalState.copy(
        state = f.originalGameState.copy(
          player1 = f.originalGameState.player1.copy(health = 0, stamina = 0)
        )
      ))
      f.advTurn(Wait, Wait)
      gameActor.stateName shouldBe Finished
    }

    "end when player2 loses all health" in {
      f.reset()
      gameActor.setState(stateData = f.originalState.copy(
        state = f.originalGameState.copy(
          player2 = f.originalGameState.player2.copy(health = 0, stamina = 0)
        )
      ))
      f.advTurn(Wait, Wait)
      gameActor.stateName shouldBe Finished
    }

    "end when a player disconnects" in {
      f.reset()
      gameActor.tell(LeaveGame, f.player1)
      gameActor.stateName shouldBe Finished
    }
  }

  "A player" when {
    val f = fixture
    val gameActor = f.game

    "attacking" should {
      "lose stamina" in {
        val original = f.gameState.player1.stamina
        f.advTurn(Attack, Wait)
        f.gameState.player1.health should not be original
      }

      "cause damage to an undefended player" in {
        val original = f.gameState.player2.health
        f.advTurn(Attack, Wait)
        f.gameState.player2.health should be < original
      }
    }

    f.reset()

    "defending" should {
      "lose stamina" in {
        val original = f.gameState.player1.stamina
        f.advTurn(Defend, Wait)
        f.gameState.player1.stamina should not be original
      }
      "negate attacks" in {
        val original = f.gameState.player2.health
        f.advTurn(Attack, Defend)
        f.gameState.player2.health shouldBe original
      }
    }

    "waiting" should {
      "regain stamina" in {
        val original = f.gameState.player2.stamina
        f.advTurn(Wait, Wait)
        f.gameState.player2.stamina should be > original
      }
    }
  }
}
