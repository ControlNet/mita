package controllers

import models.Exportable.ExportableOps
import models.{Memory, View}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import utils.Auth

import javax.inject._

@Singleton
class ApiController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def push: Action[AnyContent] = Action { request =>
    try {
      val body = request.body.asJson.get
      if (Auth.request(body)) {
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
      }
      else Unauthorized
    } catch {
      case e: Throwable => e.printStackTrace(); BadRequest
    }
  }

  def listViews(pw: String): Action[AnyContent] = Action {
    if (Auth.string(pw)) Ok(Json.toJson(Memory.views.keys.toList))
    else Unauthorized
  }

  def view(name: String): Action[AnyContent] = Action {
    Memory.views.get(name) match {
      case Some(value) => Ok(value.exportJson)
      case None => BadRequest
    }
  }

  def delete(name: String, pw: String): Action[AnyContent] = Action {
    if (Auth.string(pw)) {
      Memory.views.remove(name)
      Memory.needSave = true
      Ok
    } else Unauthorized
  }

  def auth: Action[AnyContent] = Action {
    Ok
  }
}
