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

import org.mindrot.jbcrypt._

import models.Users

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

  def register = Action {
    Ok(views.html.register())
  }

  def registerUser() = Action { implicit request => 
    println(Users.getUser("nope"))

    println(Users.userExists("AUTHTEST"))

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
        if (!Users.userExists(userData.username)) {
          Users.registerUser(userData.username, userData.email, userData.firstName, userData.lastName, userData.password)

          Redirect("/").withSession(
            "username" -> userData.username)
        } else {
          Ok("User already exists!")
        }
      }
    )

    userData
  }

  def logout = Action {
    Ok(views.html.logout()).withNewSession
  }

}
