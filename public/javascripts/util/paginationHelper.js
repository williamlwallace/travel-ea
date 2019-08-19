/**
 * The pagination helper. Handles creation of pagination DOM elements and event
 * click listeners.
 */
class PaginationHelper {

    /**
     * The pagination helper constructor, takes in required parameters and draws
     * the pagination bar on the page.
     *
     * @param {Number} totalNumberPages the total number of pages of cards
     * @param {Number} pageNum the current page number
     * @param {String} paginationId the id of the pagination DOM element in HTML
     * @param {Function} onChangeFunction the function that will be called when the page is
     *        changed.
     */
    constructor(totalNumberPages, pageNum, paginationId, onChangeFunction) {
        this.paginationObject = $("#" + paginationId);
        this.totalNumberPages = totalNumberPages;
        this.pageNum = pageNum;
        this.pageNumbers = [];
        this.onChangeFunction = onChangeFunction;
        this.drawPaginationBar();
    }

    /**
     * Based on what page you are on, creates the correct pagination bar with up to 5 page buttons
     * Will set the current page button to active and disable buttons that shouldn't
     * be clicked.
     */
    drawPaginationBar() {
        this.paginationObject.html("");
        if (this.totalNumberPages > 1) {
            // Less than 5 pages
            this.pageNumbers = [];
            if(this.totalNumberPages <= 5) {
                for(let i = 1; i <= this.totalNumberPages; i++) {
                    this.pageNumbers.push(i);
                }
            }
            // In first 3 pages, and more than 5 total
            else if(this.totalNumberPages >= 5 && this.pageNum < 3) {
                for(let i = 1; i <= 5; i++) {
                    this.pageNumbers.push(i);
                }
            }
            // In last 3, and more than 5 total
            else if(this.totalNumberPages >= 5 && this.pageNum > this.totalNumberPages - 2) {
                for(let i = this.totalNumberPages - 4; i <= this.totalNumberPages; i++) {
                    this.pageNumbers.push(i);
                }
            } else {
                for(let i = this.pageNum - 2; i <= this.pageNum + 2; i++) {
                    this.pageNumbers.push(i);
                }
            }

            const paginationClassObject = this;

            //Previous button
            const isFirstPage = (this.pageNum === 1);
            this.paginationObject.append("<li class=\"page-item previous " +
                (isFirstPage ? "disabled" : "") + "\" "
                + "style=\"cursor: pointer\"><a class=\"page-link\">Previous</a></li>");

            this.paginationObject.find('.previous').click(function() {
                paginationClassObject.goToPage(paginationClassObject.pageNum - 1);
            });

            //Numbered buttons
            this.pageNumbers.forEach((item) => {
                const isCurrentItem = (item === this.pageNum);
                this.paginationObject.append("<li class=\"page-item anotherPage "
                    + (isCurrentItem ? "active" : "") + "\" "
                    + "style=\"cursor: pointer\" "
                    + "data-pageId=\""+item+"\"><a class=\"page-link\">" + item + "</a></li>");
            });

            this.paginationObject.find('.anotherPage').click(function() {
                paginationClassObject.goToPage(parseInt($(this).attr('data-pageId')));
            });

            //Next button
            const isLastPage = (this.pageNum === this.totalNumberPages);
            this.paginationObject.append("<li class=\"page-item next " +
                (isLastPage ? "disabled" : "") + "\" "
                + "style=\"cursor: pointer\"><a class=\"page-link\">Next</a></li>");

            this.paginationObject.find('.next').click(function() {
                paginationClassObject.goToPage(paginationClassObject.pageNum + 1);
            });
        }
    }

    /**
     * Changes the page we are viewing (if possible)
     *
     * @param {Number} desiredPageNumber Page number to change to
     */
    goToPage(desiredPageNumber) {
        if(desiredPageNumber > this.totalNumberPages || desiredPageNumber < 1) {
            toast("No such page", "The page you have tried to go to does not exist", "danger");
        } else {
            this.pageNum = desiredPageNumber;
            this.onChangeFunction();
        }
    }

    /**
     * Gets the current page number
     *
     * @returns {Number} the current page number
     */
    getCurrentPageNumber() {
        return this.pageNum;
    }

    /**
     * Sets the total number of pages
     *
     * @param {Number} totalNumberOfPages the total number of pages
     */
    setTotalNumberOfPages(totalNumberOfPages) {
        this.totalNumberPages = totalNumberOfPages;
        this.drawPaginationBar();
    }
}



