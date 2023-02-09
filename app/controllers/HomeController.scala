package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class HomeController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  def appSummary: Action[AnyContent] = Action {
    Ok(Json.obj("content" -> "Scala Play React Seed!"))
  }

  def echo: Action[AnyContent] = Action { request =>
    println(request)
    Ok(Json.obj("content" -> "Echo!"))
  }
}
