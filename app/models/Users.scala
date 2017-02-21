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
  def getUser(username: String): User = {
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

    new User(email, firstName, lastName, password, notes)
  }

  /**
   * This method checks if a user exists by getting the user and checking its
   * data. If a field contains ul, this means that it was null before, which 
   * means that the field wasn't set. If that's the case, the user doesn't 
   * exist.
   */ 
  def userExists(username: String): Boolean = {
    val credential = "IhfqZxphYqBqLgi0cUX18n8qvYY46dgmNMO3sZG8"
    val userJsonUrl = "https://keep-clone-840b5.firebaseio.com/keep-clone/users/" + username + ".json?auth=" + credential

    // Get user JSON
    val user = Source.fromURL(userJsonUrl).mkString

    // If it's "null", the user doesn't exist
    user != "null"
  }

  // Registers user by putting the user's data in Firebase
  def registerUser(username: String, user: User) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val usersRef = ref.child("users")
    val currentUser = usersRef.child(username)

    currentUser.setValue(user)
  }

  // Delete user
  def deleteUser(username: String) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val usersRef = ref.child("users")
    val currentUser = usersRef.child(username)

    currentUser.removeValue()
  }

}