package models

import models.LineChart.Datum

trait Component[T <: Any] {
  val cls: String
  val name: String
  val value: T
}

case class Variable[T](name: String, value: T) extends Component[T] {
  override val cls: String = "Variable"
}

case class ProgressBar(name: String, value: Int, total: Int) extends Component[Int] {
  override val cls: String = "ProgressBar"
}

case class Image(name: String, value: String /*image path*/) extends Component[String] {
  override val cls: String = "Image"
}

case class Logger(name: String, value: List[String] /*message rows*/) extends Component[List[String]] {
  override val cls: String = "Logger"
}

case class LineChart(name: String, value: List[Datum], xLabel: String, yLabel: String)
  extends Component[List[Datum]] {
  override val cls: String = "LineChart"
}

object LineChart {
  case class Datum(x: Double, y: Double, label: String)
}
