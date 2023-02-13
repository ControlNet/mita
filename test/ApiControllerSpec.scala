package controllers

import io.github.honeycombcheesecake.play.silhouette.api.Silhouette
import io.github.honeycombcheesecake.play.silhouette.api.services.AuthenticatorService
import io.github.honeycombcheesecake.play.silhouette.impl.providers.CredentialsProvider
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.test.Helpers._
import play.api.test._
import security.MitaEnv

import javax.inject.Inject
import scala.concurrent.ExecutionContext

/** Add your spec here. You can mock out a whole application including requests,
  * plugins etc.
  *
  * For more information, see
  * https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class ApiControllerSpec @Inject() (
    silhouette: Silhouette[MitaEnv],
    authenticatorService: AuthenticatorService[MitaEnv#A],
    credentialsProvider: CredentialsProvider
) extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting {

  "ApiController GET" should {

    "auth return OK with correct password" in {
      val controller = new ApiController(
        stubControllerComponents(),
        silhouette,
        authenticatorService,
        credentialsProvider
      )
      val pw = sys.env.getOrElse("MITA_PASSWORD", "password")
      val response = controller.auth(
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> pw)
        )
      )

      status(response) mustBe OK
      contentAsJson(response).toString must include("token")
    }

    "auth return Unauthorized with incorrect password" in {
      val controller = new ApiController(
        stubControllerComponents(),
        silhouette,
        authenticatorService,
        credentialsProvider
      )
      val pw = sys.env.getOrElse("MITA_PASSWORD", "password") + "1"
      val response = controller.auth(
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> pw)
        )
      )

      status(response) mustBe UNAUTHORIZED
    }
  }
}
