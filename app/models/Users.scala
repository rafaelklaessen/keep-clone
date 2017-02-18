package models

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import java.io.File
import java.io.FileInputStream

import org.mindrot.jbcrypt._

import play.api.libs.json._

import scala.io.Source

/**
 * This object is for all user related stuff, like registering, getting users
 * and checking if a user exists.
 */
object Users {

  // This method gets the user via a request to Firebase's REST 
  def getUser(username: String): Array[Any] = {
    val credential = "IhfqZxphYqBqLgi0cUX18n8qvYY46dgmNMO3sZG8"
    val userJsonUrl = "https://keep-clone-840b5.firebaseio.com/keep-clone/users/" + username + ".json?auth=" + credential

    // Get user JSON and parse it
    val user = Json.parse(Source.fromURL(userJsonUrl).mkString)

    // Extract fields from user JSON
    // If a field is JsUndefined we replace it by the string null
    val email = if ((user \ "email").isInstanceOf[JsUndefined]) "null" else (user \ "email").as[String] 
    val firstName = if ((user \ "firstName").isInstanceOf[JsUndefined]) "null" else (user \ "firstName").as[String]
    val lastName = if ((user \ "lastName").isInstanceOf[JsUndefined]) "null" else (user \ "lastName").as[String]
    val password = if ((user \ "password").isInstanceOf[JsUndefined]) "null" else (user \ "password").as[String]
    val notes = if ((user \ "notes").isInstanceOf[JsUndefined]) Array("null") else (user \ "notes").as[JsObject].keys.toArray

    // Put the data in a map and return it
    val userData = Map(
      "email" -> email,
      "firstName" -> firstName,
      "lastName" -> lastName,
      "password" -> password
    )

    val userNotes = notes

    Array(userData, userNotes)
  }

  /**
   * This method checks if a user exists by getting the user and checking its
   * data. If a field contains ul, this means that it was null before, which 
   * means that the field wasn't set. If that's the case, the user doesn't 
   * exist.
   */ 
  def userExists(username: String): Boolean = {
    val user = getUser(username)
    
    val userData = user(0).asInstanceOf[Map[String, String]]

    userData("email") != "null" && userData("firstName") != "null" && userData("lastName") != "null" && userData("password") != "null"
  }

  // Registers user by putting the user's data in Firebase
  def registerUser(username: String, email: String, firstName: String, lastName: String, password: String) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val usersRef = ref.child("users")
    val currentUser = usersRef.child(username)

    currentUser.child("email").setValue(email)
    currentUser.child("firstName").setValue(firstName)
    currentUser.child("lastName").setValue(lastName)
    currentUser.child("password").setValue(BCrypt.hashpw(password, BCrypt.gensalt()))
  }
}