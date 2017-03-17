package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.Results

import scala.collection.mutable.ListBuffer

import models.Users

object RequestUtils {
  def requestBodyErrors(request: Request[AnyContent], bodyContent: List[String] = List()): Option[String] = {
    if (request.body.asFormUrlEncoded.isEmpty) {
      Some("Request body missing")
    } else {
      var result: Option[String] = None

      for (bodyField <- bodyContent.reverse) {
        if (!request.body.asFormUrlEncoded.get.contains(bodyField)) {
          result = Some("Missing field: " + bodyField)
        }
      }

      result
    }
  }

  def userSessionErrors(request: Request[AnyContent]): Option[String] = {
    if (request.session.get("username").isEmpty) {
      Some("Not logged in")
    } else {
      if (!Users.userExists(request.session.get("username").get)) {
        Some("Not logged in as existing user")
      } else {
        None
      }
    }
  }
}
