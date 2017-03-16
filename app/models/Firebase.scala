package models

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import scala.io.Source

/**
 * This object is for Firebase shorthands
 */
object Firebase {
  val credential = "IhfqZxphYqBqLgi0cUX18n8qvYY46dgmNMO3sZG8"
  val firebaseUrl = "https://keep-clone-840b5.firebaseio.com/keep-clone"

  val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
  val usersRef = ref.child("users")
  val notesRef = ref.child("notes")

  def getUrl(url: String) = firebaseUrl + url + ".json?auth=" + credential
  def getJson(url: String) = Source.fromURL(getUrl(url)).mkString
}
