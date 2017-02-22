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

  def facebook(code: String) = Action { request => 
    // Get app ID and app secret    
    val appId = "847737378702820"
    val appSecret = "app_secret"

    // Get access token
    val accessTokenJsonUrl = "https://graph.facebook.com/v2.8/oauth/access_token?client_id=" + appId + "&redirect_uri=http%3A%2F%2Flocalhost:9000/oauth/facebook&client_secret=" + appSecret + "&code=" + code
    val accessTokenJson = Json.parse(Source.fromURL(accessTokenJsonUrl).mkString)
    val accessToken = (accessTokenJson \ "access_token").as[String]

    // Verify that the access token actually is for our app
    val debugTokenJsonUrl = "https://graph.facebook.com/debug_token?input_token=" + accessToken + "&access_token=" + appId + "|" + appSecret
    val debugTokenJson = Json.parse(Source.fromURL(debugTokenJsonUrl).mkString)
    val debugTokenData = (debugTokenJson \ "data")
    
    // If we're having a bad access token, reject. Otherwise,
    // log the user in.
    if ((debugTokenData \ "app_id").as[String] != appId) {
      BadRequest("Invalid access token")
    } else {
      // Get user email
      val userEmailJsonUrl = "https://graph.facebook.com/me?fields=email&access_token=" + accessToken 
      val userEmailJson = Json.parse(Source.fromURL(userEmailJsonUrl).mkString)

      // The ID functions as the username
      val username = (debugTokenData \ "user_id").as[String]

      val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
      val usersRef = ref.child("users")
      val currentUser = usersRef.child(username)

      // Create user in Firebase with its ID set to true.
      // This way, the user exists verification still works.
      currentUser.child(username).setValue(true)

      Redirect("/").withSession(
        "username" -> username,
        "email" -> (userEmailJson \ "email").as[String],
        "oauth" -> "Facebook")
    }
  }
}