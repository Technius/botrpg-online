import actors._
import actors.game._
import actors.user._
import akka.actor._
import akka.testkit._
import botrpg.common._
import java.util.UUID
import models._
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.reflectiveCalls

class GameSpec(_system: ActorSystem) extends TestKit(_system)
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("GameSpec"))

  override def afterAll = TestKit.shutdownActorSystem(system)

  def fixture = new {
    val matchProbe = TestProbe()
    val outProbes @ List(probe1, probe2) = List(TestProbe(), TestProbe())
    val List(player1, player2) = outProbes map (p => TestFSMRef(
      new UserActor(p.ref, matchProbe.ref)))
    player1.setState(WithUser, Matchmaking("player1", false)) //workaround
    player2.setState(WithUser, Matchmaking("player2", false))
    val game = TestActorRef(new GameActor(UUID.randomUUID(), player1, player2))
    val originalState = game.underlyingActor.state.copy()
    player1.setState(stateData = Playing("player1", game))
    player2.setState(stateData = Playing("player2", game))

    def reset() = { game.underlyingActor.state = originalState }
  }

  "A game" should {
    val f = fixture
    val gameActor = f.game.underlyingActor

    "start at turn 1" in {
      gameActor.state.turn shouldBe 1
    }
    
    "advance to the next turn" in {
      gameActor.processTurn(Wait, Wait)
      gameActor.state.turn shouldBe 2
    }

    "end when player1 loses all health" in {
      gameActor.state = f.originalState.copy(
        p1 = (gameActor.state.p1._1, Player(0, 0))
      )
      gameActor.processTurn(Wait, Wait)
      gameActor.playing shouldBe false
    }

    "end when player2 loses all health" in {
      gameActor.state = f.originalState.copy(
        p2 = (gameActor.state.p2._1, Player(0, 0))
      )
      gameActor.processTurn(Wait, Wait)
      gameActor.playing shouldBe false
    }

    "end when a player disconnects" in {
      gameActor.state = f.originalState
      f.game ! Disconnect(f.player1)
      gameActor.playing shouldBe false
    }
  }

  "A player" when {
    val f = fixture
    val gameActor = f.game.underlyingActor

    "attacking" should {
      "lose stamina" in {
        val original = gameActor.state.player1.stamina
        gameActor.processTurn(Attack, Wait)
        gameActor.state.player1.health should not be original
      }

      "cause damage to an undefended player" in {
        val original = gameActor.state.player2.health
        gameActor.processTurn(Attack, Wait)
        gameActor.state.player2.health should be < original
      }
    }

    f.reset()

    "defending" should {
      "lose stamina" in {
        val original = gameActor.state.player1.stamina
        gameActor.processTurn(Defend, Wait)
        gameActor.state.player1.stamina should not be original
      }
      "negate attacks" in {
        val original = gameActor.state.player2.health
        gameActor.processTurn(Attack, Defend)
        gameActor.state.player2.health shouldBe original
      }
    }

    "waiting" should {
      "regain stamina" in {
        val original = gameActor.state.player2.stamina
        gameActor.processTurn(Wait, Wait)
        gameActor.state.player2.stamina should be > original
      }
    }
  }
}
