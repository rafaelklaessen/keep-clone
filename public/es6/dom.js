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

// Setup masonry
const $grid = $('#notes').masonry({
  itemSelector: '.grid-item',
  columnWidth: '.grid-sizer',
  percentPosition: true
});

const $pinnedGrid = $('#pinned-notes').masonry({
  itemSelector: '.grid-item',
  columnWidth: '.grid-sizer',
  percentPosition: true
});

// If the window size changes, the note width might have updated (which
// requires the masonry to layout again)
const $window = $(window);

$window.resize(function() {
  $grid.masonry('layout');
  $pinnedGrid.masonry('layout');
});

let windowWidth = $window.width();
setInterval(function() {
  if (windowWidth != $window.width()) {
    $grid.masonry('layout');
    $pinnedGrid.masonry('layout');

    NoteWriter.fixButtons();

    windowWidth = $window.width();
  }
}, 300);

// Run pin function when a note's pin button is clicked
$('.note .pin-btn').click(function() {
  const id = $(this).parents('.note').attr('id');

  Notes.togglePin(id);
});

// Run delete function when a note's delete button is clicked
$('.note .delete-btn').click(function() {
  const id = $(this).parents('.note').attr('id');

  Notes.deleteNote(id);
});

// Run edit function when a note's edit button is clicked
$('.note .edit-btn').click(function() {
  const id = $(this).parents('.note').attr('id');
  const note = Notes.getNote(id);

  NoteEditor.show(id, note);
});

// Run sharing function when a note's sharing button is clicked
$('.note .share-btn').click(function() {
  const $note = $(this).parents('.note');
  const id = $note.attr('id');
  const owners = $note.data('owners');

  NoteSharing.show(id, owners);
});

// Run archiving/unarchiving function when a note's archive button is clicked
$('.note .archive-btn').click(function() {
  const $note = $(this).parents('.note');
  const id = $note.attr('id');

  Notes.toggleArchived(id);
});

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
      // Perform request to backend
      $.post('/settings/update',
        { fields: JSON.stringify({ [name]: newText }) },
        (data) => {
          console.info(data);
          Notifier.toast('Successfully updated settings');
      }).fail((error) => {
        Notifier.alert(
          `Error ${error.status}`,
          `An error occured while trying to update your settings.
          <br>
          <strong>Error ${error.status}:</strong>
          <br>
          ${error.status == '404' ? 'Page not found' : escapeString(error.responseText)}`);
      });
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

// Account deletion
$settings.find('#delete-account-btn').click(function() {
    const btnHandlers = {
    ok: () => {
      // Perform request to backend
      console.info('Removing account... :(');
      $.post('/settings/delete', {}, (data) => {
        console.info(data);
        // Logout from Google as well
        const auth2 = gapi.auth2.getAuthInstance();
        auth2.signOut().then(() => {
          console.info('User signed out.');
        });
        // Redirect to homepage
        location.assign('/');
      }).fail((error) => {
        Notifier.alert(
          `Error ${error.status}`,
          `An error occured while trying to delete your account.
          <br>
          <strong>Error ${error.status}:</strong>
          <br>
          ${error.status == '404' ? 'Page not found' : escapeString(error.responseText)}`);
      });
    }
  }

  Notifier.confirm(
    'Are you sure?',
    'Are you sure you want to delete your account? There is no way back!',
    btnHandlers);
});

/**
 * escapeString()
 * Escapes given string.
 * @param {string} str String to escape
 * @return {string} escaped string
 */
function escapeString(str) {
  return str
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
