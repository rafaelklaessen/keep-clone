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