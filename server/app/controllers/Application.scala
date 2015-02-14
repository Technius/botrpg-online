package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  val scriptPath = if (Play.isProd(Play.current)) "app.js" else "app-dev.js"
  def serve(ignored: String = "") = Action {
    Ok(views.html.index(scriptPath))
  }
}
