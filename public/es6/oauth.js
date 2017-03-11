/**
 * The OAuth class contains all functionality related to logging user in with
 * OAuth.
 */
class OAuth {
  /**
   * OAuth.google()
   * Handles a Google signin
   * @param {object} googleUser
   */
  static google(googleUser) {
    const id_token = googleUser.getAuthResponse().id_token;

    $.post('/oauth/google', { id_token: id_token }, (data) => {
      console.info(data);
      // Go to /, because we're logged in now
      location.assign('/');
    }).fail((error) => {
      Notifier.alert(
        `Error ${error.status}`,
        `An error occured while trying to log you in with Google.
        <br>
        <strong>Error ${error.status}:</strong>
        <br>
        ${error.status == '404' ? 'Page not found' : escapeString(error.responseText)}`);
    });
  }
}
