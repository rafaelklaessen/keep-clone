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
import scalaj.http.Http

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
      
      // Create user in Firebase with its sub set to true. 
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

  def github(code: String) = Action { request => 
    // Perform POST request to GitHub
    val response = Http("https://github.com/login/oauth/access_token")
      .postForm(Seq(
        "code" -> code,
        "client_id" -> "a7a2de238eb384ce3d08",
        "client_secret" -> "client_secret"
      )).asString
    
    // Get response and extract accessToken from it
    val responseAccessToken = response.body.split("&")(0)
    val accessToken = responseAccessToken.substring(responseAccessToken.indexOf("=") + 1, responseAccessToken.length)

    // Get user JSON with access token
    val userJsonUrl = "https://api.github.com/user?access_token=" + accessToken
    val user = Json.parse(Source.fromURL(userJsonUrl).mkString)
    
    // The ID functions as the username
    val username = (user \ "id").as[JsNumber].toString

    println(username)

    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val usersRef = ref.child("users")
    val currentUser = usersRef.child(username)

    // Create user in Firebase with its ID set to true.
    // This way, the user exists verification still works.
    currentUser.child(username).setValue(true)

    Redirect("/").withSession(
      "username" -> username,
      "email" -> (user \ "email").as[String],
      "oauth" -> "GitHub")
  }

  def facebook = Action { request => 
    Ok("facebook")
  }
}