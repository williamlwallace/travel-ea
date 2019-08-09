
tagPickerSuggestions = [];
suggestion = "";
$('#tagsInput').on('keydown', keyDown);
$('#tagsInput').on('keyup', keyUp);


/**
* Key down handler
*/
function keyDown(e) {
    //Need to keep depreciated symbols for older browsers
    const key = e.which || e.keyCode || e.key;
    const tag = $('#tags input').val().replace(/['"]+/g, '');
    const natTag = $('#tags input').val();
    const speachCount = natTag.length - tag.length;

    //If key is tab or enter or space
    if((key === 13 || key === 9 || key === 32) && tag !== "") {
        const spacer = $("<li></li>").text("fillerfillerfil").attr('class', 'spacer');
        const closeBtn = $("<button></button>").attr('class', 'close-icon').click(deleteTag);
        let tagEl;
        if ((key === 13 || key === 32) && speachCount !== 1) {
            e.preventDefault();
            tagEl = $("<li></li>").text(tag);
        } else if (key === 9 && suggestion != "") {
            e.preventDefault();
            tagEl = $("<li></li>").text(suggestion);
        } else {
            return;
        }
        suggestion = "";
        $('#backText').html(suggestion);
        tagEl.append(closeBtn);
        //Remove spacer, append new tag, append spacer
        $('li').remove('.spacer');
        $('#tags ul').append(tagEl);
        $('#tags ul').append(spacer);
        //clear input
        $('#tags input').val("");
        $('#tags ul').scrollLeft(5000);

    // else if key is delete
    } else if (key === 8 && tag === "") {
        const spacer = $("<li></li>").text("fillerfillerfil").attr('class', 'spacer');
        //Remove spacer, remove last tag, append spacer
        $('li').remove('.spacer');
        $('#tags ul li:last-child').remove();
        $('#tags ul').append(spacer);
    // if the key is space and the strin is empty do nothing
    // or if the key is a speach mark and there is already two speach marks do nothing
    // of if the speach count is greater or equal to two do nothing
    } else if ((key === 32 && tag === "") || speachCount >= 2) {
        e.preventDefault();
    }
};

/**
* keyup handler
*/
function keyUp(e) {
    const tag = $('#tags input').val();
    if (tag.replace('"', "") === "") {
        suggestion = ""
        $('#backText').html(suggestion);
    } else {
        const sugTag = searchTags(tag);
        $('#backText').html(sugTag);
    }
};

/**
* searchs suggested tags for matches and returns closest match
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
};

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
            console.log(json)
            tagPickerSuggestions = json.usedTags.map((tag) => tag.name); 
        });
    })
}