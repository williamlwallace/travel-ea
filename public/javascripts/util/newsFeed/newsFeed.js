/*
 * Collects and handles news feed
 */
class NewsFeed {

    /**
     * Instantiates properties and loads first page of feed
     *
     * @param {Number} userId - ID of logged in user for suggesting tags
     * @param {String} id - ID for instance of class to define differences per page
     * @param {String} URL - URL of the newsfeed
     */
    constructor(userId, id, URL) {
        this.PAGE_SIZE = 4;
        this.EMPTY_NEWS_FEED = 'Nothing in the news feed!';
        this.NO_MORE_LOAD = 'Nothing more to load!';
        this.userId = userId;
        this.id = id;
        this.URL = URL;
        this.feed = $(`#${this.id}`);
        this.pageNumber = 0;
        this.data = {};
        this.getPage();
        $(window).scroll(this.scrollHandler.bind(this));
    }

    /*
     * gets next page of data
     */
    getPage() {
        const url = this.createURL();
        get(url)
        .then(response => {
            if (response.status !== 200) {
                toast("Error", "Error loading news feed", "primary")
                return;
            }
            response.json()
            .then(json => {
                if (response.status !== 200 || json.totalNumberPages <= this.pageNumber) {
                    this.noMorePages();
                    return;
                }
                if (this.insertData(json.requestOrder, json.data)) {
                    this.createCards(json.data);
                    this.pageNumber++;
                }
            });
        });
    }

    /**
     * inserts data and makes sure its not a duplicate page
     *
     * @param {number} pageNumber - page number of data
     * @param {object} page - array of page data 
     */
    insertData(pageNumber, page) {
        // if the last page does exist and the current page doesnt, add to data otherwise discard
        if ((pageNumber > 1 && !this.data[pageNumber.toString()] && !!this.data[(pageNumber - 1).toString()]) || (pageNumber === 1 && !this.data['1'])) {
            this.data[pageNumber.toString()] = page;
            return true;
        }
        return false;
    }

    /**
     * Creates an updated url for the news feed with page number and size 
     */
    createURL() {
        const pageNumber = this.pageNumber + 1;
        const url = new URL(
            this.URL,
            window.location.origin
        );
        url.searchParams.append("pageNum", pageNumber);
        url.searchParams.append("pageSize", this.PAGE_SIZE);
        url.searchParams.append("requestOrder", pageNumber);
        return url;
    }

    /**
     * Shows no more pages to be loaded
     */
    noMorePages() {
        this.feed.find('#feed-bottom-message').text(this.pageNumber ? this.NO_MORE_LOAD : this.EMPTY_NEWS_FEED);
    }

    /**
     * Creates the cards and adds to news feed
     */
    createCards(data) {
        for (const event of data) {
            const cardWrapper = this.createWrapperCard(event.thumbnail, event.message, this.formatDate(event.created));
            this.feed.find(".news-feed-body").append(cardWrapper);
        }
    }

    /**
     * CreateWrapper C
     */
    createWrapperCard(thumbnail, message, time) {
        const template = $("#news-feed-card-wrapper").get(0);
        const clone = $(template.content.cloneNode(true));

        clone.find('.wrapper-title').html(message);
        clone.find('.wrapper-picture').attr("src", "../user_content/" + thumbnail);
        clone.find('.wrapper-date').text(time);
        return clone;
    }

    /**
     * Reformats date list into string
     *
     * @param {string} date date string
     */
    formatDate(date) {
        return `${date[2]}/${date[1]}/${date[0]} ${date[3]}:${date[4]}`
    }

    /**
     * Handles window scroll event and adds new pages when at bottom
     */
    scrollHandler() {
        if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
            this.getPage();
        }
    }
}