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

case class UserData(username: String, email: String, firstName: String, lastName: String, password: String)
case class LoginData(username: String, password: String)

/**
 * This controller handles user registration and logging in
 */
@Singleton
class UserController @Inject() extends Controller {

  def usernameConstraint(username: String): ValidationResult = {
    if (username.length == 0) {
      Invalid("Required")
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
    Ok(views.html.login(loginForm))
  }

  /**
   * This action handles post requests to /login. It logs users in when 
   * all form fields are filled, the user exists and the password is correct.
   * Otherwise, it will show an error/errors.
   */
  def loginUser = Action { implicit request =>
    val loginData = loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.login(formWithErrors))
      },
      loginData => {
        if (Users.userExists(loginData.username)) {
          val user = Users.getUser(loginData.username)

          if (BCrypt.checkpw(loginData.password, user("password"))) {
            // Log user in and redirect to homepage
            Redirect("/").withSession(
              "username" -> loginData.username)
          } else {
            Ok(views.html.wrongpassword(loginForm))
          }
        } else {
          Ok(views.html.usernotfound(loginForm))
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
    Ok(views.html.register(userForm))
  }

  /**
   * This action handles post requests to /register. It registers users when
   * all form fields are filled and the username is not yet taken. 
   * After a user successfully registers, he/she is redirected to the homepage.
   */
  def registerUser() = Action { implicit request => 
    val userData = userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.register(formWithErrors))
      },
      userData => {
        if (!Users.userExists(userData.username)) {
          Users.registerUser(userData.username, userData.email, userData.firstName, userData.lastName, userData.password)

          // After the user is registered, login as well and redirect to 
          // homepage.
          Redirect("/").withSession(
            "username" -> userData.username)
        } else {
          Ok(views.html.userexists(userForm))
        }
      }
    )

    userData
  }

  // When a user goes to /logout, the user session is ended and a successfully
  // logged out page will show.
  def logout = Action {
    Ok(views.html.logout()).withNewSession
  }

}
