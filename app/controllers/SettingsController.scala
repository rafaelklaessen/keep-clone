package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class SettingsController @Inject() extends Controller {

  // Renders the settings page, but only when you're logged in
  def show = Action { request =>
    request.session.get("username").map { username => 
      Ok(views.html.settings())
    }.getOrElse {
      NotFound("Page not found")
    }
  }

}
