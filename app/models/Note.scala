package models

/**
 * This case class is used to store notes. All notes are handled via this class.
 */
case class Note(var id: Long = 0, title: String, content: String, color: String, owners: Map[String, Boolean] = Map(), pinned: Boolean = false, archived: Boolean = false) {}
