package security

import io.github.honeycombcheesecake.play.silhouette.api.{Identity, LoginInfo}

trait User extends Identity

object User {
  case object Admin extends User
  case object Guest extends User
  def apply(loginInfo: LoginInfo): Option[User] = loginInfo.providerKey match {
    case "admin" => Some(Admin)
    case "guest" => Some(Guest)
    case _       => None
  }
}
