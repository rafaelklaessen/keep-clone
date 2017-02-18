package models

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import play.api.libs.json._

import scala.io.Source

/**
 * This object is for all Notes related stuff, like adding and deleting notes.
 */
object Notes {

  // This method gets a note from Firebase by its id
  def getNote(id: Long): Map[String, Array[String]] = {
    val credential = "IhfqZxphYqBqLgi0cUX18n8qvYY46dgmNMO3sZG8"
    val noteJsonUrl = "https://keep-clone-840b5.firebaseio.com/keep-clone/notes/" + id.toString + ".json?auth=" + credential
  
    // Get note JSON and parse it 
    val note = Json.parse(Source.fromURL(noteJsonUrl).mkString)

    val title = if ((note \ "title").isInstanceOf[JsUndefined]) "null" else (note \ "title").as[String]
    val content = if ((note \ "content").isInstanceOf[JsUndefined]) "null" else (note \ "content").as[String]
    val color = if ((note \ "color").isInstanceOf[JsUndefined]) "null" else (note \ "color").as[String]
    val owners = if ((note \ "color").isInstanceOf[JsUndefined]) Array("null") else (note \ "owners").as[JsObject].keys.toArray
    
    // Put the note data in a map and return it
    val noteData = Map[String, Array[String]](
      "id" -> Array(id.toString),
      "title" -> Array(title),
      "content" -> Array(content),
      "color" -> Array(color),
      "owners" -> owners
    )

    noteData
  }

  /**
   * The getNotesByUsername method gets the notes field from a user, which
   * contains the ids of the user's notes.
   * It loops through those note ids and gets the corresponding notes.
   * The notes that it's got are returned
   */
  def getNotesByUsername(username: String): Array[Map[String, Array[String]]] = {
    val user = Users.getUser(username)
    val userNotes = user(1).asInstanceOf[Array[String]]

    if (userNotes(0) == "null") {
      Array()
    } else { 
      val noteIds = for (i <- userNotes) yield i.replaceAll("[note-]", "").toLong
      val sortedNoteIds = noteIds.sortWith(_ < _)
      val notes = for (i <- sortedNoteIds) yield getNote(i)

      notes
    }
  }

  // Creates note in Firebase
  def createNote(owner: String, id: Long, title: String, content: String, color: String) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val notesRef = ref.child("notes")
    val currentNote = notesRef.child(id.toString)

    currentNote.child("title").setValue(title)
    currentNote.child("content").setValue(content)
    currentNote.child("color").setValue(color)
    currentNote.child("owners").child(owner).setValue(true)

    val usersRef = ref.child("users")
    val currentUser = usersRef.child(owner)

    currentUser.child("notes").child("note-" + id.toString).setValue(true)
  }

  // Deletes note in Firebase based on its id
  def deleteNote(id: Long) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val notesRef = ref.child("notes")
    val currentNote = notesRef.child(id.toString)

    currentNote.removeValue()
  }

  // Deletes note from user
  def deleteNoteFromUser(id: Long, username: String) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val usersRef = ref.child("users")
    val currentUser = usersRef.child(username)

    currentUser.child("notes").child("note-" + id.toString).removeValue()
  }

  // Gets the ID for the new note by adding one the the last note's ID
  def getId(): Long = {
    val credential = "IhfqZxphYqBqLgi0cUX18n8qvYY46dgmNMO3sZG8"
    val lastNoteJsonUrl = "https://keep-clone-840b5.firebaseio.com/keep-clone/notes.json/?orderBy=\"$key\"&limitToLast=1&auth=" + credential

    val lastNote = Json.parse(Source.fromURL(lastNoteJsonUrl).mkString)
    val lastNoteKey = lastNote.as[JsObject].keys.head.toLong
    
    val newKey = lastNoteKey + 1
    
    newKey
  }
}