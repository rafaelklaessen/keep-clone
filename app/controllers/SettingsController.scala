package controllers

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import javax.inject._
import play.api._
import play.api.mvc._

import play.api.libs.json._

import org.mindrot.jbcrypt._

import models.Users
import models.Notes
import models.Firebase

import scala.util.Try

import controllers.RequestUtils.userSessionErrors

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class SettingsController @Inject() extends Controller {

  // Renders the settings page, but only when you're logged in
  def show = Action { request =>
    request.session.get("username").map { username =>
      // If we're logged in with OAuth, render a different settings page
      request.session.get("oauth").map { oauth =>
        Ok(views.html.settings.settingsOAuth(oauth, request.session.get("email").get))
      }.getOrElse {
        val user = Users.getUser(username)
        Ok(views.html.settings.settings(username, user))
      }
    }.getOrElse {
      NotFound(views.html.error.notfound())
    }
  }

  // Update user settings
  def update = Action { request =>
    val requestContent = request.body.asFormUrlEncoded.get
    val reqUser = request.session.get("username")

    if (!userSessionErrors(request).isEmpty) {
      Unauthorized(userSessionErrors(request).get)
    } else if (!requestContent.contains("fields")) {
      BadRequest("No fields provided")
    } else {
      // Get fields from request and parse them to a map
      if (Try(Json.parse(requestContent("fields").head)).isSuccess) {
        val fields = Json.parse(requestContent("fields").head).as[Map[String, String]]

        // Make sure we're only setting valid fields
        val filteredFields = fields.filterKeys(_ match {
          case "email" | "firstName" | "lastName" | "password" => true
          case _ => false
        })

        // Transform required fields (which is only the password, as it has to
        // be hashed + salted)
        val finalFields = filteredFields.transform((k, v) => k match {
          case "password" => BCrypt.hashpw(v, BCrypt.gensalt())
          case k => v
        })

        // The regex used to determine whether an email is valid or not
        val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

        // Function used to validate if an email is valid, based on the regex
        def isValidEmail(e: String): Boolean = e match{
          case null => false
          case e if e.trim.isEmpty => false
          case e if emailRegex.findFirstMatchIn(e).isDefined => true
          case _ => false
        }

        // Check if the email is valid, but only if it's there
        val validEmail = if (finalFields.contains("email")) {
          isValidEmail(finalFields("email"))
        } else {
          true
        }

        // If our email is set, but not valid, give a badrequest.
        // Otherwise, put the data in Firebase.
        if (!validEmail) {
          BadRequest("Not a valid email")
        } else {
          // Get Firebase reference
          val currentUser = Firebase.usersRef.child(reqUser.get)

          // Set all Firebase fields
          finalFields.keys.foreach(i =>
            currentUser.child(i).setValue(finalFields(i))
          )

          Ok("success")
        }
      } else {
        BadRequest("Invalid fields")
      }
    }
  }

  // Account deletion
  def deleteAccount = Action { request =>
    val reqUser = request.session.get("username")

    if (!userSessionErrors(request).isEmpty) {
      Unauthorized(userSessionErrors(request).get)
    } else {
      // Get username and user
      val username = reqUser.get
      val user = Users.getUser(username)
      // Get the user's note
      val userNotes = Notes.getNotesByUsername(username)
      // Only delete notes that are only owned by the current user
      var notesToDelete = for (note <- userNotes if note.owners.keys.size == 1) yield note

      // Loop through the users's notes and delete them
      for (note <- notesToDelete) {
        Notes.deleteNote(note.id)
      }

      // Delete user
      Users.deleteUser(username)

      // Return success and logout
      Ok("success").withNewSession
    }
  }
}
