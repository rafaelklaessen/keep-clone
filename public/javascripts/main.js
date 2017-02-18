'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

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
        title = '<h4 class="note-title">' + note.title.trim() + '</h4>';
      }

      var content = '';

      if (note.content.trim()) {
        content = '<p class="note-content">' + note.content.trim() + '</p>';
      }

      var $item = $('\n      <article id="9" class="note grid-item" style="background-color: ' + note.color.trim() + '">\n        ' + title + '\n        ' + content + '\n        <button class="material-icons delete-btn md-btn btn">delete</button>\n      </article>\n    ');

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

      $.post('/notes', note, function (data) {
        console.info(data);
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
  }]);

  return Notes;
}();
