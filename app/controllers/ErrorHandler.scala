package controllers

import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._

@Singleton
class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
  ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    Future.successful(
      InternalServerError(views.html.error.internalservererror(exception.toString))
    )
  }

  override def onNotFound(request: RequestHeader, message: String) = {
    Future.successful(
      NotFound(views.html.error.notfound())
    )
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    if (statusCode == 404) {
      onNotFound(request, message)
    } else {
      Future.successful(
        BadRequest(views.html.error.clienterror(statusCode, message))
      )
    }
  }
}
