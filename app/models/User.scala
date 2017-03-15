package models

/**
 * This case class is used to store users. All users are handled via this class.
 */
case class User(email: String, firstName: String, lastName: String, password: String, notes: Map[String, Boolean] = Map()) {
  // For Firebase
  def getEmail = email
  def getFirstName = firstName
  def getLastName = lastName
  def getPassword = password
}
