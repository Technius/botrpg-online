package botrpg.client.directive

import biz.enef.angular._
import biz.enef.angular.core._
import scala.scalajs.js

class AutoScrollBottomDirective($timeout: Timeout) extends Directive {

  override val restrict = "A"

  override val isolateScope = js.Dictionary("scrollToBottom" -> "=")

  override def postLink(
      scope: Scope,
      element: JQLite,
      attrs: Attributes,
      controller: js.Dynamic) = {
    scope.$watch("scrollToBottom", { (a: js.Object, b: js.Object) =>
      $timeout { () =>
        element foreach { e =>
          e.scrollTop = e.scrollHeight
        }
      }
    })
  }
}
