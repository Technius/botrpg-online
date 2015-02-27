package botrpg.client.directive

import biz.enef.angular._
import biz.enef.angular.core._
import scala.scalajs.js

class AutoScrollBottomDirective($timeout: Timeout) extends Directive {
  override val restrict = "A"

  override def postLink(
      scope: Scope,
      element: JQLite,
      attrs: Attributes,
      controller: js.Dynamic) = {
    scope.$watchCollection(new js.Object(attrs("scrollToBottom").get), {
      (_: js.Object, _: js.Object, _: Scope) =>
        $timeout { () =>
          // TODO: figure it out
        }
        ()
    }: js.Function3[js.Object, js.Object, Scope, Unit])
  }
}
