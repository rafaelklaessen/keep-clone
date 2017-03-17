package models

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

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

  def getUser(username: String) = parse(Firebase.getJson("/users/" + username)).extract[User]
  def userExists(username: String) = Firebase.getJson("/users/" + username) != "null"
  def registerUser(username: String, user: User) = Firebase.usersRef.child(username).setValue(user)
  def deleteUser(username: String) = Firebase.usersRef.child(username).removeValue()
}
