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
    construtor(userId, id, URL) {
        this.PAGE_SIZE = 10;
        this.userId = userId;
        this.id = id;
        this.URL = URL;
        this.feed = $(`#${this.id}`);
        this.pageNumber = 0;
        this.data = {};
        this.getPage();
    }

    /*
     * gets next page of data
     */
    getPage() {
        url = this.createURL();
        get(url)
        .then(response => {
            response.json().then(json => {
                if (response.status !== 200) {
                    toast("Error", "Error loading news feed", "primary")
                    return;
                }
                if (json.totalNumberPages <= this.pageNumber) {
                    noMorePages();
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
        //Do something
        return;
    }

    /**
     * Creates the cards and adds to news feed
     */
    createCards(data) {
        //Do something
        return;
    }
}