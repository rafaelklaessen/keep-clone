package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject() extends Controller {

  def login = Action {
    Ok(views.html.login()).withSession(
      "user_email" -> "test@example.com")
  }

  def register = Action {
    Ok(views.html.register())
  }

  def logout = Action {
    Ok(Json.parse("""
    {
      "json": true
    }
    """)).withNewSession
  }

}
