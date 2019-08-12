/**
 * Class which is used to display tags associated with objects
 */
class TagDisplay {

    /**
     * Class constructor, initialises ID of class instance
     * @param {Number} id - Unique ID to create difference between instances of class, used by different pages
     */
    constructor(id) {
        this.id = id;
    }

    /**
     * Populates the tag display with all tags
     *
     * @param {Array} tags - List of tags to display
     */
    populateTags(tags) {
        const tagHolder = $(`#${this.id}`);
        tagHolder.empty();

        for (const tag of tags) {
            const nextTag = $("<li>").text(tag.name);
            tagHolder.append(nextTag);
        }
    }
}