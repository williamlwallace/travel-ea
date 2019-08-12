/**
 * Class which is used to display tags associated with objects
 */
class TagDisplay {

    constructor(id) {
        this.id = id;
        this.list = $(`#${this.id} ul`);
        this.overlay = $(`#${this.id} p`);
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