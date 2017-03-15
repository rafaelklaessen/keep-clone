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
  private val credential = "IhfqZxphYqBqLgi0cUX18n8qvYY46dgmNMO3sZG8"
  private val firebaseUrl = "https://keep-clone-840b5.firebaseio.com/keep-clone"

  // This method gets a note from Firebase by its id
  def getNote(id: Long): Note = {
    val noteJsonUrl = firebaseUrl + "/notes/" + id.toString + ".json?auth=" + credential

    // Get note JSON and parse it
    val note = parse(Source.fromURL(noteJsonUrl).mkString).extract[Note]
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
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val notesRef = ref.child("notes")
    val currentNote = notesRef.child(id.toString)

    currentNote.child("title").setValue(title)
    currentNote.child("content").setValue(content)
    currentNote.child("color").setValue(color)
    currentNote.child("owners").child(owner).setValue(true)
    if (pinned) currentNote.child("pinned").setValue(true)

    val usersRef = ref.child("users")
    val currentUser = usersRef.child(owner)

    currentUser.child("notes").child("note-" + id.toString).setValue(true)
  }

  // Deletes note in Firebase based on its id
  def deleteNote(id: Long) = {
    // Get old note
    val note = Notes.getNote(id)

    // Delete note from Firebase
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val notesRef = ref.child("notes")
    val currentNote = notesRef.child(id.toString)

    currentNote.removeValue()

    // Loop through all note owners and delete the note from them
    for (owner <- note.owners.keys) {
      Notes.deleteNoteFromUser(id, owner)
    }
  }

  // Deletes note from user
  def deleteNoteFromUser(id: Long, username: String) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val usersRef = ref.child("users")
    val currentUser = usersRef.child(username)

    currentUser.child("notes").child("note-" + id.toString).removeValue()
  }

  // Checks if note exists
  def noteExists(id: Long): Boolean = {
    val noteJsonUrl = firebaseUrl + "/notes/" + id.toString + ".json?auth=" + credential

    // Get note JSON
    val note = Source.fromURL(noteJsonUrl).mkString

    // If it's "null", the note doesn't exist
    note != "null"
  }

  // Pins/unpins a note
  def setPinned(id: Long, pinned: Boolean) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val notesRef = ref.child("notes")
    val currentNote = notesRef.child(id.toString)

    if (pinned) {
      currentNote.child("pinned").setValue(true)
    } else {
      currentNote.child("pinned").removeValue()
    }
  }

  // Archives/unarchives a note
  def setArchived(id: Long, archived: Boolean) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val notesRef = ref.child("notes")
    val currentNote = notesRef.child(id.toString)

    if (archived) {
      currentNote.child("archived").setValue(true)
    } else {
      currentNote.child("archived").removeValue()
    }
  }

  // Gets the ID for the new note by adding one the the last note's ID
  def getId: Long = {
    val lastNoteJsonUrl = firebaseUrl + "/notes.json/?orderBy=\"$key\"&limitToLast=1&auth=" + credential

    val lastNote = Json.parse(Source.fromURL(lastNoteJsonUrl).mkString)
    val lastNoteKey = lastNote.as[JsObject].keys.head.toLong

    val newKey = lastNoteKey + 1

    newKey
  }
}
