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
import models.Firebase

import scala.collection.mutable.ListBuffer
import scala.util.Try

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
    val missing = validateRequest(request, List("id", "fields"))
    var result = Ok("default")

    if (!missing("session").isEmpty) {
      missing("session").foreach(sessionField => result = sessionField match {
        case "username" => Unauthorized("Not logged in")
      })
      result
    } else if (!missing("body").isEmpty) {
      missing("body").foreach(bodyField => result = bodyField match {
        case "id" => BadRequest("No note ID provided")
        case "fields" => BadRequest("No fields provided")
        case "body" => BadRequest("No request body was given")
      })
      result
    } else {
      val requestContent = request.body.asFormUrlEncoded.get
      val reqUser = request.session.get("username")

      // Get ID from request
      val id = requestContent("id").head.toString

      if (Try(id.toLong).isSuccess && Try(Json.parse(requestContent("fields").head).as[Map[String, String]]).isSuccess) {
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
            val currentNote = Firebase.notesRef.child(id)

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
      } else {
        BadRequest("Invalid ID or fields")
      }
    }
  }

  // Adds owner to note
  def addNoteOwner = Action { request =>
    setNoteOwner(request, true)
  }

  // Removes owner from note
  def removeNoteOwner = Action { request =>
    setNoteOwner(request, false)
  }

  // Sets or removes a note owner, a helper method for addNoteOwner and removeNoteOwner
  def setNoteOwner(request: Request[AnyContent], add: Boolean): Result = {
    val missing = validateRequest(request, List("id", "owner"))
    var result = Ok("default")

    if (!missing("session").isEmpty) {
      missing("session").foreach(sessionField => result = sessionField match {
        case "username" => Unauthorized("Not logged in")
      })
      result
    } else if (!missing("body").isEmpty) {
      missing("body").foreach(bodyField => result = bodyField match {
        case "id" => BadRequest("No note ID provided")
        case "owner" => BadRequest("No owner provided")
        case "body" => BadRequest("No request body was given")
      })
      result
    } else {
      val requestContent = request.body.asFormUrlEncoded.get
      val reqUser = request.session("username")

      // Get ID from request
      val id = requestContent("id").head.toString
      // Get owner to add from request
      val owner = requestContent("owner").head

      if (Try(id.toLong).isSuccess) {
        // Make sure note and owner to add exist
        if (Notes.noteExists(id.toLong) && Users.userExists(owner)) {
          val note = Notes.getNote(id.toLong)

          // Make sure note actually belongs to the current user.
          // If it doesn't, act as if it doesn't exist.
          if (note.owners.contains(reqUser)) {
            // Get Firebase reference
            val currentNote = Firebase.notesRef.child(id)
            val currentUser = Firebase.usersRef.child(owner)

            // Add or remove note based on the add parameter
            if (add) {
              // Add new owner to note
              currentNote.child("owners").child(owner).setValue(true)

              // Add note to new owner
              currentUser.child("notes").child("note-" + id).setValue(true)
            } else {
              // Remove owner from note
              currentNote.child("owners").child(owner).removeValue()

              // Remove owner from note
              currentUser.child("notes").child("note-" + id).removeValue()
            }

            Ok("success")
          } else {
            BadRequest("Note doesn't exist")
          }
        } else if (!Notes.noteExists(id.toLong)) {
          BadRequest("Note doesn't exist")
        } else {
          BadRequest("Owner to delete doesn't exist")
        }
      } else {
        BadRequest("Invalid ID")
      }
    }
  }

  // Pins or unpins note
  def setPinned = Action { request =>
    val missing = validateRequest(request, List("id", "pinned"))
    var result = Ok("default")

    if (!missing("session").isEmpty)  {
      missing("session").foreach(sessionField => result = sessionField match {
        case "username" => Unauthorized("Not logged in")
      })
      result
    } else if (!missing("body").isEmpty) {
      missing("body").foreach(bodyField => result = bodyField match {
        case "id" => BadRequest("No note ID provided")
        case "pinned" => BadRequest("No pinned state provided")
        case "body" => BadRequest("No request body was given")
      })
      result
    } else {
      val requestContent = request.body.asFormUrlEncoded.get
      val reqUser = request.session.get("username")
      // Get ID from request
      val id = requestContent("id").head.toString
      // Get pinnned state from request
      val pinned = requestContent("pinned").head

      if (Try(id.toLong).isSuccess && Try(pinned.toBoolean).isSuccess) {
        // Make sure note exists
        if (Notes.noteExists(id.toLong)) {
          val note = Notes.getNote(id.toLong)

          // Make sure note actually beloongs to the current user.
          // If it doesn't, act as if it doesn't exist.
          if (note.owners.contains(reqUser.get)) {
            Notes.setPinned(id.toLong, pinned.toBoolean)
            Ok("success")
          } else {
            BadRequest("Note doesn't exist")
          }
        } else {
          BadRequest("Note doesn't exist")
        }
      } else {
        BadRequest("Invalid ID or pinned state")
      }
    }
  }

  // Archives or unarchieves note
  def setArchived = Action { request =>
    val missing = validateRequest(request, List("id", "archived"))
    var result = Ok("default")

    if (!missing("session").isEmpty)  {
      missing("session").foreach(sessionField => result = sessionField match {
        case "username" => Unauthorized("Not logged in")
      })
      result
    } else if (!missing("body").isEmpty) {
      missing("body").foreach(bodyField => result = bodyField match {
        case "id" => BadRequest("No note ID provided")
        case "archived" => BadRequest("No archived state provided")
        case "body" => BadRequest("No request body was given")
      })
      result
    } else {
      val requestContent = request.body.asFormUrlEncoded.get
      val reqUser = request.session.get("username")
      // Get ID from request
      val id = requestContent("id").head.toString
      // Get pinnned state from request
      val archived = requestContent("archived").head

      if (Try(id.toLong).isSuccess && Try(archived.toBoolean).isSuccess) {
        // Make sure note exists
        if (Notes.noteExists(id.toLong)) {
          val note = Notes.getNote(id.toLong)

          // Make sure note actually beloongs to the current user.
          // If it doesn't, act as if it doesn't exist.
          if (note.owners.contains(reqUser.get)) {
            // Convert pinned string to boolean
            Notes.setArchived(id.toLong, archived.toBoolean)
            Ok("success")
          } else {
            BadRequest("Note doesn't exist")
          }
        } else {
          BadRequest("Note doesn't exist")
        }
      } else {
        BadRequest("Invalid ID or archived state")
      }
    }
  }

  def validateRequest(request: Request[AnyContent], bodyContent: List[String] = List(), sessionContent: List[String] = List("username")): Map[String, ListBuffer[String]] = {
    val bodyMissing = ListBuffer[String]()
    val sessionMissing = ListBuffer[String]()

    for (sessionField <- sessionContent) {
      if (request.session.get(sessionField).isEmpty) sessionMissing += sessionField
    }

    if (request.body.asFormUrlEncoded.isEmpty) {
      bodyMissing += "body"
    } else {
      val requestBody = request.body.asFormUrlEncoded.get

      for (bodyField <- bodyContent) {
        if (!requestBody.contains(bodyField)) bodyMissing += bodyField
      }
    }

    Map[String, ListBuffer[String]](
      "session" -> sessionMissing,
      "body" -> bodyMissing
    )
  }
}
