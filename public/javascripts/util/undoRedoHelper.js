/**
 * The undo button calls the undo function
 */
$('#undoButton').click(function () {
    if (!$('#undoButton').attr('disabled')) {
        undoRedo.undo();
    }
});

/**
 * The redo button calls the redo function
 */
$('#redoButton').click(function () {
    if (!$('#redoButton').attr('disabled')) {
        undoRedo.redo();
    }
});

/**
    * Updates the undo and redo buttons in the nav bar to either disabled if there
    * are no undos or redos on the stack or not disabled if the opposite.
    */
function updateUndoRedoButtons() {
    if (undoRedo.undoStack.isEmpty()) {
        $('#undoButton').attr("disabled", "true");
    } else {
        $('#undoButton').removeAttr("disabled");
    }

    if (undoRedo.redoStack.isEmpty()) {
        $('#redoButton').attr("disabled", "true");
    } else {
        $('#redoButton').removeAttr("disabled");
    }
}