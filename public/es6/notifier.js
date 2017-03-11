/**
 * The Notifier class contains methods for sending notifications (including alerts etc).
 */
class Notifier {
  /**
   * Notifier.toast()
   * Sends a material design toast.
   * @param {string} msg The toast message.
   */
  static toast(msg) {
    const $toast = $(`
      <article class="toast">
        <p class="content">
          ${msg}
        </p>
        <button class="material-icons close-btn md-btn btn">close</button>
      </article>
    `);

    $toast.appendTo('#toast-container');

    // Add activated class after a little delay because otherwise the animation
    // won't work
    setTimeout(() => {
      $toast.addClass('activated');
    }, 10)

    $toast.find('.close-btn').click(function() {
      $toast.removeClass('activated');
      setTimeout(() => {
        $toast.remove();
      }, 200);
    });

    setTimeout(() => {
      $toast.removeClass('activated');
      setTimeout(() => {
        $toast.remove();
      }, 200);
    }, 5200);
  }

  /**
   * Notifier.alert()
   * Sends an alert.
   * @param {string} title The title of the alert.
   * @param {string} content The content of the alert.
   */
  static alert(title, content) {
    // Generate HTML
    const $alert = $(`
      <section class="overlay-black">
        <section class="alert popup">
          <button class="material-icons close-btn md-btn btn">close</button>
          <header class="popup-title-container">
            <h1 class="popup-title">${title}</h1>
          </header>
          <p class="popup-content">
            ${content}
          </p>
          <footer class="popup-footer">
            <section class="btn-container">
              <button class="ok-btn md-btn btn">OK</button>
            </section>
          </footer>
        </section>
      </section>
    `);

    // Append HTML to body
    $alert.appendTo('body');

    // Show the alert
    $alert.fadeIn(200);
    $alert.find('.popup').fadeIn(400);

    // Add closing listener to ok and close button
    $alert.find('.close-btn, .ok-btn').click(function() {
      $alert.find('popup').fadeOut(200);
      $alert.fadeOut(400);
      setTimeout(() => {
        $alert.remove();
      }, 400);
    });
  }

  /**
   * Notifier.confirm()
   * Sends a confirm.
   * @param {string} title The title of the confirm.
   * @param {string} content The content of the confirm.
   * @param {object} btnHandlers Handlers for the ok and cancel button
   */
  static confirm(title, content, btnHandlers) {
    // Generate HTML
    const $confirm = $(`
      <section class="overlay-black">
        <section class="confirm popup">
          <button class="material-icons close-btn md-btn btn">close</button>
          <header class="popup-title-container">
            <h1 class="popup-title">${title}</h1>
          </header>
          <p class="popup-content">
            ${content}
          </p>
          <footer class="popup-footer">
            <section class="btn-container">
              <button class="cancel-btn md-btn btn">Cancel</button>
              <button class="ok-btn md-btn btn">OK</button>
            </section>
          </footer>
        </section>
      </section>
    `);

    // Append HTML to body
    $confirm.appendTo('body');

    // Show the alert
    $confirm.fadeIn(200);
    $confirm.find('.popup').fadeIn(400);

    // Add closing listener to ok and close button
    $confirm.find('.close-btn, .cancel-btn, .ok-btn').click(function() {
      // Call correct button handler if it exists
      if ($(this).hasClass('ok-btn')) {
        if (typeof btnHandlers.ok == 'function') {
          btnHandlers.ok();
        }
      } else {
        if (typeof btnHandlers.cancel == 'function') {
          btnHandlers.cancel();
        }
      }

      $confirm.find('popup').fadeOut(200);
      $confirm.fadeOut(400);
      setTimeout(() => {
        $confirm.remove();
      }, 400);
    });

  }

}
