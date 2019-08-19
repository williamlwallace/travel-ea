/**
 * Class which is used to display tags associated with objects
 */
class TagDisplay {

    /**
     * Class constructor, initialises ID of class instance
     * @param {String} id - Unique ID to create difference between instances of class, used by different pages
     */
    constructor(id) {
        this.id = id;
        this.list = $(`#${this.id} ul`);
    }

    /**
     * Populates the tag display with all tags
     *
     * @param {Array} tags - List of tags to display
     */
    populateTags(tags) {
        const tagHolder = $(`#${this.id}`);
        this.list.empty();

        for (const tag of tags) {
            const nextTag = $("<li>").text(tag.name);
            this.list.append(nextTag);
        }
    }
}