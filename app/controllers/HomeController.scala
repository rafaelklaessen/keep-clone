package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /** 
   * Render the homepage when the user isn't logged in. Otherwise, render
   * notes page.
   */
  def index = Action { request =>
    request.session.get("username").map { username =>
      Ok(views.html.notes(username))
    }.getOrElse {
      Ok(views.html.index())
    }
  }

}
