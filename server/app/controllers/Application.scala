package controllers

import javax.inject.Inject
import play.api._
import play.api.cache.Cached
import play.api.mvc._
import play.api.Play.current

class Application @Inject()(cached: Cached) extends Controller {
  def serve(ignored: String = "") = cached("webpage") {
    Action {
      Ok(views.html.index())
    }
  }
}
