let tagPickerSuggestions = [];
let suggestion = "";
$('#tagsInput').on('keydown', keyDown);
$('#tagsInput').on('keyup', keyUp);


/**
* Called when key is pressed
*/
function keyDown(e) {
    // Need to keep depreciated symbols for older browsers
    const key = e.which || e.keyCode || e.key;
    const tag = $('#tags input').val().replace(/['"]+/g, '');
    const natTag = $('#tags input').val();
    const speechCount = natTag.length - tag.length;

    // If key pressed is tab, enter or space
    if ((key === 13 || key === 9 || key === 32) && tag !== "") {
        // If key pressed is space or enter to use typed tag
        if ((key === 13 || key === 32) && speechCount !== 1) {
            e.preventDefault();
            insertTag(tag);
        }
        // If key pressed is tab to use suggested tag
        else if (key === 9 && suggestion !== "") {
            e.preventDefault();
            insertTag(suggestion);
            suggestion = "";
            $('#backText').html(suggestion);
        }
    // If key is delete
    } else if (key === 8 && natTag === "") {
        const spacer = $("<li></li>").text("fillerfillerfil").attr('class', 'spacer');
        //Remove spacer, remove last tag, append spacer
        $('li').remove('.spacer');
        $('#tags ul li:last-child').remove();
        $('#tags ul').append(spacer);
    // if the key is space and the string is empty do nothing
    // or if the key is a speech mark and there is already two speech marks do nothing
    // of if the speech count is greater or equal to two do nothing
    } else if ((key === 32 && tag === "") || (natTag !== "" && key === 222 && speechCount === 0) || speechCount >= 2) {
        e.preventDefault();
    }
}

/**
* Called when key is released
*/
function keyUp() {
    const tag = $('#tags input').val();
    if (tag.replace('"', "") === "") {
        suggestion = "";
        $('#backText').html(suggestion);
    } else {
        const sugTag = searchTags(tag);
        $('#backText').html(sugTag);
    }
}

/**
 * Inserts a tag with given name into the tag input field
 * @param tagName - Name of tag to be inserted
 */
function insertTag(tagName) {
    // Creates tag element and adds close button to tag
    const tagEl = $("<li></li>").text(tagName);
    const closeBtn = $("<button></button>").attr('class', 'close-icon').click(deleteTag);
    tagEl.append(closeBtn);

    const tagsInput = $("#tags ul");
    // Inserts new tag between last current tag and spacer
    const spacer = $("<li></li>").text("fillerfillerfil").attr('class', 'spacer');
    $('li').remove('.spacer');
    tagsInput.append(tagEl);
    tagsInput.append(spacer);

    // Clears input
    $('#tags input').val("");

    tagsInput.scrollLeft(5000);
}

/**
 * Populates tag input with a list of tags
 * @param tags
 */
function populateTags(tags) {
    for (const tag of tags) {
        insertTag(tag.name);
    }
}

/**
* Searches suggested tags for matches and returns closest match
*
* @param {String} string string to match
*/
function searchTags(string) {
    for (const tag of tagPickerSuggestions) {
        const lowerTag = tag.toLowerCase().replace(/['"]+/g, '');
        const lowerString = string.toLowerCase().replace(/['"]+/g, '');
        if (lowerTag.startsWith(lowerString) && lowerString !== lowerTag) {
            suggestion = tag;
            return `${'&nbsp'.repeat(string.length - 1)}<mark>${tag.slice(string.replace(/['"]+/g, '').length)}</mark>`.replace(' ', '&nbsp');
        }
    }
    suggestion = "";
    return "";
}

/**
* Deletes tag assuming tag element gets binded to function
*/
function deleteTag() {
    $(this).parent().remove();
}

/**
* returns a list of all selected tags
*/
function getTags() {
    const tags = [];
    for (const li of $('#tags ul li')) {
        tags.push(li.innerText.replace(/['"]+/g, ''));
    }
    return tags.slice(0, -1);
}

/**
 * gets the recently used tags of a user and adds them to suggestions
 *
 * @param {Number} userId
 */
function fillTagSuggestions(userId) {
    const url = userRouter.controllers.backend.UserController.getUser(userId).url;
    get(url)
    .then((res) => {
        res.json()
        .then((json) => {
            tagPickerSuggestions = json.usedTags.map((tag) => tag.name);
        });
    })
}