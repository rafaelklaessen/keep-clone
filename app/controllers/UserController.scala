package controllers

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import java.io.File
import java.io.FileInputStream

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._

import models.Users

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

  def registerUser() = Action { implicit request => 
    val serviceAccount = new FileInputStream("../keep-clone-840b5-firebase-adminsdk-ztnub-40397c0ba3.json")

    val options = new FirebaseOptions.Builder()
      .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
      .setDatabaseUrl("https://keep-clone-840b5.firebaseio.com/")
      .build()
    
    val apps = FirebaseApp.getApps()

    if (apps.isEmpty()) {
      FirebaseApp.initializeApp(options)
    }

    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")

    val usersRef = ref.child("users")

    println(Users.getUser("AUTHTEST"))

    case class UserData(username: String, email: String, firstName: String, lastName: String, password: String)

    val userForm = Form(
      mapping(
        "username" -> nonEmptyText,
        "email" -> nonEmptyText,
        "firstName" -> nonEmptyText,
        "lastName" -> nonEmptyText,
        "password" -> nonEmptyText
      )(UserData.apply)(UserData.unapply)
    )

    val userData = userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest("Form not filled correctly")
      },
      userData => {
        Users.registerUser(userData.username, userData.email, userData.firstName, userData.lastName, userData.password)

        Redirect("/")
      }
    )

    userData
  }

  def logout = Action {
    Ok(views.html.logout()).withNewSession
  }

}
