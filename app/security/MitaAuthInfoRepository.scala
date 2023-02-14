package security

import io.github.honeycombcheesecake.play.silhouette.api.repositories.AuthInfoRepository
import io.github.honeycombcheesecake.play.silhouette.api.util.PasswordHasher
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
  )(implicit tag: ClassTag[T]): Future[Option[T]] = Future {
    Some(password.asInstanceOf[T])
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

  private val password =
    passwordHasher.hash(sys.env.getOrElse("MITA_PASSWORD", "password"))
}
