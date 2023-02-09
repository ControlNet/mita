package models

import scala.collection.mutable

object Memory {
  val views: mutable.Map[String, View] = mutable.Map()
  var needSave = false
}
