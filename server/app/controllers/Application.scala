package controllers

import play.api._
import play.api.cache.Cached
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {
  val scriptPath = if (Play.isProd(Play.current)) "app.js" else "app-dev.js"
  def serve(ignored: String = "") = Cached("webpage") {
    Action {
      Ok(views.html.index(scriptPath))
    }
  }
}
