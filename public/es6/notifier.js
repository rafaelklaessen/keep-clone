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

    $toast
      .appendTo('#toast-container')
      .show(200);

    $toast.find('.close-btn').click(function() {
      $toast.hide(200);
      setTimeout(() => {
        $toast.remove();
      }, 200);
    });

    setTimeout(() => {
      $toast.hide(200);
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
  static alert(title, content) {}

  /**
   * Notifier.confirm()
   * Sends a confirm.
   * @param {string} title The title of the confirm.
   * @param {string} content The content of the confirm.
   * @param {object} btnHandlers Handlers for the ok and cancel button
   */
  static confirm(title, content, btnHandlers) {}

}
