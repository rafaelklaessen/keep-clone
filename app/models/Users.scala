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
  def initializeFireBaseApp() = {
    val serviceAccount = new FileInputStream("../keep-clone-840b5-firebase-adminsdk-ztnub-40397c0ba3.json")

    val options = new FirebaseOptions.Builder()
      .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
      .setDatabaseUrl("https://keep-clone-840b5.firebaseio.com/")
      .build()
    
    FirebaseApp.initializeApp(options)
  }

  // This method gets the user via a request to Firebase's REST 
  def getUser(username: String):Map[String, String] = {
    val credential = "IhfqZxphYqBqLgi0cUX18n8qvYY46dgmNMO3sZG8"
    val baseUrl = "https://keep-clone-840b5.firebaseio.com/keep-clone/users/" + username + "/"
    
    // Get URLs
    var email = baseUrl + "email.json?auth=" + credential
    var firstName = baseUrl + "firstName.json?auth=" + credential
    var lastName = baseUrl + "lastName.json?auth=" + credential
    var password = baseUrl + "password.json?auth=" + credential

    // Perform request to URLs and get data
    email = scala.io.Source.fromURL(email).mkString
    firstName = scala.io.Source.fromURL(firstName).mkString
    lastName = scala.io.Source.fromURL(lastName).mkString
    password = scala.io.Source.fromURL(password).mkString

    // All data is quoted in "", so remove those quotes (as we don't want hem)
    email = email.slice(1, email.size - 1)
    firstName = firstName.slice(1, firstName.size - 1)
    lastName = lastName.slice(1, lastName.size - 1)
    password = password.slice(1, password.size - 1)

    // Put the data in a map and return it
    val userData = Map(
      "email" -> email,
      "firstName" -> firstName,
      "lastName" -> lastName,
      "password" -> password
    )

    userData
  }

  /**
   * This method checks if a user exists by getting the user and checking its
   * data. If a field contains ul, this means that it was null before, which 
   * means that the field wasn't set. If that's the case, the user doesn't 
   * exist.
   */ 
  def userExists(username: String) = {
    val user = getUser(username)

    user("email") != "ul" && user("firstName") != "ul" && user("lastName") != "ul" && user("password") != "ul"
  }

  // Registers user by putting the user's data in Firebase
  def registerUser(username: String, email: String, firstName: String, lastName: String, password: String) = {
    val apps = FirebaseApp.getApps()

    if (apps.isEmpty()) {
      Users.initializeFireBaseApp()
    }

    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val usersRef = ref.child("users")
    val currentUser = usersRef.child(username)

    currentUser.child("email").setValue(email)
    currentUser.child("firstName").setValue(firstName)
    currentUser.child("lastName").setValue(lastName)
    currentUser.child("password").setValue(BCrypt.hashpw(password, BCrypt.gensalt()))
  }
}