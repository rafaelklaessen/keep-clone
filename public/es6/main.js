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

      console.info(input);
    }

    NoteWriter.close();
  }
}

// Setup masonry
$('.grid').masonry({
  itemSelector: '.grid-item',
  columnWidth: '.grid-sizer',
  percentPosition: true
});