// Get the note writer element
const $noteWriter = $('#write-note');

// Open the note writer when it's clicked
$noteWriter.find('.note-title').click(function() {
  NoteWriter.show();
});

// Open the note writer when an input's focused
$noteWriter.find('.note-title-input, .note-content-input').focus(function() {
  NoteWriter.show();
});

// Close the note writer when the cancel button is clicked
$noteWriter.find('.cancel-btn').click(function() {
  NoteWriter.close();
});

// Save note writer input when the save button is clicked
$noteWriter.find('.save-btn').click(function() {
  NoteWriter.save();
});

// Change note color when a color ball is clicked
$noteWriter.find('.color-ball').click(function() {
  const color = $(this).data('color');

  NoteWriter.setColor(color);
});

/**
 * The NoteWriter class contains all note writer related functionality
 */
class NoteWriter {
  /**
    * NoteWriter.show()
    * Shows the note writer.
    */
  static show() {
    $noteWriter.addClass('open');

    const $elementsToChange = $noteWriter.find('.note-content, .toolbar');
    const $noteContent = $elementsToChange.eq(0);
    const $toolbar = $elementsToChange.eq(1);

    $noteContent.css({'display': 'block'});
    $toolbar.css({'display': 'inline-block'});

    setTimeout(() => {
      $noteContent.css({'height': '50px', 'opacity': 1});
      $toolbar.css({'height': '36px', 'opacity': 1});
    }, 10);
  }

  /**
    * NoteWriter.close()
    * Closes the note writer and resets its input and color.
    */
  static close() {
    $noteWriter.removeClass('open');

    // Close note writer
    const $elementsToChange = $noteWriter.find('.note-content, .toolbar');
    const $noteContent = $elementsToChange.eq(0);
    const $toolbar = $elementsToChange.eq(1);
  
    $elementsToChange.hide();
    $elementsToChange.css({'height': 0, 'opacity': 0});

    // Reset input
    $noteWriter.find('.note-title-input, .note-content-input').val('');
  
    // Reset color
    NoteWriter.setColor('#FFFFFF');
  }

  /**
   * NoteWriter.setColor()
   * Changes the color of the note writer.
   * @param {string} color Color to change to
   */
  static setColor(color) {
    $noteWriter.data('color', color);
    $noteWriter.css({'background-color': color});
  }

  /**
   * NoteWriter.save()
   * Saves the note writer input
   */
  static save() {
    const titleInput = $noteWriter.find('.note-title-input').val().trim();
    const contentInput = $noteWriter.find('.note-content-input').val().trim();
    const colorInput = $noteWriter.data('color').trim();

    // Only save the note if the title or content field is set
    if (titleInput || contentInput) {
      const input = {
        title: titleInput,
        content: contentInput,
        color: colorInput
      }

      Notes.addNote(input);

      console.info(input);
    }

    NoteWriter.close();
  }
}

// Setup masonry
const $grid = $('.grid').masonry({
  itemSelector: '.grid-item',
  columnWidth: '.grid-sizer',
  percentPosition: true
});

// Run delete function when a note's delete button is clicked
$('.note .delete-btn').click(function() {
  const id = $(this).parents('.note').attr('id');

  Notes.deleteNote(id);
});

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
      title = `<h4 class="note-title">${note.title.trim()}</h4>`;
    }

    let content = '';

    if (note.content.trim()) {
      content = `<p class="note-content">${note.content.trim()}</p>`;
    }

    const $item = $(`
      <article class="note grid-item" style="background-color: ${note.color.trim()}">
        ${title}
        ${content}
        <button class="material-icons delete-btn md-btn btn">delete</button>
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

const $settings = $('#settings');

// Edit settings
$settings.find('.edit-btn').click(function() {
  const $settingsField = $(this).parents('.settings-field');
  const $titleContent = $(this).siblings('.title-content');

  // Get the name of the field we're editing
  const name = $settingsField.data('name');

  // Check if the settings field has the editing class.
  // If it does, we save the edits. If it doesn't, we toggle editing
  // mode.
  if ($settingsField.hasClass('editing')) {
    const orgText = $titleContent.find('.field-input').data('orgtext').trim();
    let newText = $titleContent.find('.field-input').val().trim();

    // Only save edits to backend if there are actually edits
    if (orgText != newText) {
      // Do backend stuff here
      console.log(name, newText);
    }

    // If the field we're editing is the password field, insert
    // • instead of text.
    if ($settingsField.hasClass('password-field')) {
      newText = '&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;';
    }

    // Remove input
    $titleContent.html(newText);
    // Change edit button icon back to edit icon
    $(this).text('edit');
  } else { 
    // Original setting content
    const orgText = $titleContent.text().trim();
 
    // Get the type of the input. Is always text except if we're editing
    // the password
    const type = $settingsField.hasClass('password-field') ? 'password' : 'text';

    // Change title content to an input
    $titleContent.html(`
      <input class="field-input input" type="${type}" value="${orgText}" data-orgText="${orgText}">
    `);

    // Change edit button icon to done icon
    $(this).text('done');
  }

  $settingsField.toggleClass('editing');
});