package security

import com.google.inject.Inject
import io.github.honeycombcheesecake.play.silhouette.api.crypto.Base64AuthenticatorEncoder
import io.github.honeycombcheesecake.play.silhouette.api.util.Clock
import io.github.honeycombcheesecake.play.silhouette.impl.authenticators.{
  JWTAuthenticatorService,
  JWTAuthenticatorSettings
}
import io.github.honeycombcheesecake.play.silhouette.impl.util.SecureRandomIDGenerator

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class MitaAuthenticatorService @Inject() (implicit
    clock: Clock,
    ec: ExecutionContext
) extends JWTAuthenticatorService(
      JWTAuthenticatorSettings(
        issuerClaim = "mita",
        authenticatorExpiry = 30.days,
        sharedSecret = sys.env.getOrElse(
          "MITA_TOKEN_SECRET",
          "my-32-character-ultra-secure-and-ultra-long-secret"
        )
      ),
      None,
      new Base64AuthenticatorEncoder,
      new SecureRandomIDGenerator(32),
      clock
    )
