package models

import models.LineChart.Datum
import play.api.libs.json.{JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue}

trait Component[T <: Any] {
  val cls: String
  val name: String
  val value: T
}

object Component {
  def importFromJson(value: JsValue): Component[_] = {
    val obj = value.as[JsObject].value

    val componentName = obj("name").as[JsString].value
    obj("cls") match {
      case JsString("Variable") =>
        val name = componentName
        val value = obj("value") match {
          case JsNumber(value)  => value.toDouble
          case JsString(value)  => value
          case JsBoolean(value) => value
          case JsNull           => null
          case _                => throw new Exception("Unknown type")
        }
        Variable(name, value)
      case JsString("ProgressBar") => ProgressBar(
          componentName,
          obj("value").as[JsNumber].value.toInt,
          obj("total").as[JsNumber].value.toInt
        )
      case JsString("Image") => Image(
          componentName,
          obj("value").as[JsString].value
        )
      case JsString("Logger") => Logger(
          componentName,
          obj("value").as[JsArray].value.map(_.as[JsString].value).toList
        )
      case JsString("LineChart") => LineChart(
          componentName,
          obj("value")
            .as[JsArray]
            .value
            .map(datum => {
              val datumObj = datum.as[JsObject].value
              LineChart.Datum(
                datumObj("x").as[JsNumber].value.toDouble,
                datumObj("y").as[JsNumber].value.toDouble,
                datumObj("label").as[JsString].value
              )
            })
            .toList,
          obj("x_label").as[JsString].value,
          obj("y_label").as[JsString].value
        )
      case _ => throw new Exception("Unknown type")
    }
  }
}

case class Variable[T](name: String, value: T) extends Component[T] {
  override val cls: String = "Variable"
}

case class ProgressBar(name: String, value: Int, total: Int) extends Component[Int] {
  override val cls: String = "ProgressBar"
}

case class Image(name: String, value: String /*image path*/ ) extends Component[String] {
  override val cls: String = "Image"
}

case class Logger(name: String, value: List[String] /*message rows*/ ) extends Component[List[String]] {
  override val cls: String = "Logger"
}

case class LineChart(
    name: String,
    value: List[Datum],
    xLabel: String,
    yLabel: String
) extends Component[List[Datum]] {
  override val cls: String = "LineChart"
}

object LineChart {
  case class Datum(x: Double, y: Double, label: String)
}
