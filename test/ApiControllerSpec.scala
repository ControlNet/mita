package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

/** Add your spec here. You can mock out a whole application including requests,
  * plugins etc.
  *
  * For more information, see
  * https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class ApiControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting {

  "ApiController GET" should {

    "auth return OK with correct password" in {
      val controller = new ApiController(stubControllerComponents())
      val pw = sys.env.getOrElse("MITA_PASSWORD", "password")
      val response = controller.auth(pw)(FakeRequest(GET, s"/api/auth?pw=$pw"))

      status(response) mustBe OK
    }

    "auth return Unauthorized with incorrect password" in {
      val controller = new ApiController(stubControllerComponents())
      val pw = sys.env.getOrElse("MITA_PASSWORD", "password") + "1"
      val response = controller.auth(pw)(FakeRequest(GET, s"/api/auth?pw=$pw"))

      status(response) mustBe UNAUTHORIZED
    }
  }
}
