package security

import io.github.honeycombcheesecake.play.silhouette.api.Env
import io.github.honeycombcheesecake.play.silhouette.impl.authenticators.JWTAuthenticator

trait MitaEnv extends Env {
  override type I = User
  override type A = JWTAuthenticator
}
