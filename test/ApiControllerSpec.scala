import controllers.ApiController
import models.Memory
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.Play.materializer
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceInjectorBuilder}
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.api.test.Helpers._
import play.api.test._
import play.test.WithApplication
import utils.FileSync

import java.lang.annotation.Annotation
import javax.inject.Scope

/** Add your spec here. You can mock out a whole application including requests,
  * plugins etc.
  *
  * For more information, see
  * https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class ApiControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with BeforeAndAfterAll {

  // remove temp files generated
  override def afterAll(): Unit = {
    FileSync.remove("test_view_1")
    FileSync.remove("test_view_2")
    FileSync.remove("test_view_3")
  }

  "ApiController GET" should {

    "auth admin return OK with correct password" in {

      val pw = sys.env.getOrElse("MITA_PASSWORD", "password")
      val response = route(
        app,
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> pw)
        )
      ).get

      status(response) mustBe OK
      contentAsJson(response).toString must include("token")
      contentAsJson(response).toString must include("role")
      contentAsJson(response).toString must include("admin")
    }

    "auth guest return OK with correct password" in {
      val pw = sys.env.getOrElse("MITA_GUEST_PASSWORD", "changeit")
      val response = route(
        app,
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> pw)
        )
      ).get

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
      val response = route(
        app,
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> pw)
        )
      ).get

      status(response) mustBe UNAUTHORIZED

      val guestPw = sys.env.getOrElse("MITA_GUEST_PASSWORD", "password") + "1"
      val guestResponse = route(
        app,
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> guestPw)
        )
      ).get

      status(guestResponse) mustBe UNAUTHORIZED
    }
  }

  "ApiController POST" should {
    "push success with admin token" in {
      val token = TokenOf.admin
      val response = route(
        app,
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
      ).get

      status(response) mustBe OK
      // check in memory
      Memory.views.contains("test_view_1") mustBe true
      Memory.views("test_view_1").components.contains("x") mustBe true
      Memory.views("test_view_1").components("x").cls mustBe "Variable"
      Memory.views("test_view_1").components("x").name mustBe "x"
      Memory.views("test_view_1").components("x").value mustBe 1
    }

    "push fail with guest token" in {
      TokenOf.guest match {
        case Some(token) =>
          val response = route(
            app,
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
          ).get
          status(response) mustBe UNAUTHORIZED
          // check in memory
          Memory.views.contains("test_view_2") mustBe false
        case None =>
      }
    }

    "push fail with no token" in {
      val response = route(
        app,
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
      ).get

      status(response) mustBe UNAUTHORIZED
      // check in memory
      Memory.views.contains("test_view_3") mustBe false
    }

    "push fail with invalid body format" in {
      val token = TokenOf.admin
      val response = route(
        app,
        FakeRequest(
          POST,
          s"/api/push",
          Headers("Content-Type" -> "application/json", "X-Auth-Token" -> token),
          Json.obj(
            "view" -> "test_view_4",
            "cls" -> "Variable",
            "name" -> "x",
            "value" -> 4
          )
        )
      ).get

      status(response) mustBe BAD_REQUEST
      // check in memory
      Memory.views.contains("test_view_4") mustBe false
    }
  }

  object TokenOf {
    lazy val admin: String = getToken(sys.env.getOrElse("MITA_PASSWORD", "password"))
    lazy val guest: Option[String] =
      if (sys.env.getOrElse("MITA_GUEST_PASSWORD", "changeit") == "changeit") None
      else Some(getToken(sys.env.getOrElse("MITA_GUEST_PASSWORD", "changeit")))

    private def getToken(pw: String): String = {
      val response = route(
        app,
        FakeRequest(
          POST,
          s"/api/auth",
          Headers("Content-Type" -> "application/json"),
          Json.obj("password" -> pw)
        )
      ).get
      (contentAsJson(response) \ "token").as[String]
    }
  }

}
