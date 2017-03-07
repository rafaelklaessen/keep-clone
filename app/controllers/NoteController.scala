package controllers

import com.google.firebase
import com.google.firebase._
import com.google.firebase.auth._
import com.google.firebase.database._

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import models.Notes
import models.Users

@Singleton
class NoteController @Inject() extends Controller {
  // Creates a note via Notes.createNote
  def createNote = Action { request =>
    val requestContent = request.body.asFormUrlEncoded.get

    val reqOwner = request.session.get("username")
    
    if (reqOwner.isEmpty) {
      Unauthorized("Not logged in")
    } else if (!Users.userExists(reqOwner.get)) {
      Unauthorized("Not logged in as existing user")
    } else {
      val id = Notes.getId

      val owner = reqOwner.get
        
      def isGiven(reqParam: String): Boolean = {
        if (requestContent.contains(reqParam)) {
          !(requestContent(reqParam).head.length == 0)
        } else {
          false
        }
      }

      val title = if (isGiven("title")) Some(requestContent("title").head) else None
      val content = if (isGiven("content")) Some(requestContent("content").head) else None
      val color = if (isGiven("color")) requestContent("color").head else "#FFFFFF"

      if (title.isEmpty && content.isEmpty) {
        BadRequest("No title and no content given")
      } else {
        val noteTitle = if (title.isEmpty) "" else title.get
        val noteContent = if (content.isEmpty) "" else content.get

        println("We'll send this:")
        println(owner)
        println(id)
        println(noteTitle)
        println(noteContent)
        println(color)
        Notes.createNote(owner, id, noteTitle, noteContent, color)
        Ok(id.toString)
      }
    }
  }

  // Deletes a note via Notes.deleteNote
  def deleteNote = Action { request =>
    val requestContent = request.body.asFormUrlEncoded.get

    val reqOwner = request.session.get("username")
    
    if (reqOwner.isEmpty) {
      Unauthorized("Not logged in")
    } else if (!Users.userExists(reqOwner.get)) {
      Unauthorized("Not logged in as existing user")
    } else if (!requestContent.contains("id")) {
      BadRequest("No ID given")
    } else {
      try {
        val id = requestContent("id").head.toLong

        // Make sure note exists
        if (Notes.noteExists(id)) {
          // Get note and make sure it actually belongs to the current user
          val note = Notes.getNote(id)

          if (note.owners.contains(reqOwner.get)) {
            Notes.deleteNote(id)
            Ok("success")
          } else {
            // Act as if note doesn't exist
            BadRequest("Note doesn't exist")
          }
        } else {
          BadRequest("Note doesn't exist")
        }
      } catch {
        case nfe: NumberFormatException => BadRequest("Incorrect ID")
      }
    }
  }

  // Updates a note
  def updateNote = Action { request => 
    val requestContent = request.body.asFormUrlEncoded.get
    val reqUser = request.session.get("username")

    if (reqUser.isEmpty) {
      Unauthorized("Not logged in")
    } else if (!Users.userExists(reqUser.get)) {
      Unauthorized("Not logged in as existing user")
    } else if (!requestContent.contains("id")) {
      BadRequest("No note ID provided")
    } else if (!requestContent.contains("fields")) {
      BadRequest("No fields provided")
    } else {
      // Get ID from request
      val id = requestContent("id").head.toString

      try {
        // Make sure note actually exists
        if (Notes.noteExists(id.toLong)) {
          val note = Notes.getNote(id.toLong)
          
          // Make sure note actually belongs to the current user.
          // If it doesn't, act as if it doesn't exist.
          if (note.owners.contains(reqUser.get)) {      
            // Get fields from request and parse them to a map
            val fields = Json.parse(requestContent("fields").head).as[Map[String, String]]

            // Make sure we're only setting valid fields
            val filteredFields = fields.filterKeys(_ match {
              case "title" | "content" | "color" => true
              case _ => false
            })

            // Get Firebase reference
            val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
            val notesRef = ref.child("notes")
            val currentNote = notesRef.child(id)

            // Set all Firebase fields
            filteredFields.keys.foreach(i => 
              currentNote.child(i).setValue(filteredFields(i))
            )

            Ok("success")
          } else {
            BadRequest("Note doesn't exist!")
          }
        } else {
          BadRequest("Note doesn't exist!")
        }
      } catch {
        case nfe: NumberFormatException => BadRequest("Invalid ID")
        case jpe: com.fasterxml.jackson.core.JsonParseException => BadRequest("Invalid fields")
        case e: Exception => BadRequest("Unknown parameter error")
      }
    }
  }

  // Adds owner to note
  def addNoteOwner = Action { request =>
    val requestContent = request.body.asFormUrlEncoded.get
    val reqUser = request.session.get("username")

    if (reqUser.isEmpty) {
      Unauthorized("Not logged in")
    } else if (!Users.userExists(reqUser.get)) {
      Unauthorized("Not logged in as existing user")
    } else if (!requestContent.contains("id")) {
      BadRequest("No note ID provided")
    } else if (!requestContent.contains("owner")) {
      BadRequest("No new owner provided")
    } else {
      // Get ID from request 
      val id = requestContent("id").head.toString
      // Get owner to add from request
      val owner = requestContent("owner").head

      try {
        // Make sure note and owner to add exist
        if (Notes.noteExists(id.toLong) && Users.userExists(owner)) {
          val note = Notes.getNote(id.toLong)

          // Make sure note actually belongs to the current user.
          // If it doesn't, act as if it doesn't exist.
          if (note.owners.contains(reqUser.get)) {
            // Get Firebase reference
            val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
            val notesRef = ref.child("notes")
            val usersRef = ref.child("users")
            val currentNote = notesRef.child(id)
            val currentUser = usersRef.child(owner)

            // Add new owner to note
            currentNote.child("owners").child(owner).setValue(true)
            
            // Add note to new owner
            currentUser.child("notes").child("note-" + id).setValue(true)

            Ok("success")
          } else {
            BadRequest("Note doesn't exist")
          }
        } else if (!Notes.noteExists(id.toLong)) {
          BadRequest("Note doesn't exist")
        } else {
          BadRequest("User to add doesn't exist")
        }
      } catch {
        case nfe: NumberFormatException => BadRequest("Invalid ID")
        case e: Exception => BadRequest("Unknown parameter error")
      }
    }
  }

  // Removes owner from note
  def removeNoteOwner = Action { request =>
    val requestContent = request.body.asFormUrlEncoded.get
    val reqUser = request.session.get("username")

    if (reqUser.isEmpty) {
      Unauthorized("Not logged in")
    } else if (!Users.userExists(reqUser.get)) {
      Unauthorized("Not logged in as existing user")
    } else if (!requestContent.contains("id")) {
      BadRequest("No note ID provided")
    } else if (!requestContent.contains("owner")) {
      BadRequest("No owner to delete provided")
    } else {
      // Get ID from request 
      val id = requestContent("id").head.toString
      // Get owner to add from request
      val owner = requestContent("owner").head

      try {
        // Make sure note and owner to add exist
        if (Notes.noteExists(id.toLong) && Users.userExists(owner)) {
          val note = Notes.getNote(id.toLong)

          // Make sure note actually belongs to the current user.
          // If it doesn't, act as if it doesn't exist.
          if (note.owners.contains(reqUser.get)) {
            // Get Firebase reference
            val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
            val notesRef = ref.child("notes")
            val usersRef = ref.child("users")
            val currentNote = notesRef.child(id)
            val currentUser = usersRef.child(owner)

            // Add new owner to note
            currentNote.child("owners").child(owner).removeValue()
            
            // Add note to new owner
            currentUser.child("notes").child("note-" + id).removeValue()

            Ok("success")
          } else {
            BadRequest("Note doesn't exist")
          }
        } else if (!Notes.noteExists(id.toLong)) {
          BadRequest("Note doesn't exist")
        } else {
          BadRequest("Owner to delete doesn't exist")
        }
      } catch {
        case nfe: NumberFormatException => BadRequest("Invalid ID")
        case e: Exception => BadRequest("Unknown parameter error")
      }
    }
  }

  // Pins or unpins note
  def setPinned = Action { request =>
    val requestContent = request.body.asFormUrlEncoded.get
    val reqUser = request.session.get("username")

    if (reqUser.isEmpty) {
      Unauthorized("Not logged in")
    } else if (!Users.userExists(reqUser.get)) {
      Unauthorized("Not logged in as existing user")
    } else if (!requestContent.contains("id")) {
      BadRequest("No note ID provided")
    } else if (!requestContent.contains("pinned")) {
      BadRequest("No pinned state provided")
    } else {
      // Get ID from request
      val id = requestContent("id").head.toString
      // Get pinnned state from request
      val pinned = requestContent("pinned").head

      try {
        // Make sure note exists
        if (Notes.noteExists(id.toLong)) {
          val note = Notes.getNote(id.toLong)

          // Make sure note actually beloongs to the current user.
          // If it doesn't, act as if it doesn't exist.
          if (note.owners.contains(reqUser.get)) {
            // Convert pinned string to boolean
            try {
              Notes.setPinned(id.toLong, pinned.toBoolean)
              Ok("success")
            } catch {
              case iae: IllegalArgumentException => BadRequest("Invalid pinned state")
              case e: Exception => BadRequest("Unknown parameter error")
            }
          } else {
            BadRequest("Note doesn't exist")
          }
        } else {
          BadRequest("Note doesn't exist")
        }
      } catch {
        case nfe: NumberFormatException => BadRequest("Invalid ID")
        case e: Exception => BadRequest("Unknown parameter error")
      }
    }
  }
}