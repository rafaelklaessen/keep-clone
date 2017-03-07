/**
 * The Notes class contains all functionality related to the notes
 */
class Notes {
  /**
   * Notes.addNote()
   * Adds note to DOM and performs a request to the backend to save the note.
   * @param {object} note Note to add.
   */
  static addNote(note) {
    let title = '';

    if (note.title.trim()) {
      title = `<h4 class="note-title">${escapeString(note.title.trim())}</h4>`;
    }

    let content = '';

    if (note.content.trim()) {
      content = `<p class="note-content">${escapeString(note.content.trim())}</p>`;
    }

    const $item = $(`
      <article class="note grid-item" style="background-color: ${escapeString(note.color.trim())}" data-pinned="false">
        ${title}
        ${content}
        <button class="pin-btn md-btn btn">
          <img class="icon" src="assets/images/icons/pin.png" alt="Pin icon">
        </button>
        <div class="note-action-container">
          <button class="material-icons share-btn md-btn btn">person_add</button>
          <button class="material-icons edit-btn md-btn btn">edit</button>
          <button class="material-icons delete-btn md-btn btn">delete</button>
        </div>
      </article>
    `);

    // Add listeners
    $item
      .find('.delete-btn')
      .click(function() {
        const id = $(this).parents('.note').attr('id');

        Notes.deleteNote(id);
      })
      .siblings('.edit-btn')
      .click(function() {
        const id = $(this).parents('.note').attr('id');
        const note = Notes.getNote(id);

        NoteEditor.show(id, note);
      })
      .siblings('.share-btn')
      .click(function() {
        const $note = $(this).parents('.note');
        const id = $note.attr('id');
        const owners = $note.data('owners');

        NoteSharing.show(id, owners);
      })
      .parents('.note')
      .find('.pin-btn')
      .click(function() {
        const id = $(this).parents('.note').attr('id');

        Notes.togglePin(id);
      });

    // Set data-owners
    $item.data('owners', [JSON.stringify(sessionUser)]);

    $grid
      .prepend($item)
      .masonry('prepended', $item);

    // Backend request would be put here
    console.log(note);
    
    for (let item in note) {
      if (note[item].trim() == '') {
        delete note[item];
      }
    }

    console.log(note);

    $.post('/notes', note, (response) => {
      // The response contains the ID of the item we've just added.
      // We'll have to get that ID and add it to the element.
      $item.attr('id', response)
    }).fail((error) => {
      alert(`ERROR (${error.status}): ${error.responseText}`);
    });
  }

  /**
   * Notes.deleteNote()
   * Deletes note from DOM and performs request to the delete backend.
   * @param {number} id Id of the note to delete.
   */
  static deleteNote(id) {
    const $toDelete = $(`#${id}`);
    
    $grid
      .masonry('remove', $toDelete)
      .masonry('layout');

    $pinnedGrid
      .masonry('remove', $toDelete)
      .masonry('layout');
    
    // Backend request would be put here
    $.post('/notes/delete', {id: id}, (data) => {
      console.info(data);
    }).fail((error) => {
      alert(`ERROR (${error.status}): ${error.responseText}`);
    });

    // Update titles
    setTimeout(() => {
      Notes.updateTitles();
    }, 500);
  }

  /**
   * Notes.getNote()
   * Gets a note's data.
   * @param {string} id The id of the note to get.
   * @return {object} The note's data
   */
  static getNote(id) {
    const $note = $(`#${id}`);

    return {
      title: $note.find('.note-title').text().trim() || '',
      content: $note.find('.note-content').text().trim() || '',
      color: $note.css('background-color').trim() || '#FFFFFF'
    };
  }

  /**
   * Notes.togglePin()
   * Toggles given note's pin state & updates DOM.
   * @param {string} id The id of the note to pin.
   */
  static togglePin(id) {
    const $note = $(`#${id}`);
    const $noteClone = $note.clone();

    // Add listeners
    $noteClone
      .find('.delete-btn')
      .click(function() {
        const id = $(this).parents('.note').attr('id');

        Notes.deleteNote(id);
      })
      .siblings('.edit-btn')
      .click(function() {
        const id = $(this).parents('.note').attr('id');
        const note = Notes.getNote(id);

        NoteEditor.show(id, note);
      })
      .siblings('.share-btn')
      .click(function() {
        const $note = $(this).parents('.note');
        const id = $note.attr('id');
        const owners = $note.data('owners');

        NoteSharing.show(id, owners);
      })
      .parents('.note')
      .find('.pin-btn')
      .click(function() {
        const id = $note.attr('id');

        Notes.togglePin(id);
      });    

    if ($note.data('pinned')) {
      $grid
        .prepend($noteClone)
        .masonry('prepended', $noteClone);

      $pinnedGrid
        .masonry('remove', $note)
        .masonry('layout');

      $note.find('.pin-btn .icon').attr('src', 'assets/images/icons/pin.png');
      $noteClone.find('.pin-btn .icon').attr('src', 'assets/images/icons/pin.png');
    } else {
      $pinnedGrid
        .prepend($noteClone)
        .masonry('prepended', $noteClone)
        .masonry('layout')

      $grid
        .masonry('remove', $note)
        .masonry('layout');

      $note.find('.pin-btn .icon').attr('src', 'assets/images/icons/pin-blue.png');
      $noteClone.find('.pin-btn .icon').attr('src', 'assets/images/icons/pin-blue.png');
    }

    // Toggle data-pinned
    $note.data('pinned', !$note.data('pinned'));
    $noteClone.data('pinned', !$noteClone.data('pinned'));

    // Perform request to backend
    $.post('/notes/setpinned', {
      id: id,
      pinned: $noteClone.data('pinned')
    }, (data) => {
      console.info(data);
    }).fail((error) => {
      alert(`ERROR (${error.status}): ${error.responseText}`);
    });

    // Update titles
    setTimeout(() => {
      Notes.updateTitles();
    }, 500);
  }

  /**
   * Notes.updateTitles()
   * Shows/hides titles above a note section.
   */
  static updateTitles() {
    const $notes = $('#notes .note');
    const $pinnedNotes = $('#pinned-notes .note');

    if ($notes.length) {
      $('.other-notes-title').removeClass('deactivated');
    } else {
      $('.other-notes-title').addClass('deactivated');
    }

    if ($pinnedNotes.length) {
      $('.pinned-notes-title').removeClass('deactivated');
    } else {
      $('.pinned-notes-title').addClass('deactivated');
      $('.other-notes-title').addClass('deactivated');
    }
  }
}

// Make sure titles are showing correctly
Notes.updateTitles();