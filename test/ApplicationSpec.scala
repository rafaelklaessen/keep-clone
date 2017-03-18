import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HomeController" should {

    "render the index page when there is no user session" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Keep Clone")
    }

    "render the notes page when there is a user session" in {
      val notes = route(app, FakeRequest(GET, "/").withSession(("username", "hold_on"))).get

      status(notes) mustBe OK
      contentType(notes) mustBe Some("text/html")
      contentAsString(notes) must include ("<section id=\"write-note\" class=\"write-note\" data-color=\"#FFFFFF\">")
    }

    "render a 404 page instead of the archive page when there is no user session" in {
      val archive = route(app, FakeRequest(GET, "/archive")).get

      status(archive) mustBe NOT_FOUND
    }

    "render the archive page when there is a user session" in {
      val archive = route(app, FakeRequest(GET, "/archive").withSession(("username", "hold_on"))).get

      status(archive) mustBe OK
      contentType(archive) mustBe Some("text/html")
      contentAsString(archive) must include ("Archive")
    }

  }

}
