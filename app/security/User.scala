package security

import io.github.honeycombcheesecake.play.silhouette.api.{Identity, LoginInfo}

case class User(loginInfo: LoginInfo) extends Identity
