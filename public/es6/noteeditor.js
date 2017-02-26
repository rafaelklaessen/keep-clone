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

    const $noteEditor = $(`
      <section class="overlay-black">
        <section id="edit-note" class="edit-note" data-color="${color}" style="background-color: ${color}">
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

    $noteEditor.appendTo('body');
  }

}