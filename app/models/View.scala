package models

import play.api.libs.json._

import scala.collection.mutable

case class View(name: String) {
  val components: mutable.Map[String, Component[_]] = mutable.Map()
  var needSave = false

  def addComponent(component: Component[_]): Unit = {
    components.update(component.name, component)
    needSave = true
    Memory.needSave = true
  }

  def loadFromJson(json: String): Unit = Json.parse(json).as[JsArray].value.foreach(value => {
    val obj = value.as[JsObject].value

    val componentName = obj("name").as[JsString].value
    obj("cls") match {
      case JsString("Variable") =>
        val name = componentName
        val value = obj("value") match {
          case JsNumber(value) => value.toDouble
          case JsString(value) => value
          case JsBoolean(value) => value
          case JsNull => null
          case _ => throw new Exception("Unknown type")
        }
        addComponent(Variable(name, value))
      case JsString("ProgressBar") => addComponent(ProgressBar(
        componentName,
        obj("value").as[JsNumber].value.toInt,
        obj("total").as[JsNumber].value.toInt
      ))
      case JsString("Image") => addComponent(Image(
        componentName,
        obj("value").as[JsString].value
      ))
      case JsString("Logger") => addComponent(Logger(
        componentName,
        obj("value").as[JsArray].value.map(_.as[JsString].value).toList
      ))
      case JsString("LineChart") => addComponent(LineChart(
        componentName,
        obj("value").as[JsArray].value.map(datum => {
          val datumObj = datum.as[JsObject].value
          LineChart.Datum(
            datumObj("x").as[JsNumber].value.toDouble,
            datumObj("y").as[JsNumber].value.toDouble,
            datumObj("label").as[JsString].value
          )
        }).toList,
        obj("xLabel").as[JsString].value,
        obj("yLabel").as[JsString].value
      ),
      )
      case _ => throw new Exception("Unknown type")
    }
  })
}
