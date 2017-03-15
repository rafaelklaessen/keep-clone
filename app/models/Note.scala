package models

/**
 * This class is used to store notes. All notes are handled via this class.
 */
case class Note(var id: Long = 0, title: String, content: String, color: String, owners: Array[String] = Array(), pinned: Boolean = false, archived: Boolean = false) {}
