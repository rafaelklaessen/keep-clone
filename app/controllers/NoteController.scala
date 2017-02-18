package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import play.api.libs.json._

import models.Notes

@Singleton
class NoteController @Inject() extends Controller {
  // Creates a note via Notes.createNote and sends a JSON repsonse
  def createNote = Action { request =>
    val requestContent = request.body.asFormUrlEncoded.get

    val reqOwner = request.session.get("username")
    
    if (reqOwner.isEmpty) {
      Unauthorized("Not logged in")
    } else if (!requestContent.contains("id")) {
      BadRequest("No ID given")
    } else {
      val reqId = requestContent("id").toArray

      try {
        val id = reqId(0).toLong

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

        println(title)
        println(content)
        println(color)

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
          Ok("kees")
        }

      } catch {
        case nfe: NumberFormatException => BadRequest("ID incorrect")
      }
    }
  }
}