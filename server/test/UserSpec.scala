import actors._
import actors.game._
import akka.actor._
import akka.testkit._
import botrpg.common._
import models._
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import scala.concurrent._
import scala.concurrent.duration._

class UserSpec(_system: ActorSystem) extends TestKit(_system)
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("GameSpec"))

  override def afterAll = TestKit.shutdownActorSystem(system)

  def fixture = new {
    val out = TestProbe()
    val matchmaker = TestProbe()
    val user = TestFSMRef(new PlayerActor(out.ref, matchmaker.ref))
  }

  "A user" when {
    val f = fixture
    "not logged in" should {
      "have no data" in {
        f.user.stateName shouldBe NoUser
      }

      "have no name" in {
        f.user.stateData should not be a [Name]
      }
    }

    "logging in" should {
      "switch to matchmaking" in {
        f.user ! LoginReq("user")
        f.user.stateData shouldEqual Matchmaking("user", true)
      }
    }

    "logged in" should {
      "have a name" in {
        f.user.stateData shouldBe a [Name]
      }
    }
  }
}
