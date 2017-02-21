package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import play.api.libs.json._

import scala.io.Source

@Singleton
class OAuthController @Inject() extends Controller {
  def google = Action { request =>
    val requestContent = request.body.asFormUrlEncoded.get

    if (requestContent.contains("id_token")) {
      val id_token = requestContent("id_token").head
      val googleUser = Json.parse(Source.fromURL("https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + id_token).mkString).as[Map[String, String]]
      println(googleUser)
      
      // The sub functions as the username
      val username = googleUser("sub")

      val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
      val usersRef = ref.child("users")
      val currentUser = usersRef.child(username)
      
      // Create user in Firebase its sub set to true. 
      // This way, the user exists verification will still work. 
      currentUser.child(username).setValue(true)

      Ok("success").withSession(
        "username" -> username,
        "email" -> googleUser("email"),
        "oauth" -> "Google")  
    } else {
      BadRequest("No ID token provided")
    }
  }

  def github = Action { request => 
    Ok("github")
  }

  def twitter = Action { request => 
    Ok("twitter")
  }

  def facebook = Action { request => 
    Ok("facebook")
  }
}