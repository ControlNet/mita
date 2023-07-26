package security

import io.github.honeycombcheesecake.play.silhouette.api.{LoginInfo, RequestProvider}
import io.github.honeycombcheesecake.play.silhouette.api.exceptions.ConfigurationException
import io.github.honeycombcheesecake.play.silhouette.api.repositories.AuthInfoRepository
import io.github.honeycombcheesecake.play.silhouette.api.util.{Credentials, PasswordHasherRegistry, PasswordInfo}
import io.github.honeycombcheesecake.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import io.github.honeycombcheesecake.play.silhouette.impl.providers.{CredentialsProvider, PasswordProvider}
import io.github.honeycombcheesecake.play.silhouette.impl.providers.PasswordProvider.{HasherIsNotRegistered, PasswordDoesNotMatch, PasswordInfoNotFound}
import play.api.libs.json.JsResultException
import play.api.mvc.{AnyContent, Request}

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class MitaRequestProvider @Inject() (
    override val authInfoRepository: AuthInfoRepository,
    override val passwordHasherRegistry: PasswordHasherRegistry
)(implicit override val executionContext: ExecutionContext)
    extends CredentialsProvider(authInfoRepository, passwordHasherRegistry)(executionContext)
    with RequestProvider {
  this: PasswordProvider =>

  override def authenticate[B](request: Request[B]): Future[Option[LoginInfo]] = Future {
    val password = (request.body.asInstanceOf[AnyContent].asJson.get \ "password").as[String]
    Credentials("", password)
  }.flatMap(authenticate).map(Some(_)).recover {
    case _: NoSuchElementException => None
    case _: JsResultException      => None
  }

  override def authenticate(credentials: Credentials): Future[LoginInfo] = {
    // try to authenticate with admin passwords
    LoginInfoBuilder.admin.flatMap { loginInfo =>
      authenticatePassword(loginInfo, credentials.password).map {
        case Authenticated   => loginInfo
        case NotFound(error) => throw new IdentityNotFoundException(error)
        case _ => Await.result(
            LoginInfoBuilder.guest.flatMap { loginInfo =>
              authenticatePassword(loginInfo, credentials.password).map {
                case Authenticated            => loginInfo
                case InvalidPassword(error)   => throw new InvalidPasswordException(error)
                case UnsupportedHasher(error) => throw new ConfigurationException(error)
                case NotFound(error)          => throw new IdentityNotFoundException(error)
              }
            },
            Duration.Inf
          )
      }
    }
  }

  private def authenticatePassword(loginInfo: LoginInfo, password: String): Future[State] = {
    authInfoRepository.find[PasswordInfo](loginInfo).flatMap {
      case Some(passwordInfo) => passwordHasherRegistry.find(passwordInfo) match {
          case Some(hasher) if hasher.matches(passwordInfo, password) => Future.successful(Authenticated)
          case Some(hasher) => Future.successful(InvalidPassword(PasswordDoesNotMatch.format(id)))
          case None => Future.successful(
              UnsupportedHasher(
                HasherIsNotRegistered
                  .format(id, passwordInfo.hasher, passwordHasherRegistry.all.map(_.id).mkString(", "))
              )
            )
        }
      case None => Future.successful(NotFound(PasswordInfoNotFound.format(id, loginInfo)))
    }
  }

  // Disable the previous loginInfo method
  override def loginInfo(credentials: Credentials): Future[LoginInfo] =
    Future.failed(new NotImplementedError("loginInfo"))

  private object LoginInfoBuilder {
    def admin: Future[LoginInfo] = Future.successful(LoginInfo(id, "admin"))
    def guest: Future[LoginInfo] = Future.successful(LoginInfo(id, "guest"))
  }
}
