package models

/**
 * This class is used to store users. All users are handled via this class.
 */
class User(userEmail: String, userFirstName: String, userLastName: String, userPassword: String, userNotes: Array[String] = Array()) {
  val email = userEmail
  val firstName = userFirstName
  val lastName = userLastName
  val password = userPassword
  val notes = userNotes

  // For Firebase
  def getEmail = email
  def getFirstName = firstName
  def getLastName = lastName
  def getPassword = password
}