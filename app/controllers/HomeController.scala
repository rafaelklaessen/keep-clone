package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import models.Notes

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
      val notes = Notes.getNotesByUsername(username)

      Ok(views.html.home.notes(username, notes))
    }.getOrElse {
      Ok(views.html.home.index())
    }
  }

}
