package controllers

import io.github.honeycombcheesecake.play.silhouette.api.{RequestProvider, Silhouette}
import io.github.honeycombcheesecake.play.silhouette.api.services.AuthenticatorService
import io.github.honeycombcheesecake.play.silhouette.impl.providers.CredentialsProvider
import models.Memory
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.test.Helpers._
import play.api.test._
import security.MitaEnv
import utils.FileSync

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
    requestProvider: RequestProvider
)(implicit ec: ExecutionContext)
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting
    with BeforeAndAfterAll {

  val controller = new ApiController(
    stubControllerComponents(),
    silhouette,
    authenticatorService,
    requestProvider
  )

  // remove temp files generated
  override def afterAll(): Unit = {
    FileSync.remove("test_view_1")
    FileSync.remove("test_view_2")
    FileSync.remove("test_view_3")
  }

  "ApiController GET" should {

    "auth admin return OK with correct password" in {

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
      contentAsJson(response).toString must include("role")
      contentAsJson(response).toString must include("admin")
    }

    "auth guest return OK with correct password" in {
      val pw = sys.env.getOrElse("MITA_GUEST_PASSWORD", "changeit")
      val response = controller.auth(
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> pw)
        )
      )

      if (pw == "changeit") {
        status(response) mustBe UNAUTHORIZED
      } else {
        status(response) mustBe OK
        contentAsJson(response).toString must include("token")
        contentAsJson(response).toString must include("role")
        contentAsJson(response).toString must include("guest")
      }
    }

    "auth return Unauthorized with incorrect password" in {
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

      val guestPw = sys.env.getOrElse("MITA_GUEST_PASSWORD", "password") + "1"
      val guestResponse = controller.auth(
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> guestPw)
        )
      )

      status(guestResponse) mustBe UNAUTHORIZED
    }
  }

  "ApiController POST" should {
    "push success with admin token" in {
      val token = TokenOf.admin
      val response = controller.push(
        FakeRequest(
          POST,
          s"/api/push",
          Headers("Content-Type" -> "application/json", "X-Auth-Token" -> token),
          Json.obj(
            "view" -> "test_view_1",
            "data" -> Json.arr(
              Json.obj("cls" -> "Variable", "name" -> "x", "value" -> 1)
            )
          )
        )
      )

      status(response) mustBe OK
      // check in memory
      Memory.views.contains("test_view_1") mustBe true
      Memory.views("test_view_1").components.contains("x") mustBe true
      Memory.views("test_view_1").components("x").cls mustBe "Variable"
      Memory.views("test_view_1").components("x").name mustBe "x"
      Memory.views("test_view_1").components("x").value mustBe 1
    }

    "push fail with guest token" in {
      val token = TokenOf.guest.get
      val response = controller.push(
        FakeRequest(
          POST,
          s"/api/push",
          Headers("Content-Type" -> "application/json", "X-Auth-Token" -> token),
          Json.obj(
            "view" -> "test_view_2",
            "data" -> Json.arr(
              Json.obj("cls" -> "Variable", "name" -> "x", "value" -> 2)
            )
          )
        )
      )

      status(response) mustBe UNAUTHORIZED
      // check in memory
      Memory.views.contains("test_view_2") mustBe false
    }

    "push fail with no token" in {
      val response = controller.push(
        FakeRequest(
          POST,
          s"/api/push",
          Headers("Content-Type" -> "application/json"),
          Json.obj(
            "view" -> "test_view_3",
            "data" -> Json.arr(
              Json.obj("cls" -> "Variable", "name" -> "x", "value" -> 3)
            )
          )
        )
      )

      status(response) mustBe UNAUTHORIZED
      // check in memory
      Memory.views.contains("test_view_3") mustBe false
    }

    "push fail with invalid body format" in {
      val token = TokenOf.admin
      val response = controller.push(
        FakeRequest(
          POST,
          s"/api/push",
          Headers("Content-Type" -> "application/json", "X-Auth-Token" -> token),
          Json.obj(
            "view" -> "test_view_4",
            "data" -> Json.arr(
              Json.obj("cls" -> "Variable", "name" -> "x", "value" -> 4)
            )
          )
        )
      )

      status(response) mustBe BAD_REQUEST
      // check in memory
      Memory.views.contains("test_view_4") mustBe false
    }
  }

  object TokenOf {
    def admin: String = getToken(sys.env.getOrElse("MITA_PASSWORD", "password"))
    def guest: Option[String] =
      if (sys.env.getOrElse("MITA_GUEST_PASSWORD", "changeit") == "changeit") None
      else Some(getToken(sys.env.getOrElse("MITA_GUEST_PASSWORD", "changeit")))

    private def getToken(pw: String): String = {
      val response = controller.auth(
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> pw)
        )
      )
      (contentAsJson(response) \ "token").as[String]
    }
  }
}
