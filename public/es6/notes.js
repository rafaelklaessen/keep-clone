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
      <article class="note grid-item" style="background-color: ${escapeString(note.color.trim())}">
        ${title}
        ${content}
        <div class="note-action-container">
          <button class="material-icons edit-btn md-btn btn">edit</button>
          <button class="material-icons delete-btn md-btn btn">delete</button>
        </div>
      </article>
    `);

    $item
      .find('.delete-btn')
      .click(function() {
        const id = $(this).parents('.note').attr('id');

        Notes.deleteNote(id);
      });

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
    
    // Backend request would be put here
    $.post('/notes/delete', {id: id}, (data) => {
      console.info(data);
    }).fail((error) => {
      alert(`ERROR (${error.status}): ${error.responseText}`);
    });
  }
}