package utils

import play.api.libs.json.{JsObject, JsString, JsValue}

object Auth {
  private val password = sys.env.getOrElse("MITA_PASSWORD", "password")

  def request(requestBody: JsValue): Boolean = requestBody.as[JsObject].value.get("password") match {
    case Some(JsString(password)) => password == Auth.password
    case _ => false
  }

  def string(password: String): Boolean = password == Auth.password
}
