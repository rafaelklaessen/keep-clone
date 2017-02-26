'use strict';

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

// Get the note writer element
var $noteWriter = $('#write-note');

// Open the note writer when it's clicked
$noteWriter.find('.note-title').click(function () {
  NoteWriter.show();
});

// Open the note writer when an input's focused
$noteWriter.find('.note-title-input, .note-content-input').focus(function () {
  NoteWriter.show();
});

// Close the note writer when the cancel button is clicked
$noteWriter.find('.cancel-btn').click(function () {
  NoteWriter.close();
});

// Save note writer input when the save button is clicked
$noteWriter.find('.save-btn').click(function () {
  NoteWriter.save();
});

// Change note color when a color ball is clicked
$noteWriter.find('.color-ball').click(function () {
  var color = $(this).data('color');

  NoteWriter.setColor(color);
});

// Setup masonry
var $grid = $('.grid').masonry({
  itemSelector: '.grid-item',
  columnWidth: '.grid-sizer',
  percentPosition: true
});

// Run delete function when a note's delete button is clicked
$('.note .delete-btn').click(function () {
  var id = $(this).parents('.note').attr('id');

  Notes.deleteNote(id);
});

// Run edit function when a note's edit button is clicked
$('.note .edit-btn').click(function () {
  var id = $(this).parents('.note').attr('id');
  var note = Notes.getNote(id);

  NoteEditor.show(id, note);
});

var $settings = $('#settings');

// Edit settings
$settings.find('.edit-btn').click(function () {
  var $settingsField = $(this).parents('.settings-field');
  var $titleContent = $(this).siblings('.title-content');

  // Get the name of the field we're editing
  var name = $settingsField.data('name');

  // Check if the settings field has the editing class.
  // If it does, we save the edits. If it doesn't, we toggle editing
  // mode.
  if ($settingsField.hasClass('editing')) {
    var orgText = $titleContent.find('.field-input').data('orgtext').trim();
    var newText = $titleContent.find('.field-input').val().trim();

    // Only save edits to backend if there are actually edits
    if (orgText != newText) {
      // Do backend stuff here
      $.post('/settings/update', { fields: JSON.stringify(_defineProperty({}, name, newText)) }, function (data) {
        console.info(data);
      }).fail(function (error) {
        alert('ERROR (' + error.status + '): ' + error.responseText);
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
    var _orgText = $titleContent.text().trim();

    // Get the type of the input. Is always text except if we're editing
    // the password
    var type = $settingsField.hasClass('password-field') ? 'password' : 'text';

    // Change title content to an input
    $titleContent.html('\n      <input class="field-input input" type="' + type + '" value="' + _orgText + '" data-orgText="' + _orgText + '">\n    ');

    // Change edit button icon to done icon
    $(this).text('done');
  }

  $settingsField.toggleClass('editing');
});

// Account deletion
$settings.find('#delete-account-btn').click(function () {
  var confirmed = confirm('Are you sure you want to remove your account? This can\'t be undone!');

  if (confirmed) {
    // Request to backend would be put here
    console.info('Removing account... :(');
    $.post('/settings/delete', {}, function (data) {
      console.info(data);
      // Logout from Google as well
      var auth2 = gapi.auth2.getAuthInstance();
      auth2.signOut().then(function () {
        console.info('User signed out.');
      });
      // Redirect to homepage
      location.assign('/');
    }).fail(function (error) {
      alert('ERROR (' + error.status + '): ' + error.responseText);
    });
  }
});

/**
 * escapeString()
 * Escapes given string.
 * @param {string} str String to escape
 * @return {string} escaped string
 */
function escapeString(str) {
  return str.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/**
 * The NoteEditor class contains all functionality related to editing notes.
 */
var NoteEditor = function () {
  function NoteEditor() {
    _classCallCheck(this, NoteEditor);
  }

  _createClass(NoteEditor, null, [{
    key: 'show',

    /**
     * NoteEditor.show()
     * Adds note editor to DOM and shows it.
     * @param {string} id The id of the note we're editing.
     * @param {object} note The previous note.
     */
    value: function show(id, note) {
      // Make sure note exists
      if (!$('#' + id).length) {
        return;
      }

      var title = note.title || '';
      var content = note.content || '';
      var color = note.color || '#FFFFFF';

      // Generate HTML
      var $noteEditor = $('\n      <section class="overlay-black">\n        <section id="edit-note" class="edit-note" data-note="' + id + '" data-color="' + color + '" style="background-color: ' + color + '">\n          <header class="note-title-container">\n            <h4 class="note-title">\n              <input class="note-title-input" placeholder="Title" value="' + title + '">\n            </h4>\n          </header>\n          <section class="note-content">\n            <textarea class="note-content-input" placeholder="Take a note...">' + content + '</textarea>\n          </section>\n          <footer class="toolbar">\n            <section class="color-balls">\n              <span class="color-ball" style="background-color: #FFFFFF" data-color="#FFFFFF"></span>\n              <span class="color-ball" style="background-color: #80D8FF" data-color="#80D8FF"></span>\n              <span class="color-ball" style="background-color: #FFD180" data-color="#FFD180"></span>\n              <span class="color-ball" style="background-color: #B388FF" data-color="#B388FF"></span>\n              <span class="color-ball" style="background-color: #FF8A80" data-color="#FF8A80"></span>\n            </section>\n            <section class="btn-container">\n              <button class="cancel-btn md-btn btn">Cancel</button>\n              <button class="save-btn md-btn btn">Save</button>\n            </section>\n          </footer>\n        </section>\n      </section>\n    ');

      // Append HTML to body
      $noteEditor.appendTo('body');

      // Fade the note editor in
      $noteEditor.fadeIn(200);
      $noteEditor.find('#edit-note').fadeIn(400);

      // Add listeners
      $noteEditor.find('.cancel-btn').click(function () {
        NoteEditor.cancel();
      });

      $noteEditor.find('.save-btn').click(function () {
        NoteEditor.save();
      });

      $noteEditor.find('.color-ball').click(function () {
        var color = $(this).data('color');

        NoteEditor.setColor(color);
      });
    }

    /**
     * NoteEditor.cancel()
     * Cancels note editing.
     */

  }, {
    key: 'cancel',
    value: function cancel() {
      $('#edit-note').fadeOut(200).parent().fadeOut(400);

      setTimeout(function () {
        $('#edit-note').parent().remove();
      }, 400);
    }

    /**
     * NoteEditor.save()
     * Saves note edits.
     */

  }, {
    key: 'save',
    value: function save() {
      var $noteEditor = $('#edit-note');

      // Get note id
      var noteId = $noteEditor.data('note');
      var oldNote = Notes.getNote(noteId);
      var newNote = {
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
        }, function (data) {
          console.info(data);
        }).fail(function (error) {
          alert('ERROR (' + error.status + '): ' + error.responseText);
        });

        // Update DOM
        var $note = $('#' + noteId);
        var $noteTitle = $note.find('.note-title');
        var $noteContent = $note.find('.note-content');

        // Create elements if they don't exist yet
        if ($noteTitle.length) {
          $noteTitle.text(newNote.title);
        } else {
          $note.append('<h4 class="note-title">' + newNote.title + '</h4>');
        }

        if ($noteContent.length) {
          $noteContent.text(newNote.content);
        } else {
          $note.append('<p class="note-content">' + newNote.content + '</p>');
        }

        // Remove elements if they aren't required anymore
        if (!newNote.title) {
          $noteTitle.remove();
        }

        if (!newNote.content) {
          $noteContent.remove();
        }

        $note.css({ 'background-color': newNote.color });

        // Remove the note editor
        NoteEditor.cancel();
      }
    }

    /**
     * NoteEditor.setColor()
     * Sets the color of the note editor.
     * @param {string} color Color to set the note editor to.
     */

  }, {
    key: 'setColor',
    value: function setColor(color) {
      $('#edit-note').data('color', color).css({ 'background-color': color });
    }
  }]);

  return NoteEditor;
}();
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/**
 * The Notes class contains all functionality related to the notes
 */
var Notes = function () {
  function Notes() {
    _classCallCheck(this, Notes);
  }

  _createClass(Notes, null, [{
    key: 'addNote',

    /**
     * Notes.addNote()
     * Adds note to DOM and performs a request to the backend to save the note.
     * @param {object} note Note to add.
     */
    value: function addNote(note) {
      var title = '';

      if (note.title.trim()) {
        title = '<h4 class="note-title">' + escapeString(note.title.trim()) + '</h4>';
      }

      var content = '';

      if (note.content.trim()) {
        content = '<p class="note-content">' + escapeString(note.content.trim()) + '</p>';
      }

      var $item = $('\n      <article class="note grid-item" style="background-color: ' + escapeString(note.color.trim()) + '">\n        ' + title + '\n        ' + content + '\n        <div class="note-action-container">\n          <button class="material-icons edit-btn md-btn btn">edit</button>\n          <button class="material-icons delete-btn md-btn btn">delete</button>\n        </div>\n      </article>\n    ');

      $item.find('.delete-btn').click(function () {
        var id = $(this).parents('.note').attr('id');

        Notes.deleteNote(id);
      });

      $grid.prepend($item).masonry('prepended', $item);

      // Backend request would be put here
      console.log(note);

      for (var item in note) {
        if (note[item].trim() == '') {
          delete note[item];
        }
      }

      console.log(note);

      $.post('/notes', note, function (response) {
        // The response contains the ID of the item we've just added.
        // We'll have to get that ID and add it to the element.
        $item.attr('id', response);
      }).fail(function (error) {
        alert('ERROR (' + error.status + '): ' + error.responseText);
      });
    }

    /**
     * Notes.deleteNote()
     * Deletes note from DOM and performs request to the delete backend.
     * @param {number} id Id of the note to delete.
     */

  }, {
    key: 'deleteNote',
    value: function deleteNote(id) {
      var $toDelete = $('#' + id);

      $grid.masonry('remove', $toDelete).masonry('layout');

      // Backend request would be put here
      $.post('/notes/delete', { id: id }, function (data) {
        console.info(data);
      }).fail(function (error) {
        alert('ERROR (' + error.status + '): ' + error.responseText);
      });
    }

    /**
     * Notes.getNote()
     * Gets a note's data.
     * @param {string} id The id of the note to get.
     * @return {object} The note's data
     */

  }, {
    key: 'getNote',
    value: function getNote(id) {
      var $note = $('#' + id);

      return {
        title: $note.find('.note-title').text().trim() || '',
        content: $note.find('.note-content').text().trim() || '',
        color: $note.css('background-color').trim() || '#FFFFFF'
      };
    }
  }]);

  return Notes;
}();
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/**
 * The NoteWriter class contains all note writer related functionality
 */
var NoteWriter = function () {
  function NoteWriter() {
    _classCallCheck(this, NoteWriter);
  }

  _createClass(NoteWriter, null, [{
    key: 'show',

    /**
      * NoteWriter.show()
      * Shows the note writer.
      */
    value: function show() {
      $noteWriter.addClass('open');

      var $elementsToChange = $noteWriter.find('.note-content, .toolbar');
      var $noteContent = $elementsToChange.eq(0);
      var $toolbar = $elementsToChange.eq(1);

      $noteContent.css({ 'display': 'block' });
      $toolbar.css({ 'display': 'inline-block' });

      setTimeout(function () {
        $noteContent.css({ 'height': '50px', 'opacity': 1 });
        $toolbar.css({ 'height': '36px', 'opacity': 1 });
      }, 10);
    }

    /**
      * NoteWriter.close()
      * Closes the note writer and resets its input and color.
      */

  }, {
    key: 'close',
    value: function close() {
      $noteWriter.removeClass('open');

      // Close note writer
      var $elementsToChange = $noteWriter.find('.note-content, .toolbar');
      var $noteContent = $elementsToChange.eq(0);
      var $toolbar = $elementsToChange.eq(1);

      $elementsToChange.hide();
      $elementsToChange.css({ 'height': 0, 'opacity': 0 });

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

  }, {
    key: 'setColor',
    value: function setColor(color) {
      $noteWriter.data('color', color);
      $noteWriter.css({ 'background-color': color });
    }

    /**
     * NoteWriter.save()
     * Saves the note writer input
     */

  }, {
    key: 'save',
    value: function save() {
      var titleInput = $noteWriter.find('.note-title-input').val().trim();
      var contentInput = $noteWriter.find('.note-content-input').val().trim();
      var colorInput = $noteWriter.data('color').trim();

      // Only save the note if the title or content field is set
      if (titleInput || contentInput) {
        var input = {
          title: titleInput,
          content: contentInput,
          color: colorInput
        };

        Notes.addNote(input);

        console.info(input);
      }

      NoteWriter.close();
    }
  }]);

  return NoteWriter;
}();
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/**
 * The OAuth class contains all functionality related to logging user in with 
 * OAuth.
 */
var OAuth = function () {
  function OAuth() {
    _classCallCheck(this, OAuth);
  }

  _createClass(OAuth, null, [{
    key: 'google',

    /**
     * OAuth.google()
     * Handles a Google signin
     * @param {object} googleUser
     */
    value: function google(googleUser) {
      var id_token = googleUser.getAuthResponse().id_token;

      $.post('/oauth/google', { id_token: id_token }, function (data) {
        console.info(data);
        // Go to /, because we're logged in now
        location.assign('/');
      }).fail(function (error) {
        alert('ERROR (' + error.status + '): ' + error.responseText);
      });
    }
  }]);

  return OAuth;
}();
