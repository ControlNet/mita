import com.google.inject.{AbstractModule, Provides}
import io.github.honeycombcheesecake.play.silhouette.api.repositories.AuthInfoRepository
import io.github.honeycombcheesecake.play.silhouette.api.services.{
  AuthenticatorService,
  IdentityService
}
import io.github.honeycombcheesecake.play.silhouette.api.util.{
  Clock,
  PasswordHasher,
  PasswordHasherRegistry
}
import io.github.honeycombcheesecake.play.silhouette.api.{
  Environment,
  EventBus,
  RequestProvider,
  Silhouette,
  SilhouetteProvider
}
import io.github.honeycombcheesecake.play.silhouette.impl.authenticators.JWTAuthenticator
import io.github.honeycombcheesecake.play.silhouette.impl.providers.BasicAuthProvider
import io.github.honeycombcheesecake.play.silhouette.password.BCryptSha256PasswordHasher
import net.codingwell.scalaguice.ScalaModule
import security.{
  MitaAuthInfoRepository,
  MitaAuthenticatorService,
  MitaEnv,
  User,
  UserService
}

import scala.concurrent.ExecutionContext

/** This class is a Guice module that tells Guice how to bind several different
  * types. This Guice module is created when the Play application starts.
  *
  * Play will automatically use any class called `Module` that is in the root
  * package. You can create modules in other locations by adding
  * `play.modules.enabled` settings to the `application.conf` configuration
  * file.
  */
class Module extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[Clock].toInstance(Clock())
    bind[Silhouette[MitaEnv]].to[SilhouetteProvider[MitaEnv]]
    bind[IdentityService[User]].toInstance(UserService)
    bind[Hook].asEagerSingleton()
  }

  @Provides
  def provideAuthInfoRepository(implicit
      ec: ExecutionContext,
      passwordHasher: PasswordHasher
  ): AuthInfoRepository = new MitaAuthInfoRepository

  @Provides
  def providePasswordHasher(): PasswordHasher = new BCryptSha256PasswordHasher

  @Provides
  def provideAuthenticatorService(implicit
      clock: Clock,
      ec: ExecutionContext
  ): AuthenticatorService[JWTAuthenticator] = new MitaAuthenticatorService

  @Provides
  def providePasswordHasherRegistry(implicit
      passwordHasher: PasswordHasher
  ): PasswordHasherRegistry =
    PasswordHasherRegistry(passwordHasher, Nil)

  @Provides
  def provideAuthProvider(implicit
      authInfoRepository: AuthInfoRepository,
      passwordHasherRegistry: PasswordHasherRegistry,
      ec: ExecutionContext
  ): RequestProvider = new BasicAuthProvider(
    authInfoRepository,
    passwordHasherRegistry
  )

  @Provides
  def provideEventBus(implicit ec: ExecutionContext): EventBus = EventBus()

  @Provides
  def provideEnvironment(implicit
      authenticatorService: AuthenticatorService[JWTAuthenticator],
      authProvider: RequestProvider,
      eventBus: EventBus,
      ec: ExecutionContext
  ): Environment[MitaEnv] = Environment[MitaEnv](
    UserService,
    authenticatorService,
    Seq(authProvider),
    eventBus
  )
}
