package utils

import models.Exportable.ExportableOps
import models.{Component, Memory, View}

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.util.{Failure, Success, Try, Using}

object FileSync {
  var needStop = false
  private val savePath = Paths.get("data")
  if (!savePath.toFile.exists()) savePath.toFile.mkdirs()

  private def saveView(view: View): Unit = {
    if (view.needSave) {
      val oldPath = savePath.resolve(view.name + ".json")
      val newPath = savePath.resolve(view.name + ".json.tmp")

      Try {
        Files.write(newPath, view.exportJson.getBytes)
      } match {
        case Failure(exception) => exception.printStackTrace()
        case Success(_) =>
          if (oldPath.toFile.exists()) oldPath.toFile.delete()
          newPath.toFile.renameTo(oldPath.toFile)
          view.needSave = false
      }
    }
  }

  def save(): Unit = {
    if (Memory.needSave) {
      Memory.views.values.foreach(saveView)
      Memory.needSave = false
    }
  }

  def load(): Unit = savePath.toFile
    .listFiles()
    .filter(_.getName.endsWith(".json"))
    .foreach(file => {
      val view = View(file.getName.replace(".json", ""))
      Using(Source.fromFile(file)) { source =>
        view.loadFromJson(source.mkString)
      }
      Memory.views.update(view.name, view)
    })

  def stop(): Unit = needStop = true
}
