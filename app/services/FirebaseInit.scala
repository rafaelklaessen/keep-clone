package services

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import javax.inject._

import java.io.File
import java.io.FileInputStream

class FirebaseInit @Inject() (configuration: play.api.Configuration) {
  val apps = FirebaseApp.getApps()

  if (apps.isEmpty()) {
    val serviceAccount = new FileInputStream(configuration.underlying.getString("firebaseAuthPath"))

    val options = new FirebaseOptions.Builder()
      .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
      .setDatabaseUrl("https://keep-clone-840b5.firebaseio.com/")
      .build()

    FirebaseApp.initializeApp(options)
  }
}
