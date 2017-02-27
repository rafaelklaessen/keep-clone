/**
 * The NoteEditor class contains all functionality related to editing notes.
 */
class NoteEditor {
  /**
   * NoteEditor.show()
   * Adds note editor to DOM and shows it.
   * @param {string} id The id of the note we're editing.
   * @param {object} note The previous note.
   */
  static show(id, note) {
    // Make sure note exists
    if (!$(`#${id}`).length) {
      return;
    }

    const title = note.title || '';
    const content = note.content || '';
    const color = note.color || '#FFFFFF';

    // Generate HTML
    const $noteEditor = $(`
      <section class="overlay-black">
        <section id="edit-note" class="edit-note popup" data-note="${id}" data-color="${color}" style="background-color: ${color}">
          <header class="note-title-container">
            <h4 class="note-title">
              <input class="note-title-input" placeholder="Title" value="${title}">
            </h4>
          </header>
          <section class="note-content">
            <textarea class="note-content-input" placeholder="Take a note...">${content}</textarea>
          </section>
          <footer class="toolbar">
            <section class="color-balls">
              <span class="color-ball" style="background-color: #FFFFFF" data-color="#FFFFFF"></span>
              <span class="color-ball" style="background-color: #80D8FF" data-color="#80D8FF"></span>
              <span class="color-ball" style="background-color: #FFD180" data-color="#FFD180"></span>
              <span class="color-ball" style="background-color: #B388FF" data-color="#B388FF"></span>
              <span class="color-ball" style="background-color: #FF8A80" data-color="#FF8A80"></span>
            </section>
            <section class="btn-container">
              <button class="cancel-btn md-btn btn">Cancel</button>
              <button class="save-btn md-btn btn">Save</button>
            </section>
          </footer>
        </section>
      </section>
    `);

    // Append HTML to body
    $noteEditor.appendTo('body');

    // Fade the note editor in
    $noteEditor.fadeIn(200);
    $noteEditor.find('#edit-note').fadeIn(400);

    // Add listeners
    $noteEditor.find('.cancel-btn').click(function() {
      NoteEditor.cancel();
    });

    $noteEditor.find('.save-btn').click(function() {
      NoteEditor.save();
    });

    $noteEditor.find('.color-ball').click(function() {
      const color = $(this).data('color');
      
      NoteEditor.setColor(color);
    });
  }

  /**
   * NoteEditor.cancel()
   * Cancels note editing.
   */
  static cancel() {
    $('#edit-note')
      .fadeOut(200)
      .parent()
      .fadeOut(400);

    setTimeout(() => {
      $('#edit-note').parent().remove();
    }, 400);
  }

  /**
   * NoteEditor.save()
   * Saves note edits.
   */
  static save() {
    const $noteEditor = $('#edit-note');

    // Get note id
    const noteId = $noteEditor.data('note');
    const oldNote = Notes.getNote(noteId);
    const newNote = {
      title: $noteEditor.find('.note-title-input').val().trim() || '',
      content: $noteEditor.find('.note-content-input').val().trim() || '',
      color: $noteEditor.data('color').trim() || '#FFFFFF'
    };

    // Make sure that either the note title or note content is given (or both)
    if (newNote.title == '' && newNote.content == '') {
      NoteEditor.cancel();
      return;
    }

    // Only update the database when there actually are changes
    if (oldNote.title == newNote.title && oldNote.content == newNote.content && oldNote.color == newNote.color) {
      NoteEditor.cancel();
    } else {
      // Perform request to backend
      $.post('/notes/update', {
          id: noteId,
          fields: JSON.stringify(newNote)
        },
        (data) => {
          console.info(data);
      }).fail((error) => {
        alert(`ERROR (${error.status}): ${error.responseText}`);
      });

      // Update DOM
      const $note = $(`#${noteId}`);
      const $noteTitle = $note.find('.note-title');
      const $noteContent = $note.find('.note-content');

      // Create elements if they don't exist yet. Only do this if the field
      // is actually set. If it isn't, remove the element.
      if (newNote.title) {
        if ($noteTitle.length) {
          $noteTitle.text(newNote.title);
        } else {
          $note.prepend(`<h4 class="note-title">${newNote.title}</h4>`);
        }
      } else {
        $noteTitle.remove();
      }

      if (newNote.content) {
        if ($noteContent.length) {
          $noteContent.text(newNote.content);
        } else {
          $note.append(`<p class="note-content">${newNote.content}</p>`);
        }
      } else {
        $noteContent.remove();
      }

      $note.css({'background-color': newNote.color});

      // Remove the note editor
      NoteEditor.cancel();
    }
  }

  /**
   * NoteEditor.setColor()
   * Sets the color of the note editor.
   * @param {string} color Color to set the note editor to.
   */
  static setColor(color) {
    $('#edit-note')
      .data('color', color)
      .css({'background-color': color});
  }
}