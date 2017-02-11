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

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject() extends Controller {

  def login = Action {
    Ok(views.html.login()).withSession(
      "user_email" -> "test@example.com")
  }

  def register = Action {
    // Fetch the service account key JSON file contents
    val serviceAccount = new FileInputStream("../keep-clone-840b5-firebase-adminsdk-ztnub-40397c0ba3.json")

    // Initialize the app with a service account, granting admin privileges
    val options = new FirebaseOptions.Builder()
      .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
      .setDatabaseUrl("https://keep-clone-840b5.firebaseio.com/")
      .build()
    
    val apps = FirebaseApp.getApps()

    if (apps.isEmpty()) {
      FirebaseApp.initializeApp(options)
    }

    // As an admin, the app has access to read and write all data, regardless of Security Rules
    val ref = FirebaseDatabase.getInstance().getReference("restricted_access/secret_document")
    
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
      override def onDataChange(dataSnapshot: DataSnapshot) {
        val document = dataSnapshot.getValue()
        println(document)
      }

      def onCancelled(error: DatabaseError) { }
    })

    Ok(views.html.register())
  }

  def logout = Action {
    Ok(views.html.logout()).withNewSession
  }

}
