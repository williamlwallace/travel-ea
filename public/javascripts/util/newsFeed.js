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
                if (response.status !== 200 || json.totalNumberPages
                    <= this.pageNumber) {
                    this.noMorePages();
                    return;
                }
                if (this.insertData(json.requestOrder, json.data)) {
                    this.createCards(json.data);
                    this.pageNumber++;
                }
                // $('.upflight-icon').click(() => {
                //     if ($('.upflight-icon').hasClass("far")) {
                //         $('.upflight-icon').addClass("rotate-top");
                //         $('.upflight-counter').text(parseInt($('.upflight-counter').text()) + 1);
                //         setTimeout(() => {
                //             $('.upflight-icon').removeClass("fas-in");
                //             $('.upflight-icon').removeClass("rotate-top");
                //             $('.upflight-icon').removeClass("far");
                //             $('.upflight-icon').addClass("fas");
                //             $('.upflight-icon').addClass("fas-in");
                //         }, 500);
                //         setTimeout(() => {
                //             $('.upflight-icon').removeClass("fas-in");
                //         }, 700);

                //     } else {
                //         $('.upflight-counter').text(parseInt($('.upflight-counter').text()) - 1);
                //         $('.upflight-icon').removeClass("fas");
                //         $('.upflight-icon').addClass("far");
                //         $('.upflight-icon').addClass("fas-in");
                //     }
                    
                    
                // })
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
        if ((pageNumber > 1 && !this.data[pageNumber.toString()]
            && !!this.data[(pageNumber - 1).toString()]) || (pageNumber === 1
            && !this.data['1'])) {
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
        this.feed.find('#feed-bottom-message').text(
            this.pageNumber ? this.NO_MORE_LOAD : this.EMPTY_NEWS_FEED);
    }

    /**
     * Creates the cards and adds to news feed
     */
    createCards(data) {
        for (const event of data) {
            const createCard = NewsFeedEventTypes[event.eventType];
            if (typeof createCard == "function") {
                const card = createCard(event)
                this.feed.find(".news-feed-body").append(card);
            }

        }
    }

    /**
     * Handles window scroll event and adds new pages when at bottom
     */
    scrollHandler() {
        if ((window.innerHeight + window.scrollY)
            >= document.body.offsetHeight) {
            this.getPage();
        }
    }
}

/****************************
 News Feed event types
 *****************************/

const NewsFeedEventTypes = {
    // Destinations events

    /**
     * Grouped event for multiple photos being linked to a destination by the same person
     * reference ID = ID of photo
     * dest ID = ID of destination
     */
    MULTIPLE_DESTINATION_PHOTO_LINKS: multipleDestinationPhotoLinks,

    /**
     * A public destination has had its primary photo updated
     * reference ID = ID of photo
     */
    NEW_PRIMARY_DESTINATION_PHOTO: newPrimaryPhotoCard,

    /**
     * A public destination has been created
     * reference ID = ID of destination
     */
    CREATED_NEW_DESTINATION: createdNewDestinationCard,

    /**
     * Existing destination has been updated, or set from private to public
     * reference ID = ID of destination
     */
    UPDATED_EXISTING_DESTINATION: updatedExistingDestinationCard,

    // User photo events
    /**
     * New profile picture has been set for a user
     * reference ID = ID of photo
     */
    NEW_PROFILE_PHOTO: newProfilePhotoCard,

    /**
     * A user has uploaded multiple new gallery photos, that have been grouped into one news feed event
     *
     */
    MULTIPLE_GALLERY_PHOTOS: multipleGalleryPhotos,

    /**
     * A user has updated their cover picture
     * reference ID = ID of photo
     */
    NEW_PROFILE_COVER_PHOTO: newProfileCoverPhotoCard,

    // Trip events
    /**
     * A new public trip has been created by a user
     * reference ID = ID of trip just created
     */
    CREATED_NEW_TRIP: createdNewTripCard,

    /**
     * An existing trip that is public has been updated, or a private trip has been set to public
     * reference ID = ID of trip updated or made public
     */
    GROUPED_TRIP_UPDATES: groupedTripUpdates
};

/******************************
 News feed card creation - Helpers
 *******************************/

/**
 * Create news feed card wrapper
 *
 * @param {string} thumbnail address of thumbnail
 * @param {string} message event message
 * @param {string} time string timestamp
 * @param {number} eventId the guid of the event being displayed
 */
function createWrapperCard(thumbnail, message, time, eventId) {
    eventId = 1; //TODO: remove when eventId is correctly send to frontend

    const template = $("#news-feed-card-wrapper").get(0);
    const clone = $(template.content.cloneNode(true));

    clone.find('.wrapper-title').html(message);
    if (thumbnail != null) {
        clone.find('.wrapper-picture').attr("src", "../user_content/" + thumbnail);
    }

    clone.find('.wrapper-date').text(time);
    const likeButton = clone.find('.likes-button');
    likeButton.attr('data-event-id', eventId);
    likeButton.attr('data-liked', "false");

    likeButton.click(likeUnlikeEvent);

    return clone;
}

/**
 * Sends request to like and unlike events. Updates data attribute of liked
 * button to the result.
 */
function likeUnlikeEvent() {
    const eventLikeButton = $(this);
    const likeCounter= eventLikeButton.next();
    const setLiked = eventLikeButton.attr('data-liked') === "false";
    //TODO send put request to the like endpoint for the event
    //TODO change the follower count if needed

    if (setLiked) {
        eventLikeButton.attr('data-liked', "true");
        likeCounter.text(parseInt(likeCounter.text()) + 1);
    } else {
        eventLikeButton.attr('data-liked', "false");
        likeCounter.text(parseInt(likeCounter.text()) - 1);
    }

    updateLikeButton(eventLikeButton);
}

/**
 * Sets the like button style to be liked or unliked for a given event
 * @param {Object} eventLikeButton jquery object of target button
 */
function updateLikeButton(eventLikeButton) {
    if (eventLikeButton.attr('data-liked') === "true") {
        eventLikeButton.addClass("rotate-top");
        setTimeout(() => {
            eventLikeButton.removeClass("fas-in");
            eventLikeButton.removeClass("rotate-top");
            eventLikeButton.removeClass("far");
            eventLikeButton.addClass("fas");
            eventLikeButton.addClass("fas-in");
        }, 500);
        setTimeout(() => {
            eventLikeButton.removeClass("fas-in");
        }, 700);

    } else {
        
        eventLikeButton.removeClass("fas");
        eventLikeButton.addClass("far");
        eventLikeButton.addClass("fas-in");
    }
}

/**
 * Adds tags to a card for the newsfeed
 * @param card the dom element to add tags to
 * @param tags a list of tags to add to the card
 */
function addTags(card, tags) {
    const list = card.find('.wrapper-tags');
    const tagsDisplay = new TagDisplay('fakeId');
    tagsDisplay.list = list;
    tagsDisplay.populateTags(tags);
}

/**
 * Reformats date list into string
 *
 * @param {string} date date string
 */
function formatDate(date) {
    return `${date[2]}/${date[1]}/${date[0]} ${date[3]}:${date[4]}`
}

/**
 * Creates news Feed wrapper made by destination
 *
 * @param {object} event newsfeed event item data
 */
function createDestinationWrapperCard(event) {
    const message = `The destination <a href="${"/destination/"
    + event.eventerId}"> 
                        ${event.name}
                    </a>
                    ${event.message}`;
    return createWrapperCard(event.thumbnail, message,
        this.formatDate(event.created), event.id);
}

/**
 * Creates news Feed wrapper made by user
 *
 * @param {object} event newsfeed event item data
 */
function createUserWrapperCard(event) {
    const id = 1; //testing
    const message = `
        <a href="${"/profile/" + event.eventerId}"> 
            ${event.name}
        </a>
        ${event.message}`;

    return createWrapperCard(event.thumbnail, message,
        this.formatDate(event.created), event.id);
}

/******************************
 News feed card creation - Custom
 *******************************/

/**
 * Creates news Feed card for creating a trip
 *
 * @param {object} event newsfeed event item data
 */
function createdNewTripCard(event) {
    const card = createUserWrapperCard(event);
    const tripCard = createTripCard(event.data);
    card.find('.wrapper-body').append(tripCard);
    addTags(card, event.data.tags);
    return card;
}

/**
 * Creates news Feed card for creating a trip
 *
 * @param {object} event newsfeed event item data
 */
function newProfilePhotoCard(event) {
    const card = createUserWrapperCard(event);

    const template = $("#photo-card-template").get(0);
    const photoCard = $(template.content.cloneNode(true));

    photoCard.find('.photo-picture').attr("src",
        "../user_content/" + event.data.filename);
    photoCard.find('.baguette-image').attr("href",
        "../user_content/" + event.data.filename);
    setTimeout(() => baguetteBox.run('.photo-row'), 10);
    card.find('.wrapper-body').append(photoCard);
    return card;
}

/**
 * Creates news Feed card for destination primary photo change
 *
 * @param {object} event newsfeed event item data
 */
function newPrimaryPhotoCard(event) {
    const card = createDestinationWrapperCard(event);
    //TODO: The card
    return card
}

/**
 * Creates news Feed card for groupes of trip photos
 *
 * @param {object} event newsfeed event item data
 */
function groupedTripUpdates(event) {
    const card = createUserWrapperCard(event);
    //TODO: The card
    return card;
}


/**
 * Creates news Feed card for groupes of destination photos
 *
 * @param {object} event newsfeed event item data
 */
function multipleDestinationPhotoLinks(event) {
    const card = createUserWrapperCard(event);
    //TODO: The card
    return card;
}


/**
 * Creates news Feed card for groups of gallery photos
 *
 * @param {object} event newsfeed event item data
 */
function multipleGalleryPhotos(event) {
    const card = createUserWrapperCard(event);
    //TODO: The card
    return card;
}



/**
 * Creates news Feed card for user creating new destination
 *
 * @param {object} event newsfeed event item data
 */
function createdNewDestinationCard(event) {
    const card = createUserWrapperCard(event);

    const template = $("#destinationCardTemplate").get(0);
    const destinationCard = $(template.content.cloneNode(true));

    const dest = event.data;

    let tags = "";
    let travellerTypes = "";

    $(destinationCard).find("#card-header").append(dest.name);
    if (dest.primaryPhoto) {
        $(destinationCard).find("#card-thumbnail").attr("src",
            "../user_content/" + dest.primaryPhoto.thumbnailFilename);
    }
    $(destinationCard).find("#district").append(
        dest.district ? dest.district : "No district");
    $(destinationCard).find("#country").append(dest.country.name);
    $(destinationCard).find("#destType").append(
        dest.destType ? dest.destType : "No type");
    $(destinationCard).find("#card-header").attr("data-id", dest.id.toString());
    $(destinationCard).find("#card-header").attr("id",
        "destinationCard-" + dest.id.toString());

    $($(destinationCard).find('#destinationCard-' + dest.id.toString())).click(
        function () {
            location.href = '/destinations/' + $(this).data().id;
        });

    dest.tags.forEach(item => {
        tags += item.name + ", ";
    });
    tags = tags.slice(0, -2);

    dest.travellerTypes.forEach(item => {
        travellerTypes += item.description + ", ";
    });
    travellerTypes = travellerTypes.slice(0, -2);

    $(destinationCard).find("#destinatonCardTravellerTypes").append(
        travellerTypes ? travellerTypes : "No traveller types");

    $(destinationCard).find("#tags").remove();

    card.find('.wrapper-body').append(destinationCard);

    addTags(card, dest.tags);

    return card
}

/**
 * Creates news Feed card for user updateing destination
 *
 * @param {object} event newsfeed event item data
 */
function updatedExistingDestinationCard(event) {
    return createdNewDestinationCard(event);
}

/**
 * Creates news Feed card for user changing cover photo
 *
 * @param {object} event newsfeed event item data
 */
function newProfileCoverPhotoCard(event) {
    return newProfilePhotoCard(event);
}