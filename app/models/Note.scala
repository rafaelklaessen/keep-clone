package models

/**
 * This class is used to store notes. All notes are handled via this class.
 */
class Note(noteId: Long, noteTitle: String, noteContent: String, noteColor: String, noteOwners: Array[String], notePinned: Boolean, noteArchived: Boolean) {
  val id = noteId
  val title = noteTitle
  val content = noteContent
  val color = noteColor
  val owners = noteOwners
  val pinned = notePinned
  val archived = noteArchived
}