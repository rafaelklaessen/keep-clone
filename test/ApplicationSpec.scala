import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

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

  "NoteController" should {
    "create a note when there is a user session and the POST parameters are correct" in {
      val createNote = route(app, FakeRequest(POST, "/notes")
        .withFormUrlEncodedBody(("title", "test"))
        .withSession(("username", "hold_on"))).get

      status(createNote) mustBe OK
      contentType(createNote) mustBe Some("text/plain")
    }

    "not create a note when there is a user session but the POST parameters are incorrect" in {
      val createNote = route(app, FakeRequest(POST, "/notes")
        .withFormUrlEncodedBody(("notcontent", "test"))
        .withSession(("username", "hold_on"))).get

      status(createNote) mustBe BAD_REQUEST
      contentType(createNote) mustBe Some("text/plain")
    }

    "not create a note when there is no user session" in {
      val createNote = route(app, FakeRequest(POST, "/notes").withFormUrlEncodedBody(("title", "test"))).get

      status(createNote) mustBe UNAUTHORIZED
      contentType(createNote) mustBe Some("text/plain")
    }

    "delete an existing note when there is a user session that owns the note and a correct ID is given" in {
      val noteId = contentAsString(route(app, FakeRequest(POST, "/notes")
        .withFormUrlEncodedBody(("title", "test"))
        .withSession(("username", "hold_on"))).get)

      val deleteNote = route(app, FakeRequest(POST, "/notes/delete")
        .withFormUrlEncodedBody(("id", noteId))
        .withSession(("username", "hold_on"))).get

      status(deleteNote) mustBe OK
      contentType(deleteNote) mustBe Some("text/plain")
    }

    "not delete a note when the user session doesn't own the note" in {
      val noteId = contentAsString(route(app, FakeRequest(POST, "/notes")
        .withFormUrlEncodedBody(("title", "test"))
        .withSession(("username", "ABC"))).get)

      val deleteNote = route(app, FakeRequest(POST, "/notes/delete")
        .withFormUrlEncodedBody(("id", noteId))
        .withSession(("username", "hold_on"))).get

      status(deleteNote) mustBe BAD_REQUEST
      contentType(deleteNote) mustBe Some("text/plain")
    }

    "not delete a note when no ID is given" in {
      val deleteNote = route(app, FakeRequest(POST, "/notes/delete")
        .withFormUrlEncodedBody(("notid", "test"))
        .withSession(("username", "hold_on"))).get

      status(deleteNote) mustBe BAD_REQUEST
      contentType(deleteNote) mustBe Some("text/plain")
    }

    "not delete a note when an incorrect ID is given" in {
      val deleteNote = route(app, FakeRequest(POST, "/notes/delete")
        .withFormUrlEncodedBody(("id", "test"))
        .withSession(("username", "hold_on"))).get

      status(deleteNote) mustBe BAD_REQUEST
      contentType(deleteNote) mustBe Some("text/plain")
    }

    "not delete a nonexisting note" in {
      val deleteNote = route(app, FakeRequest(POST, "/notes/delete")
        .withFormUrlEncodedBody(("id", "1"))
        .withSession(("username", "hold_on"))).get

      status(deleteNote) mustBe BAD_REQUEST
      contentType(deleteNote) mustBe Some("text/plain")
    }

    "not delete a note when there is no user session" in {
      val deleteNote = route(app, FakeRequest(POST, "/notes/delete").withFormUrlEncodedBody(("foo", "bar"))).get

      status(deleteNote) mustBe UNAUTHORIZED
      contentType(deleteNote) mustBe Some("text/plain")
    }

    "update an existing note when there's a user session that owns the note and correct POST parameters are given" in {
      val updateNote = route(app, FakeRequest(POST, "/notes/update")
        .withFormUrlEncodedBody(("id", "104"), ("fields", "{}"))
        .withSession(("username", "hold_on"))).get

      status(updateNote) mustBe OK
      contentType(updateNote) mustBe Some("text/plain")
    }

    "not update a note when no ID is given" in {
      val updateNote = route(app, FakeRequest(POST, "/notes/update")
        .withFormUrlEncodedBody(("fields", "{}"))
        .withSession(("username", "hold_on"))).get

      status(updateNote) mustBe BAD_REQUEST
      contentType(updateNote) mustBe Some("text/plain")
    }

    "not update a note when an incorrect ID is given" in {
      val updateNote = route(app, FakeRequest(POST, "/notes/update")
        .withFormUrlEncodedBody(("id", "test"), ("fields", "{}"))
        .withSession(("username", "hold_on"))).get

      status(updateNote) mustBe BAD_REQUEST
      contentType(updateNote) mustBe Some("text/plain")
    }

    "not update a note when no fields are given" in {
      val updateNote = route(app, FakeRequest(POST, "/notes/update")
        .withFormUrlEncodedBody(("id", "104"))
        .withSession(("username", "hold_on"))).get

      status(updateNote) mustBe BAD_REQUEST
      contentType(updateNote) mustBe Some("text/plain")
    }

    "not update a note when the fields parameter is incorrect" in {
      val updateNote = route(app, FakeRequest(POST, "/notes/update")
        .withFormUrlEncodedBody(("id", "104"), ("fields", "test"))
        .withSession(("username", "hold_on"))).get

      status(updateNote) mustBe BAD_REQUEST
      contentType(updateNote) mustBe Some("text/plain")
    }

    "not update a note when the user session doesn't own the note" in {
      val updateNote = route(app, FakeRequest(POST, "/notes/update")
        .withFormUrlEncodedBody(("id", "104"), ("fields", "{}"))
        .withSession(("username", "ABB"))).get

      status(updateNote) mustBe BAD_REQUEST
      contentType(updateNote) mustBe Some("text/plain")
    }

    "not update a nonexisting note" in {
      val updateNote = route(app, FakeRequest(POST, "/notes/update")
        .withFormUrlEncodedBody(("id", "1"), ("fields", "{}"))
        .withSession(("username", "hold_on"))).get

      status(updateNote) mustBe BAD_REQUEST
      contentType(updateNote) mustBe Some("text/plain")
    }

    "not update a note when there is no user session" in {
      val updateNote = route(app, FakeRequest(POST, "/notes/update").withFormUrlEncodedBody(("foo", "bar"))).get

      status(updateNote) mustBe UNAUTHORIZED
      contentType(updateNote) mustBe Some("text/plain")
    }

    "add an existing owner to a note when there's a user session that owns the note and correct POST parameters are given" in {
      val addOwner = route(app, FakeRequest(POST, "/notes/addowner")
        .withFormUrlEncodedBody(("id", "104"), ("owner", "ABC"))
        .withSession(("username", "hold_on"))).get

      status(addOwner) mustBe OK
      contentType(addOwner) mustBe Some("text/plain")
    }

    "not add an owner to a note when no note ID is given" in {
      val addOwner = route(app, FakeRequest(POST, "/notes/addowner")
        .withFormUrlEncodedBody(("owner", "ABC"))
        .withSession(("username", "hold_on"))).get

      status(addOwner) mustBe BAD_REQUEST
      contentType(addOwner) mustBe Some("text/plain")
    }

    "not add an owner to a note when an incorrect note ID is given" in {
      val addOwner = route(app, FakeRequest(POST, "/notes/addowner")
        .withFormUrlEncodedBody(("id", "test"), ("owner", "ABC"))
        .withSession(("username", "hold_on"))).get

      status(addOwner) mustBe BAD_REQUEST
      contentType(addOwner) mustBe Some("text/plain")
    }

    "not add an owner to a note when no owner is given" in {
      val addOwner = route(app, FakeRequest(POST, "/notes/addowner")
        .withFormUrlEncodedBody(("id", "104"))
        .withSession(("username", "hold_on"))).get

      status(addOwner) mustBe BAD_REQUEST
      contentType(addOwner) mustBe Some("text/plain")
    }

    "not add a nonexisting owner to a note" in {
      val addOwner = route(app, FakeRequest(POST, "/notes/addowner")
        .withFormUrlEncodedBody(("id", "104"), ("owner", "iwontandshallnotexist"))
        .withSession(("username", "hold_on"))).get

      status(addOwner) mustBe BAD_REQUEST
      contentType(addOwner) mustBe Some("text/plain")
    }

    "not add an owner to a note when the user session doesn't own the note" in {
      val addOwner = route(app, FakeRequest(POST, "/notes/addowner")
        .withFormUrlEncodedBody(("id", "104"), ("owner", "hold_on"))
        .withSession(("username", "ABB"))).get

      status(addOwner) mustBe BAD_REQUEST
      contentType(addOwner) mustBe Some("text/plain")
    }

    "not add an owner to a nonexising note" in {
      val addOwner = route(app, FakeRequest(POST, "/notes/addowner")
        .withFormUrlEncodedBody(("id", "1"), ("owner", "ABC"))
        .withSession(("username", "hold_on"))).get

      status(addOwner) mustBe BAD_REQUEST
      contentType(addOwner) mustBe Some("text/plain")
    }

    "not add an owner to a note when there is no user session" in {
      val addOwner = route(app, FakeRequest(POST, "/notes/addowner").withFormUrlEncodedBody(("foo", "bar"))).get

      status(addOwner) mustBe UNAUTHORIZED
      contentType(addOwner) mustBe Some("text/plain")
    }

    "pin a note when there's a user session that owns the note and correct POST parameters are given" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setpinned")
        .withFormUrlEncodedBody(("id", "104"), ("pinned", "true"))
        .withSession(("username", "hold_on"))).get

      status(pinNote) mustBe OK
      contentType(pinNote) mustBe Some("text/plain")
    }

    "not pin a note when no ID is given" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setpinned")
        .withFormUrlEncodedBody(("pinned", "true"))
        .withSession(("username", "hold_on"))).get

      status(pinNote) mustBe BAD_REQUEST
      contentType(pinNote) mustBe Some("text/plain")
    }

    "not pin a note when an incorrect note ID is given" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setpinned")
        .withFormUrlEncodedBody(("id", "test"), ("pinned", "true"))
        .withSession(("username", "hold_on"))).get

      status(pinNote) mustBe BAD_REQUEST
      contentType(pinNote) mustBe Some("text/plain")
    }

    "not pin a note when no pinned state is given" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setpinned")
        .withFormUrlEncodedBody(("id", "104"))
        .withSession(("username", "hold_on"))).get

      status(pinNote) mustBe BAD_REQUEST
      contentType(pinNote) mustBe Some("text/plain")
    }

    "not pin a note when an incorrect pinned state is given" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setpinned")
        .withFormUrlEncodedBody(("id", "104"), ("pinned", "test"))
        .withSession(("username", "hold_on"))).get

      status(pinNote) mustBe BAD_REQUEST
      contentType(pinNote) mustBe Some("text/plain")
    }

    "not pin a note when the user session doesn't own the note" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setpinned")
        .withFormUrlEncodedBody(("id", "104"), ("pinned", "true"))
        .withSession(("username", "ABB"))).get

      status(pinNote) mustBe BAD_REQUEST
      contentType(pinNote) mustBe Some("text/plain")
    }

    "not pin a nonexisting note" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setpinned")
        .withFormUrlEncodedBody(("id", "1"), ("pinned", "true"))
        .withSession(("username", "hold_on"))).get

      status(pinNote) mustBe BAD_REQUEST
      contentType(pinNote) mustBe Some("text/plain")
    }

    "not pin a note when there is no user session" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setpinned").withFormUrlEncodedBody(("foo", "bar"))).get

      status(pinNote) mustBe UNAUTHORIZED
      contentType(pinNote) mustBe Some("text/plain")
    }

    "unarchive a note when there's a user session that owns the note and correct POST parameters are given" in {
      val archiveNote = route(app, FakeRequest(POST, "/notes/setarchived")
        .withFormUrlEncodedBody(("id", "104"), ("archived", "false"))
        .withSession(("username", "hold_on"))).get

      status(archiveNote) mustBe OK
      contentType(archiveNote) mustBe Some("text/plain")
    }

    "not unarchive a note when no ID is given" in {
      val archiveNote = route(app, FakeRequest(POST, "/notes/setarchived")
        .withFormUrlEncodedBody(("archived", "false"))
        .withSession(("username", "hold_on"))).get

      status(archiveNote) mustBe BAD_REQUEST
      contentType(archiveNote) mustBe Some("text/plain")
    }

    "not unarchive a note when an incorrect note ID is given" in {
      val archiveNote = route(app, FakeRequest(POST, "/notes/setarchived")
        .withFormUrlEncodedBody(("id", "test"), ("pinned", "false"))
        .withSession(("username", "hold_on"))).get

      status(archiveNote) mustBe BAD_REQUEST
      contentType(archiveNote) mustBe Some("text/plain")
    }

    "not unarchive a note when no archived state is given" in {
      val archiveNote = route(app, FakeRequest(POST, "/notes/setarchived")
        .withFormUrlEncodedBody(("id", "104"))
        .withSession(("username", "hold_on"))).get

      status(archiveNote) mustBe BAD_REQUEST
      contentType(archiveNote) mustBe Some("text/plain")
    }

    "not unarchive a note when an incorrect archived state is given" in {
      val archiveNote = route(app, FakeRequest(POST, "/notes/setarchived")
        .withFormUrlEncodedBody(("id", "104"), ("archived", "test"))
        .withSession(("username", "hold_on"))).get

      status(archiveNote) mustBe BAD_REQUEST
      contentType(archiveNote) mustBe Some("text/plain")
    }

    "not unarchive a note when the user session doesn't own the note" in {
      val archiveNote = route(app, FakeRequest(POST, "/notes/setarchived")
        .withFormUrlEncodedBody(("id", "104"), ("archived", "false"))
        .withSession(("username", "ABB"))).get

      status(archiveNote) mustBe BAD_REQUEST
      contentType(archiveNote) mustBe Some("text/plain")
    }

    "not unarchive a nonexisting note" in {
      val pinNote = route(app, FakeRequest(POST, "/notes/setarchived")
        .withFormUrlEncodedBody(("id", "1"), ("archived", "false"))
        .withSession(("username", "hold_on"))).get

      status(pinNote) mustBe BAD_REQUEST
      contentType(pinNote) mustBe Some("text/plain")
    }

    "not unarchive a note when there is no user session" in {
      val archiveNote = route(app, FakeRequest(POST, "/notes/setarchived").withFormUrlEncodedBody(("foo", "bar"))).get

      status(archiveNote) mustBe UNAUTHORIZED
      contentType(archiveNote) mustBe Some("text/plain")
    }

  }

  "SettingsController" should {
    "render a 404 page instead of the setttings page when there is no user session" in {
      val settings = route(app, FakeRequest(GET, "/settings")).get

      status(settings) mustBe NOT_FOUND
    }

    "render the settings page when there is a user session" in {
      val settings = route(app, FakeRequest(GET, "/settings").withSession(("username", "hold_on"))).get

      status(settings) mustBe OK
      contentType(settings) mustBe Some("text/html")
      contentAsString(settings) must include ("Settings")
    }

    "render the OAuth settings page when the user session is provided by OAuth" in {
      val settings = route(app, FakeRequest(GET, "/settings")
        .withSession(("oauth", "google"), ("username", "foobar"), ("email", "foo@bar"))).get

      status(settings) mustBe OK
      contentType(settings) mustBe Some("text/html")
      contentAsString(settings) must include ("Settings")
      contentAsString(settings) must include ("OAuth")
    }

    "update the user settings when there's a user session and valid fields are provided" in {
      val updateSettings = route(app, FakeRequest(POST, "/settings/update")
        .withFormUrlEncodedBody(("fields", "{}"))
        .withSession(("username", "hold_on"))).get

      status(updateSettings) mustBe OK
      contentType(updateSettings) mustBe Some("text/plain")
    }

    "not update the user settings when there are no fields provided" in {
      val updateSettings = route(app, FakeRequest(POST, "/settings/update")
        .withFormUrlEncodedBody(("foo", "bar"))
        .withSession(("username", "hold_on"))).get

      status(updateSettings) mustBe BAD_REQUEST
      contentType(updateSettings) mustBe Some("text/plain")
    }

    "not update the user settings when the fields are incorrect" in {
      val updateSettings = route(app, FakeRequest(POST, "/settings/update")
        .withFormUrlEncodedBody(("fields", "foobar"))
        .withSession(("username", "hold_on"))).get

      status(updateSettings) mustBe BAD_REQUEST
      contentType(updateSettings) mustBe Some("text/plain")
    }

    "not update the user settings when the fields' email is invalid" in {
      val updateSettings = route(app, FakeRequest(POST, "/settings/update")
        .withFormUrlEncodedBody(("fields", "{ \"email\": \"foobar\" }"))
        .withSession(("username", "hold_on"))).get

      status(updateSettings) mustBe BAD_REQUEST
      contentType(updateSettings) mustBe Some("text/plain")
    }

    "not update the user settings when the user session is invalid" in {
      val updateSettings = route(app, FakeRequest(POST, "/settings/update")
        .withFormUrlEncodedBody(("fields", "{}"))
        .withSession(("username", "nonexistinguserheresowecantupdatehissettings"))).get

      status(updateSettings) mustBe UNAUTHORIZED
      contentType(updateSettings) mustBe Some("text/plain")
    }

    "not update the user settings when there is no user session" in {
      val updateSettings = route(app, FakeRequest(POST, "/settings/update").withFormUrlEncodedBody(("fields", "{}"))).get

      status(updateSettings) mustBe UNAUTHORIZED
      contentType(updateSettings) mustBe Some("text/plain")
    }

    "not delete a user when the user session is invalid" in {
      val deleteUser = route(app, FakeRequest(POST, "/settings/delete")
        .withFormUrlEncodedBody(("foo", "bar"))
        .withSession(("username", "nonexistinguserheresowecantdeletehisaccount"))).get

      status(deleteUser) mustBe UNAUTHORIZED
      contentType(deleteUser) mustBe Some("text/plain")
    }

    "not delete a user when there is no user session" in {
      val deleteUser = route(app, FakeRequest(POST, "/settings/delete").withFormUrlEncodedBody(("foo", "bar"))).get

      status(deleteUser) mustBe UNAUTHORIZED
      contentType(deleteUser) mustBe Some("text/plain")
    }
  }

  "UserController" should {
    "render the login page" in {
      val login = route(app, FakeRequest(GET, "/login")).get

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include ("Login")
    }

    "redirect from /login to / when there is a user session" in {
      val login = route(app, FakeRequest(GET, "/login").withSession(("username", "hold_on"))).get

      status(login) mustBe 303
    }

    "log a user in when a valid username and password are provided and the user exists" in {
      val login = route(app, FakeRequest(POST, "/login")
        .withFormUrlEncodedBody(("username", "hold_on"), ("password", "password"))).get

      status(login) mustBe 303
    }

    "not log a user in when the user doesn't exist" in {
      val login = route(app, FakeRequest(POST, "/login")
        .withFormUrlEncodedBody(("username", "nonexistinguserheresowecantloghimin"), ("password", "password"))).get

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include ("A user with that username could not be found")
    }

    "not log a user in when the password is wrong" in {
      val login = route(app, FakeRequest(POST, "/login")
        .withFormUrlEncodedBody(("username", "hold_on"), ("password", "wrongpassword"))).get

      status(login) mustBe OK
      contentType(login) mustBe Some("text/html")
      contentAsString(login) must include ("Wrong password!")
    }

    "not log a user in when not all form fields are given" in {
      val login = route(app, FakeRequest(POST, "/login")
        .withFormUrlEncodedBody(("password", "password"))).get

      status(login) mustBe BAD_REQUEST
      contentType(login) mustBe Some("text/html")
    }

    "render the register page" in {
      val register = route(app, FakeRequest(GET, "/register")).get

      status(register) mustBe OK
      contentType(register) mustBe Some("text/html")
      contentAsString(register) must include ("Register")
    }

    "redirect from /register to / when there is a user session" in {
      val register = route(app, FakeRequest(GET, "/register").withSession(("username", "hold_on"))).get

      status(register) mustBe 303
    }

    "register a user when all form fields are given and the username isn't taken" in {
      val uniqueUsername = "test_user" + (System.currentTimeMillis / 1000).toString
      val register = route(app, FakeRequest(POST, "/register")
        .withFormUrlEncodedBody(
          ("username", uniqueUsername),
          ("email", "foo@bar"),
          ("firstName", "Foo"),
          ("lastName", "Bar"),
          ("password", "password")
        )).get

      status(register) mustBe 303
    }

    "not register a user when the username is already taken" in {
      val register = route(app, FakeRequest(POST, "/register")
        .withFormUrlEncodedBody(
          ("username", "hold_on"),
          ("email", "foo@bar"),
          ("firstName", "Foo"),
          ("lastName", "Bar"),
          ("password", "password")
        )).get

      status(register) mustBe OK
      contentType(register) mustBe Some("text/html")
      contentAsString(register) must include ("A user with that username already exists")
    }

    "not register a user when not all form fields are given" in {
      val register = route(app, FakeRequest(POST, "/register")
        .withFormUrlEncodedBody(("password", "password"))).get

      status(register) mustBe BAD_REQUEST
      contentType(register) mustBe Some("text/html")
    }

    "log a user out and render the logout page" in {
      val logout = route(app, FakeRequest(GET, "/logout")).get

      status(logout) mustBe OK
      contentType(logout) mustBe Some("text/html")
      contentAsString(logout) must include ("Successfully logged out")
    }
  }
}
