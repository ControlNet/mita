import akka.actor.ActorSystem
import play.api.Logging
import play.api.inject.ApplicationLifecycle
import utils.FileSync

import javax.inject._
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class Hook @Inject() (lifecycle: ApplicationLifecycle, akkaSystem: ActorSystem) extends Logging {

  FileSync.load()

  private val syncFileContext: ExecutionContext = akkaSystem.dispatchers.lookup("sync-file-context")

  private val fileSync = Future {
    while (!FileSync.needStop) {
      FileSync.save()
      Thread.sleep(1000)
    }
  }(syncFileContext)

  lifecycle.addStopHook { () =>
    FileSync.stop()
    Await.ready(fileSync, scala.concurrent.duration.Duration.Inf)
    Future.successful(())
  }
}
