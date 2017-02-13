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

import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import org.mindrot.jbcrypt._

import models.Users

case class UserData(username: String, email: String, firstName: String, lastName: String, password: String)

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject() extends Controller {

  def login = Action {
    Ok(views.html.login())
  }

  def loginUser = Action { implicit request =>
    case class LoginData(username: String, password: String)

    val loginForm = Form(
      mapping(
        "username" -> nonEmptyText,
        "password" -> nonEmptyText
      )(LoginData.apply)(LoginData.unapply)
    )

    val loginData = loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest("Please enter all form fields")
      },
      loginData => {
        if (Users.userExists(loginData.username)) {
          val user = Users.getUser(loginData.username)

          println(BCrypt.checkpw(loginData.password, user("password")))

          if (BCrypt.checkpw(loginData.password, user("password"))) {
            Redirect("/").withSession(
              "username" -> loginData.username)
          } else {
            Ok("Wrong password")
          }
        } else {
          Ok("User not found")
        }
      }
    )

    loginData
  }

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "email" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserData.apply)(UserData.unapply)
  )

  def register = Action { implicit request =>
    Ok(views.html.register(userForm))
  }

  def registerUser() = Action { implicit request => 
    println(Users.getUser("nope"))

    println(Users.userExists("AUTHTEST"))

    val userData = userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.register(formWithErrors))
      },
      userData => {
        if (!Users.userExists(userData.username)) {
          Users.registerUser(userData.username, userData.email, userData.firstName, userData.lastName, userData.password)

          Redirect("/").withSession(
            "username" -> userData.username)
        } else {
          Ok(views.html.userexists(userForm))
        }
      }
    )

    userData
  }

  def logout = Action {
    Ok(views.html.logout()).withNewSession
  }

}
