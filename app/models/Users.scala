package models

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import java.io.File
import java.io.FileInputStream

import org.mindrot.jbcrypt._

import play.api.libs.json._

import org.json4s._
import org.json4s.native.JsonMethods._

import scala.io.Source

/**
 * This object is for all user related stuff, like registering, getting users
 * and checking if a user exists.
 */
object Users {
  implicit val formats = DefaultFormats
  private val credential = "IhfqZxphYqBqLgi0cUX18n8qvYY46dgmNMO3sZG8"
  private val firebaseUrl = "https://keep-clone-840b5.firebaseio.com/keep-clone"
  private val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
  private val usersRef = ref.child("users")

  // This method gets the user via a request to Firebase's REST
  def getUser(username: String): User = {
    val userJsonUrl = firebaseUrl + "/users/" + username + ".json?auth=" + credential

    // Get JSON and parse it
    parse(Source.fromURL(userJsonUrl).mkString).extract[User]
  }

  // Checks if user exists
  def userExists(username: String): Boolean = {
    val userJsonUrl = firebaseUrl + "/users/" + username + ".json?auth=" + credential

    // Get user JSON
    val user = Source.fromURL(userJsonUrl).mkString

    // If it's "null", the user doesn't exist
    user != "null"
  }

  // Registers user by putting the user's data in Firebase
  def registerUser(username: String, user: User) = usersRef.child(username).setValue(user)

  // Delete user
  def deleteUser(username: String) = usersRef.child(username).removeValue()

}
