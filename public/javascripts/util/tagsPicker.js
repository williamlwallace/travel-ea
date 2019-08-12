/**
 * Class which allows for tags to be inserted and retrieved from an input field
 */
class TagPicker {

    constructor(userId, id) {
        this.tagPickerSuggestions = [];
        this.suggestion = "";
        this.id = id;
        this.list = $(`#${this.id} ul`);
        this.input = $(`#${this.id} input`);
        this.overlay = $(`#${this.id} p`);
        this.spacer = $("<li></li>").text("fillerfillerfil").attr('class', 'spacer');

        this.input.bind({
            keydown: this.keyDown.bind(this),
            keyup: this.keyUp.bind(this)
        });
        this.fillTagSuggestions(userId);
    }

    /**
    * Key down handler
    */
    keyDown(e) {
        //Need to keep depreciated symbols for older browsers
        const key = e.which || e.keyCode || e.key;
        const tag = this.input.val().replace(/['"]+/g, '');
        const natTag = this.input.val();
        const speachCount = natTag.length - tag.length;

        //If key is tab or enter or space
        if((key === 13 || key === 9 || key === 32) && tag !== "") {
            if ((key === 13 || key === 32) && speachCount !== 1) {
                e.preventDefault();
                this.insertTag(tag);
            } else if (key === 9 && this.suggestion !== "") {
                e.preventDefault();
                this.insertTag(this.suggestion);
            } else {
                return;
            }
            this.suggestion = "";
            this.overlay.html(this.suggestion);
        // else if key is delete
        } else if (key === 8 && natTag === "") {
            //Remove spacer, remove last tag, append spacer
            $(`#${this.id} li`).remove('.spacer');
            $(`#${this.id} ul li:last-child`).remove();
            this.list.append(this.spacer);

        // if the key is space and the strin is empty do nothing
        // or if the key is a speach mark and there is already two speach marks do nothing
        // of if the speach count is greater or equal to two (and key isnt delete) do nothing
        } else if ((key === 32 && tag === "") || (natTag !== "" && key === 222 && speachCount === 0) || (speachCount >= 2 && key != 8)) {
            e.preventDefault();
        }
    };

    /**
    * keyup handler
    */
    keyUp(e) {
        const tag = this.input.val();
        if (tag.replace('"', "") === "") {
            this.suggestion = "";
            this.overlay.html(this.suggestion);
        } else {
            const sugTag = this.searchTags(tag);
            this.overlay.html(sugTag);
        }
    };

    /**
     * Inserts a tag with given name into the tag input field
     * @param tagName - Name of tag to be inserted
     */
    insertTag(tagName) {
        const closeBtn = $("<button></button>").attr('class', 'close-icon').click(this.deleteTag);
        const tagEl = $("<li></li>").text(tagName);
        tagEl.append(closeBtn);
        //Remove spacer, append new tag, append spacer
        $(`#${this.id} li`).remove('.spacer');
        this.list.append(tagEl);
        this.list.append(this.spacer);
        //clear input
        this.input.val("");
        this.list.scrollLeft(5000);
    }

    /**
     * Populates tag input with a list of tags
     * @param tags - List of tags to insert into input field
     */
    populateTags(tags) {
        this.clearTags();
        for (const tag of tags) {
            this.insertTag(tag.name);
        }
    }

    /**
    * searchs suggested tags for matches and returns closest match
    * 
    * @param {String} string string to match 
    */
    searchTags(string) {
        for (const tag of this.tagPickerSuggestions) {
            const lowerTag = tag.toLowerCase().replace(/['"]+/g, '');
            const lowerString = string.toLowerCase().replace(/['"]+/g, '');
            if (lowerTag.startsWith(lowerString) && lowerString !== lowerTag) {
                this.suggestion = tag;
                return `${'&nbsp'.repeat(string.length - 1)}<mark>${tag.slice(string.replace(/['"]+/g, '').length)}</mark>`.replace(' ', '&nbsp');
            }
        }
        this.suggestion = "";
        return "";
    }

    /**
    * Deletes tag assuming tag element gets binded to function
    */
    deleteTag() {
        $(this).parent().remove();
    };

    /**
    * Returns a list of all selected tags
    */
    getTags() {
        const tags = [];
        for (const li of $(`#${this.id} ul li`)) {
            tags.push(li.innerText.replace(/['"]+/g, ''));
        }
        return tags.slice(0, -1);
    }

    /**
     * Gets the recently used tags of a user and adds them to suggestions
     * 
     * @param {Number} userId 
     */
    fillTagSuggestions(userId) {
        const url = userRouter.controllers.backend.UserController.getUser(userId).url;
        get(url)
        .then((res) => {
            res.json()
            .then((json) => {
                this.tagPickerSuggestions = json.usedTags.map((tag) => tag.name); 
            });
        })
    }

    /**
     * Clears the tags from the input field and resets spacer
     */
    clearTags() {
        this.list.empty();
        this.list.append(this.spacer);
    }
}