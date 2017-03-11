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
          <h4 class="note-owner-username">@<span class="username">${owners[i]}</span></h4>
          <button class="material-icons remove-owner-btn md-btn btn">remove_circle</button>
        </li>
      `;
    }

    // Generate popup HTML
    const $noteSharing = $(`
      <section class="overlay-black">
        <section id="note-sharing" class="note-sharing popup" data-note="${id}">
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

    // Set data-owners
    $noteSharing.find('#note-sharing').data('owners', owners);

    // Fade the note sharing popup in
    $noteSharing.fadeIn(200);
    $noteSharing.find('.popup').fadeIn(400);

    // Add listeners
    $noteSharing.find('.username-input').on('keyup keydown click input propertychange', function() {
      // Add valid class if there's a username given (in order to show the
      // check icon).
      if ($(this).val().length > 0) {
        $(this).addClass('valid');
      } else {
        $(this).removeClass('valid');
      }
    });

    $noteSharing.find('.save-owner-btn').click(function() {
      NoteSharing.addOwner();
    });

    $noteSharing.find('.remove-owner-btn').click(function() {
      NoteSharing.removeOwner($(this).parent());
    });

    $noteSharing.find('.done-btn').click(function() {
      NoteSharing.done();
    });
  }

  /**
   * NoteSharing.addOwner()
   * Adds owner to note & updates DOM.
   */
  static addOwner() {
    const $noteSharing = $('#note-sharing');
    const $usernameInput = $noteSharing.find('.username-input');
    const username = $usernameInput.val();

    // Make sure the given username is valid
    if ($usernameInput.hasClass('valid') && username.length > 0) {
      // If user is already added, just clear the input
      if ($noteSharing.data('owners').indexOf(username) >= 0) {
        $usernameInput.val('');
        $usernameInput.removeClass('valid');
      } else {
        // Get note ID
        const id = $noteSharing.data('note');

        // Perform request to backend
        $.post('/notes/addowner', {
          id: id,
          owner: username
          },
          (data) => {
            // Update DOM after request is successfully done
            $(`
                <li class="note-owner">
                  <h4 class="note-owner-username">@<span class="username">${username}</span></h4>
                  <button class="material-icons delete-owner-btn md-btn btn" onclick="NoteSharing.removeOwner($(this).parent());">remove_circle</button>
                </li>
              `)
              .insertBefore($noteSharing.find('.add-note-owner'));

            // Add new owner to DOM
            const $note = $(`#${id}`);
            $note.data('owners').push(username);

            // Clear input
            $usernameInput.val('');
            $usernameInput.removeClass('valid');
        }).fail((error) => {
          alert(`ERROR (${error.status}): ${error.responseText}`);
        });
      }
    }
  }

  /**
   * NoteSharing.removeOwner()
   * Removes owner from note & updates DOM.
   * @param {object} noteOwnerEl The note-owner element.
   */
  static removeOwner(noteOwnerEl) {
    // Transform note owner element to a jQuery object
    const $noteOwner = $(noteOwnerEl);

    const $noteSharing = $('#note-sharing');
    // Get note ID
    const id = $noteSharing.data('note');
    // Get username of the owner to remove
    const username = $noteOwner.find('.username').text().trim();

    // Perform request to backend
    $.post('/notes/removeowner', {
      id: id,
      owner: username
      },
      (data) => {
        // Update DOM after request is successfully done
        // Remove note owner element
        $noteOwner.slideUp(200);
        setTimeout(() => {
          $noteOwner.remove();
        }, 200);

        // Remove owner from note & sharing popup DOM
        const $note = $(`#${id}`);
        const noteUsernameIndex = $note.data('owners').indexOf(username);
        const sharingUsernameIndex = $noteSharing.data('owners').indexOf(username);

        if (noteUsernameIndex >= 0 && sharingUsernameIndex >= 0) {
          $note.data('owners').splice(noteUsernameIndex, 1)
          $noteSharing.data('owners').splice(sharingUsernameIndex, 1);
        } else {
          console.warn('Couldn\'t remove owner from DOM.');
        }
    }).fail((error) => {
      alert(`ERROR (${error.status}): ${error.responseText}`);
    });
  }

  /**
   * NoteSharing.done()
   * Closes the sharing popup.
   */
  static done() {
    $('#note-sharing')
      .fadeOut(200)
      .parent()
      .fadeOut(400);

    setTimeout(() => {
      $('#note-sharing').parent().remove();
    }, 400);
  }
}
