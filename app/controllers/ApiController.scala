package controllers

import io.github.honeycombcheesecake.play.silhouette.api.Silhouette
import io.github.honeycombcheesecake.play.silhouette.api.exceptions.ConfigurationException
import io.github.honeycombcheesecake.play.silhouette.api.services.AuthenticatorService
import io.github.honeycombcheesecake.play.silhouette.api.util.Credentials
import io.github.honeycombcheesecake.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import io.github.honeycombcheesecake.play.silhouette.impl.providers.CredentialsProvider
import models.Exportable.ExportableOps
import models.{Memory, View}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import security.MitaEnv

import java.util.UUID
import javax.inject._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

@Singleton
class ApiController @Inject() (
    cc: ControllerComponents,
    silhouette: Silhouette[MitaEnv],
    authenticatorService: AuthenticatorService[MitaEnv#A],
    credentialsProvider: CredentialsProvider
) extends AbstractController(cc) {

  def push: Action[AnyContent] = silhouette.SecuredAction { request =>
    try {
      val body = request.body.asJson.get
      val viewName = (body \ "view").as[String]
      val data = (body \ "data").get.as[JsArray]
      val view = Memory.views.get(viewName) match {
        case Some(value) => value
        case None =>
          val newView = View(viewName)
          Memory.views.update(viewName, newView)
          newView
      }
      view.updateFromJson(data)
      Memory.needSave = true
      Ok
    } catch {
      case _: Throwable => BadRequest
    }
  }

  def listViews: Action[AnyContent] = silhouette.SecuredAction {
    Ok(Json.toJson(Memory.views.keys.toList))
  }

  def view(name: String): Action[AnyContent] = silhouette.SecuredAction {
    Memory.views.get(name) match {
      case Some(value) => Ok(value.exportJson)
      case None        => BadRequest
    }
  }

  def deleteView(viewName: String): Action[AnyContent] =
    silhouette.SecuredAction {
      Memory.views.remove(viewName)
      Memory.needSave = true
      Ok
    }

  def deleteComponent(
      viewName: String,
      componentName: String
  ): Action[AnyContent] = silhouette.SecuredAction {
    Memory.views.get(viewName) match {
      case Some(value) =>
        value.components.remove(componentName)
        Memory.needSave = true
        Ok
      case None => BadRequest
    }
  }

  def auth: Action[AnyContent] = silhouette.UnsecuredAction { implicit request =>
    Try {
      val body = request.body.asJson.get
      (body \ "password").as[String]
    } match {
      case Failure(_) => BadRequest
      case Success(password) =>
        val a = Credentials(UUID.randomUUID.toString, password)
        Try {
          Await.result(credentialsProvider.authenticate(a), Duration.Inf)
        } match {
          case Failure(e) => e match {
            case _@(_: InvalidPasswordException | _: ConfigurationException | _: IdentityNotFoundException) =>
              Unauthorized
            case _: Throwable => throw e
          }
          case Success(value) =>
            val authenticator = Await.result(authenticatorService.create(value), Duration.Inf)
            val token = Await.result(authenticatorService.init(authenticator), Duration.Inf)
            // Await.result(authenticatorService.embed(token, ), Duration.Inf)
            Ok(Json.obj("token" -> token))
        }
    }
  }

  def testAuth: Action[AnyContent] = silhouette.SecuredAction {
    Ok
  }
}
