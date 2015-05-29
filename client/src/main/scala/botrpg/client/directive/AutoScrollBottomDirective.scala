package botrpg.client.directive

import biz.enef.angulate._
import biz.enef.angulate.core._
import scala.scalajs.js

class AutoScrollBottomDirective($timeout: Timeout) extends Directive {

  override type ScopeType = Scope

  override val restrict = "A"

  override val isolateScope = js.Dictionary("scrollToBottom" -> "=")

  override def postLink(scope: Scope, element: JQLite, attrs: Attributes) = {
    scope.$watch("scrollToBottom", { (a: js.Object, b: js.Object) =>
      $timeout { () =>
        element.prop("scrollTop", element.prop("scrollHeight"))
      }
    })
  }
}
