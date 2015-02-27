import akka.actor.ActorSystem
import akka.testkit._
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

class BotRpgSpec(_system: ActorSystem) extends TestKit(_system)
    with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this(name: String) = this(ActorSystem(name))

  override def afterAll = TestKit.shutdownActorSystem(system)
}
