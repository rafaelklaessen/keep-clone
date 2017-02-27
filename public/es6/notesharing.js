/**
 * The NoteSharing class contains all functionality related to sharing notes.
 * (Which is adding/removing owners from it).
 */
class NoteSharing {
  /**
   * NoteSharing.show()
   * Adds note sharing popup to DOM and shows it.
   * @param {string} id Id of the note that was clicked.
   * @param {Array} owners Owners of the note that was clicked (got from data-note).
   */
  static show(id, owners) {
    // Make sure note exists
    if (!$(`#${id}`).length) {
      return;
    }

    // Make sure the owners are actually given as an array
    if (!Array.isArray(owners)) {
      return;
    }

    // Generate owner list
    let ownerList = '';

    for (let i = 0; i < owners.length; i++) {
      ownerList += `
        <li class="note-owner">
          <h4 class="note-owner-username">@${owners[i]}</h4>
          <button class="material-icons delete-owner-btn md-btn btn">remove_circle</button>
        </li>
      `;
    }

    // Generate popup HTML
    const $noteSharing = $(`
      <section class="overlay-black">
        <section id="note-sharing" class="note-sharing popup" data-note="${id}" data-owners='${owners}'>
          <header class="popup-title-container">
            <h2 class="popup-title">Sharing</h2>
          </header>
          <section class="popup-content">
            <ul class="note-owners">
              ${ownerList}
              <li class="add-note-owner note-owner">
                <input class="username-input" placeholder="Username of the user to share with">
                <button class="material-icons save-owner-btn md-btn btn">check</button>
              </li>  
            </ul>
          </section>
          <footer class="popup-footer">
            <section class="btn-container">
              <button class="done-btn md-btn btn">Done</button>
            </section>
          </footer>
        </section>
      </section>
    `);

    // Append HTML to body 
    $noteSharing.appendTo('body');

    // Fade the note sharing popup adding
    $noteSharing.fadeIn(200);
    $noteSharing.find('.popup').fadeIn(400);

    // Add listeners
    $noteSharign.find('.done-btn').click(function() {
      NoteSharing.done();
    });
  }
}