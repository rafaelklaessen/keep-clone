package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

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
      val id = Notes.getId()

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
        Ok("success")
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

        Notes.deleteNote(id)
        Notes.deleteNoteFromUser(id, reqOwner.get)

        Ok("success")
      } catch {
        case nfe: NumberFormatException => BadRequest("Incorrect ID")
      }
    }
  }
}