package security

import io.github.honeycombcheesecake.play.silhouette.api.repositories.AuthInfoRepository
import io.github.honeycombcheesecake.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import io.github.honeycombcheesecake.play.silhouette.api.{AuthInfo, LoginInfo}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class MitaAuthInfoRepository @Inject() (implicit
    passwordHasher: PasswordHasher,
    ec: ExecutionContext
) extends AuthInfoRepository {

  override def find[T <: AuthInfo](
      loginInfo: LoginInfo
  )(implicit tag: ClassTag[T]): Future[Option[T]] = loginInfo.providerKey match {
    case "admin" => Future.successful(Some(password.asInstanceOf[T]))
    case "guest" => guestPassword match {
        case Some(guestPw) => Future.successful(Some(guestPw.asInstanceOf[T]))
        case None          => Future.successful(None)
      }
  }

  override def add[T <: AuthInfo](
      loginInfo: LoginInfo,
      authInfo: T
  ): Future[T] = ???

  override def update[T <: AuthInfo](
      loginInfo: LoginInfo,
      authInfo: T
  ): Future[T] = ???

  override def save[T <: AuthInfo](
      loginInfo: LoginInfo,
      authInfo: T
  ): Future[T] = ???

  override def remove[T <: AuthInfo](loginInfo: LoginInfo)(implicit
      tag: ClassTag[T]
  ): Future[Unit] = ???

  private val password = passwordHasher.hash(sys.env.getOrElse("MITA_PASSWORD", "password"))

  private val guestPassword: Option[PasswordInfo] = sys.env.getOrElse("MITA_GUEST_PASSWORD", "changeit") match {
    case "changeit" => None
    case password   => Some(passwordHasher.hash(password))
  }
}
