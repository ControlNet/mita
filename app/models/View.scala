package models

import play.api.libs.json._

import scala.collection.mutable

case class View(name: String) {
  val components: mutable.Map[String, Component[_]] = mutable.Map()
  var needSave = false

  def updateComponent(component: Component[_]): Unit = {
    components.update(component.name, component)
    needSave = true
    Memory.needSave = true
  }

  def loadFromJson(json: String): Unit = updateFromJson(Json.parse(json))

  def updateFromJson(json: JsValue): Unit = json
    .as[JsArray]
    .value
    .map(Component.importFromJson)
    .foreach(updateComponent)
}
