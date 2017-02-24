name := """keep-clone"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.mindrot" % "jbcrypt" % "0.3m"
)

libraryDependencies += "org.scalaj" % "scalaj-http_2.11" % "2.3.0"

// https://mvnrepository.com/artifact/com.google.firebase/firebase-admin
libraryDependencies += "com.google.firebase" % "firebase-admin" % "4.1.1"

// https://mvnrepository.com/artifact/com.firebase/firebase-client
libraryDependencies += "com.firebase" % "firebase-client" % "2.2.4"

resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

fork in run := true

fork in run := true

fork in run := true

fork in run := true