package botrpg.client.directive

import biz.enef.angular._
import biz.enef.angular.core._
import scala.scalajs.js

class AutoScrollBottomDirective($timeout: Timeout) extends Directive {

  override val restrict = "A"

  override val isolateScope = js.Dictionary("scrollToBottom" -> "@")

  override def postLink(
      scope: Scope,
      element: JQLite,
      attrs: Attributes,
      controller: js.Dynamic) = {
    attrs("scrollToBottom") foreach { elemName =>
      scope.$watchCollection(new js.Object(elemName), {
        (a: js.Object, b: js.Object, _: Scope) =>
          $timeout { () =>
            element.foreach { e => e.scrollTop = e.scrollHeight }
          }
          ()
      }: js.Function3[js.Object, js.Object, Scope, Unit])
    }
  }
}
