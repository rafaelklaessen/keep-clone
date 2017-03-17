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

import play.api.data.validation._

import models.Users
import models.User
import models.Notes

case class UserData(username: String, email: String, firstName: String, lastName: String, password: String)
case class LoginData(username: String, password: String)

/**
 * This controller handles user registration and logging in
 */
@Singleton
class UserController @Inject() extends Controller {

  def usernameConstraint(username: String): ValidationResult = {
    if (username.length == 0) {
      Invalid("This field is required")
    } else if (username.contains(".")) {
      Invalid("Username may not contain dots")
    } else {
      Valid
    }
  }

  val validUsername = Constraint("username")(usernameConstraint)

  // Form for the login page
  val loginForm = Form(
    mapping(
      "username" -> text.verifying(validUsername),
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  // Render login page with login form on /login
  def login = Action { implicit request =>
    request.session.get("username").map { username =>
      Redirect("/")
    }.getOrElse {
      Ok(views.html.user.login(loginForm))
    }
  }

  /**
   * This action handles post requests to /login. It logs users in when
   * all form fields are filled, the user exists and the password is correct.
   * Otherwise, it will show an error/errors.
   */
  def loginUser = Action { implicit request =>
    val loginData = loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.user.login(formWithErrors))
      },
      loginData => {
        if (Users.userExists(loginData.username)) {
          val user = Users.getUser(loginData.username)

          if (BCrypt.checkpw(loginData.password, user.password)) {
            // Log user in and redirect to homepage
            Redirect("/").withSession(
              "username" -> loginData.username)
          } else {
            Ok(views.html.user.wrongpassword(loginForm))
          }
        } else {
          Ok(views.html.user.usernotfound(loginForm))
        }
      }
    )

    loginData
  }

  def nameConstraint(name: String): ValidationResult = {
    if (name.length == 0) {
      Invalid("This field is required")
    } else if (name.length > 2 && name.length < 100) {
      Valid
    } else {
      Invalid("Name to short")
    }
  }

  val validName = Constraint("name")(nameConstraint)

  def passwordConstraint(name: String): ValidationResult = {
    if (name.length == 0) {
      Invalid("This field is required")
    } else if (name.length > 2 && name.length < 100) {
      Valid
    } else {
      Invalid("Password too weak")
    }
  }

  val validPassword = Constraint("password")(passwordConstraint)

  // Form for the register page
  val userForm = Form(
    mapping(
      "username" -> text.verifying(validUsername),
      "email" -> email,
      "firstName" -> text.verifying(validName),
      "lastName" -> text.verifying(validName),
      "password" -> text.verifying(validPassword)
    )(UserData.apply)(UserData.unapply)
  )

  // Render register page with register form on /register
  def register = Action { implicit request =>
    request.session.get("username").map { username =>
      Redirect("/")
    }.getOrElse {
      Ok(views.html.user.register(userForm))
    }
  }

  /**
   * This action handles post requests to /register. It registers users when
   * all form fields are filled and the username is not yet taken.
   * After a user successfully registers, he/she is redirected to the homepage.
   */
  def registerUser() = Action { implicit request =>
    val userData = userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.user.register(formWithErrors))
      },
      userData => {
        if (!Users.userExists(userData.username)) {
          val hashedPassword = BCrypt.hashpw(userData.password, BCrypt.gensalt())
          val user = User(userData.email, userData.firstName, userData.lastName, hashedPassword)

          Users.registerUser(userData.username, user)

          // After the user is registered, login as well and redirect to
          // homepage.
          Redirect("/").withSession(
            "username" -> userData.username)
        } else {
          Ok(views.html.user.userexists(userForm))
        }
      }
    )

    userData
  }

  // When a user goes to /logout, the user session is ended and a successfully
  // logged out page will show.
  def logout = Action {
    Ok(views.html.user.logout()).withNewSession
  }

}
