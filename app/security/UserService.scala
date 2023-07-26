package security

import io.github.honeycombcheesecake.play.silhouette.api.LoginInfo
import io.github.honeycombcheesecake.play.silhouette.api.services.IdentityService

import scala.concurrent.Future

object UserService extends IdentityService[User] {
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    Future.successful(User(loginInfo))
}
