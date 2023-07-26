package controllers

import io.github.honeycombcheesecake.play.silhouette.api.actions.SecuredRequest
import io.github.honeycombcheesecake.play.silhouette.api.exceptions.ConfigurationException
import io.github.honeycombcheesecake.play.silhouette.api.services.AuthenticatorService
import io.github.honeycombcheesecake.play.silhouette.api.{RequestProvider, Silhouette}
import io.github.honeycombcheesecake.play.silhouette.impl.exceptions.{
  IdentityNotFoundException,
  InvalidPasswordException
}
import models.Exportable.ExportableOps
import models.{Memory, View}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import security.{MitaEnv, User}
import utils.FileSync

import javax.inject._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class ApiController @Inject() (
    cc: ControllerComponents,
    silhouette: Silhouette[MitaEnv],
    authenticatorService: AuthenticatorService[MitaEnv#A],
    requestProvider: RequestProvider
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  /** Push the view data to the server.
    * Only for Admin user.
    */
  def push: Action[AnyContent] = silhouette.SecuredAction { implicit request =>
    adminOnly {
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
  }

  /** List all views on the server.
    * For all users.
    */
  def listViews: Action[AnyContent] = silhouette.SecuredAction {
    Ok(Json.toJson(Memory.views.keys.toList))
  }

  /** Get the view data from the server.
    * For all users.
    */
  def view(name: String): Action[AnyContent] = silhouette.SecuredAction {
    Memory.views.get(name) match {
      case Some(value) => Ok(value.exportJson)
      case None        => BadRequest
    }
  }

  /** Delete the view from the server.
    * Only for Admin user.
    */
  def deleteView(viewName: String): Action[AnyContent] = silhouette.SecuredAction { implicit request =>
    adminOnly {
      Memory.views.remove(viewName)
      FileSync.remove(viewName)
      Memory.needSave = true
      Ok
    }
  }

  /** Delete all views from the server.
    * Only for Admin user.
    */
  def deleteAll: Action[AnyContent] = silhouette.SecuredAction { implicit request =>
    adminOnly {
      val views = Memory.views.keys.toList
      Memory.views.clear()
      views.foreach(FileSync.remove)
      Memory.needSave = true
      Ok
    }
  }

  /** Delete a component from the view.
    * Only for Admin user
    */
  def deleteComponent(
      viewName: String,
      componentName: String
  ): Action[AnyContent] = silhouette.SecuredAction { implicit request =>
    adminOnly {
      Memory.views.get(viewName) match {
        case Some(value) =>
          value.components.remove(componentName)
          Memory.needSave = true
          Ok
        case None => BadRequest
      }
    }
  }

  /** Authenticate the user.
    * For all people
    * @return 200, 400, 401
    *         Return the token and the role
    */
  def auth: Action[AnyContent] = Action { implicit request =>
    val future = requestProvider
      .authenticate(request)
      .map {
        _.get
      }
      .flatMap { loginInfo =>
        authenticatorService.create(loginInfo).zip(Future.successful(loginInfo.providerKey))
      }
      .flatMap { case (authenticator, role) => authenticatorService.init(authenticator).zip(Future.successful(role)) }
      .map { case (token, role) => Ok(Json.obj("token" -> token, "role" -> role)) }
      .recover {
        case _ @(_: InvalidPasswordException | _: ConfigurationException | _: IdentityNotFoundException) => Unauthorized
        case _: Throwable                                                                                => BadRequest
      }

    Await.result(future, Duration.Inf)
  }

  def testAuth: Action[AnyContent] = silhouette.SecuredAction {
    Ok
  }

  private def adminOnly(block: => Result)(implicit request: SecuredRequest[MitaEnv, AnyContent]): Result =
    request.identity match {
      case User.Admin => block
      case User.Guest => Unauthorized
    }
}
