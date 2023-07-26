package models

import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

trait Exportable[T] {
  def exportJsObject(value: T): JsValue

  private def exportJson(value: T): String = exportJsObject(value).toString()
}

object Exportable {
  private def exportJsObject[T](value: T)(implicit
      exportable: Exportable[T]
  ): JsValue = exportable.exportJsObject(value)

  private def exportJson[T](value: T)(implicit
      exportable: Exportable[T]
  ): String = exportable.exportJson(value)

  implicit class ExportableOps[T](a: T) {
    def exportJsObject(implicit exportable: Exportable[T]): JsValue = Exportable.exportJsObject[T](a)

    def exportJson(implicit exportable: Exportable[T]): String = Exportable.exportJson[T](a)
  }

  private def exportStringComponent(value: Component[String]): JsObject = Json.obj(
    "cls" -> value.cls,
    "name" -> value.name,
    "value" -> value.value
  )

  implicit val variableIntExportable: Exportable[Variable[Int]] = value =>
    Json.obj(
      "cls" -> value.cls,
      "name" -> value.name,
      "value" -> value.value
    )

  implicit val variableDoubleExportable: Exportable[Variable[Double]] = value =>
    Json.obj(
      "cls" -> value.cls,
      "name" -> value.name,
      "value" -> value.value
    )

  implicit val variableStringExportable: Exportable[Variable[String]] = exportStringComponent

  implicit val progressBarExportable: Exportable[ProgressBar] = value =>
    Json.obj(
      "cls" -> value.cls,
      "name" -> value.name,
      "value" -> value.value,
      "total" -> value.total
    )

  implicit val imageExportable: Exportable[Image] = exportStringComponent

  implicit val loggerExportable: Exportable[Logger] = value =>
    Json.obj(
      "cls" -> value.cls,
      "name" -> value.name,
      "value" -> value.value
    )

  implicit val lineChartExportable: Exportable[LineChart] = value =>
    Json.obj(
      "cls" -> value.cls,
      "name" -> value.name,
      "value" -> value.value.map(datum =>
        Json.obj(
          "x" -> datum.x,
          "y" -> datum.y,
          "label" -> datum.label
        )
      ),
      "x_label" -> value.xLabel,
      "y_label" -> value.yLabel
    )

  implicit val memoryExportable: Exportable[View] = value => {
    val arr = value.components.values.map {
      case lineChart: LineChart     => lineChart.exportJsObject
      case logger: Logger           => logger.exportJsObject
      case progressBar: ProgressBar => progressBar.exportJsObject
      case image: Image             => image.exportJsObject
      case Variable(name, value) => value match {
          case v: Int    => Variable[Int](name, v).exportJsObject
          case v: Double => Variable[Double](name, v).exportJsObject
          case v: String => Variable[String](name, v).exportJsObject
        }
    }
    JsArray(arr.toSeq)
  }
}
