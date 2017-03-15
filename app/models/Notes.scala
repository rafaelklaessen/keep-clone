package models

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import play.api.libs.json._

import org.json4s._
import org.json4s.native.JsonMethods._

import scala.io.Source

/**
 * This object is for all Notes related stuff, like adding and deleting notes.
 */
object Notes {
  implicit val formats = DefaultFormats

  // This method gets a note from Firebase by its id
  def getNote(id: Long): Note = {
    // Get note JSON and parse it
    val note = parse(Firebase.getJson("/notes/" + id.toString)).extract[Note]
    // Add note ID
    note.id = id

    note
  }

  /**
   * The getNotesByUsername method gets the notes field from a user, which
   * contains the ids of the user's notes.
   * It loops through those note ids and gets the corresponding notes.
   * The notes that it's got are returned
   */
  def getNotesByUsername(username: String): Array[Note] = {
    // If the user doesn't exist, we're logged in after register and the user
    // isn't put in Firebase yet. That user won't have any notes, so return an
    // empty array.
    if (Users.userExists(username)) {
      val user = Users.getUser(username)

      if (user.notes.keys.size == 0) {
        Array()
      } else {
        val noteIds = for (i <- user.notes.keys.toArray) yield i.replaceAll("[note-]", "").toLong
        val sortedNoteIds = noteIds.sortWith(_ < _)
        val notes = for (i <- sortedNoteIds) yield getNote(i)
        notes
      }
    } else {
      Array()
    }
  }

  // Creates note in Firebase
  def createNote(owner: String, id: Long, title: String, content: String, color: String, pinned: Boolean = false) = {
    val currentNote = Firebase.notesRef.child(id.toString)

    currentNote.child("title").setValue(title)
    currentNote.child("content").setValue(content)
    currentNote.child("color").setValue(color)
    currentNote.child("owners").child(owner).setValue(true)
    if (pinned) currentNote.child("pinned").setValue(true)

    val currentUser = Firebase.usersRef.child(owner)

    currentUser.child("notes/note-" + id.toString).setValue(true)
  }

  // Deletes note in Firebase based on its id
  def deleteNote(id: Long) = {
    // Get old note
    val note = Notes.getNote(id)

    // Delete note from Firebase
    val currentNote = Firebase.notesRef.child(id.toString)

    currentNote.removeValue()

    // Loop through all note owners and delete the note from them
    for (owner <- note.owners.keys) {
      Notes.deleteNoteFromUser(id, owner)
    }
  }

  // Deletes note from user
  def deleteNoteFromUser(id: Long, username: String) = Firebase.usersRef.child(username + "/notes/note-" + id.toString).removeValue()

  // Checks if note exists
  def noteExists(id: Long): Boolean = Firebase.getJson("/notes/" + id.toString) != "null"

  // Pins/unpins a note
  def setPinned(id: Long, pinned: Boolean) = {
    val notePinned = Firebase.notesRef.child(id.toString).child("pinned")

    if (pinned) notePinned.setValue(true) else notePinned.removeValue()
  }

  // Archives/unarchives a note
  def setArchived(id: Long, archived: Boolean) = {
    val noteArchived = Firebase.notesRef.child(id.toString).child("archived")

    if (archived) noteArchived.setValue(true) else noteArchived.removeValue()
  }

  // Gets the ID for the new note by adding one the the last note's ID
  def getId: Long = {
    val lastNoteJsonUrl = Firebase.firebaseUrl + "/notes.json/?orderBy=\"$key\"&limitToLast=1&auth=" + Firebase.credential

    val lastNote = Json.parse(Source.fromURL(lastNoteJsonUrl).mkString)
    val lastNoteKey = lastNote.as[JsObject].keys.head.toLong

    val newKey = lastNoteKey + 1

    newKey
  }
}
