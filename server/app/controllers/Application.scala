package controllers

import play.api._
import play.api.cache.Cached
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {
  def serve(ignored: String = "") = Cached("webpage") {
    Action {
      Ok(views.html.index())
    }
  }
}
