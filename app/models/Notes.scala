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

  def getNote(id: Int): Map[String, Array[String]] = {
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
      "title" -> Array(title),
      "content" -> Array(content),
      "color" -> Array(color),
      "owners" -> owners
    )

    noteData
  }

  def createNote(owner: String, id: Int, title: String, content: String, color: String) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val notesRef = ref.child("notes")
    val currentNote = notesRef.child(id.toString)

    currentNote.child("title").setValue(title)
    currentNote.child("content").setValue(content)
    currentNote.child("color").setValue(color)
    currentNote.child("owners").child(owner).setValue(true)
  }

  def deleteNote(id: Int) = {
    val ref = FirebaseDatabase.getInstance().getReference("keep-clone")
    val notesRef = ref.child("notes")
    val currentNote = notesRef.child(id.toString)

    currentNote.removeValue()
  }

  /** 
  val keys = user.as[JsObject].keys

    keys.foreach(x => println(x))*/
}