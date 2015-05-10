import actors._
import actors.game._
import actors.user._
import akka.actor._
import akka.testkit._
import botrpg.common._
import models._
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.reflectiveCalls
import actors.user.UserActor.Internal._

class UserSpec extends BotRpgSpec("UserSpec") {

  def fixture = new {
    val out = TestProbe()
    val matchmaker = TestProbe()
    val user = TestFSMRef(new UserActor(out.ref, matchmaker.ref))
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
        f.user.stateData shouldBe a [Matchmaking]
        f.user.stateData.asInstanceOf[Matchmaking].name shouldBe "user"
      }
    }

    "logged in" should {
      "have a name" in {
        f.user.stateData shouldBe a [Name]
      }
    }
  }
}
