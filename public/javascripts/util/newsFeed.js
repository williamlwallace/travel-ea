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

    /**
     * Gets next page of data
     */
    getPage() {
        const url = this.createURL();
        get(url)
        .then(response => {
            if (response.status !== 200) {
                toast("Error", "Error loading news feed", "primary");
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
        // if the last page does exist and the current page doesn't, add to data otherwise discard
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
        if ((this.feed[0].id === 'main-feed' || this.feed[0].id === 'profile-feed')&& !this.pageNumber) {
            this.EMPTY_NEWS_FEED = "";
            this.feed.find('.empty-feed-splash').css({"display": "inline-block"});
            this.feed.find('#explore-btn').css({"display": "inline-block"});
        }
        this.feed.find('#feed-bottom-message').text(
            this.pageNumber ? this.NO_MORE_LOAD : this.EMPTY_NEWS_FEED);
    }

    /**
     * Creates the cards and adds to news feed
     */
    createCards(data) {
        for (const event of data) {
            const createCard = NewsFeedEventTypes[event.eventType];
            if (typeof createCard === "function") {
                const card = createCard(event);
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
    MULTIPLE_DESTINATION_PHOTO_LINKS: multipleGalleryPhotos,

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
 * @param {number} eventIds the guids of the events being displayed
 */
function createWrapperCard(thumbnail, message, time, eventIds) {
    const eventId = eventIds[0];

    const template = $("#news-feed-card-wrapper").get(0);
    const clone = $(template.content.cloneNode(true));
    const likeCounter = $(clone.find('.likes-number'));

    clone.find('.wrapper-title').html(message);
    if (thumbnail != null) {
        clone.find('.wrapper-picture').attr("src",
            "../user_content/" + thumbnail);
    }

    clone.find('.wrapper-date').text(time);
    const likeButton = clone.find('.likes-button');
    likeButton.attr('data-event-id', eventId);
    likeButton.attr('id', 'event-id-' + eventId);

    const url = newsFeedRouter.controllers.backend.NewsFeedController.toggleLikeStatus(
        eventId).url;
    get(url)
    .then(response => {
        response.json()
        .then(data => {
            if (response.status !== 200) {
                toast("Error", "Unable to get like status",
                    "danger", 5000);
            } else {
                likeButton.attr('data-liked', data);
                updateLikeButton(likeButton, true);
                updateLikeNumber(likeCounter, eventId);
            }
        });
    });
    likeButton.click(function () {
        likeUnlikeEvent(eventId)
    });

    return clone;
}

/**
 * Sends request to like and unlike events. Updates data attribute of liked
 * button to the result.
 * @param {Number} eventId the id of the event to like/unlike
 */

function likeUnlikeEvent(eventId) {
    const eventLikeButton = $("#event-id-" + eventId);
    const likeCounter = eventLikeButton.next();
    const url = newsFeedRouter.controllers.backend.NewsFeedController.toggleLikeStatus(
        eventId).url;

    const handler = (status, json) => {
        if (status !== 200) {
            toast("Error", "Unable to like event",
                "danger", 5000);
        } else {
            if (eventLikeButton.data('event-id') !== eventId) {
                return;
            }
            if (json === "liked") {
                likeCounter.data('likes', likeCounter.data('likes') + 1);
                eventLikeButton.attr('data-liked', "true");
                likeCounter.text(
                    countFormatter(parseInt(likeCounter.data('likes'))));
            } else {
                likeCounter.data('likes', likeCounter.data('likes') - 1);
                eventLikeButton.attr('data-liked', "false");
                likeCounter.text(
                    countFormatter(parseInt(likeCounter.data('likes'))));
            }
            updateLikeButton(eventLikeButton);
        }
    };
    const reqData = new ReqData(requestTypes["TOGGLE"], url,
        handler);

    undoRedo.sendAndAppend(reqData);
}

/**
 * Sets the like button style to be liked or unliked for a given event
 * @param {Object} eventLikeButton jquery object of target button
 * @param {boolean} noAnimation set to true to disable flight animation, false by default, used for on first load
 */
function updateLikeButton(eventLikeButton, noAnimation = false) {
    if (eventLikeButton.attr('data-liked') === "true") {
        if (!noAnimation) {
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
            eventLikeButton.addClass("fas");
        }
    } else {
        eventLikeButton.removeClass("fas");
        eventLikeButton.addClass("far");
        eventLikeButton.addClass("fas-in");
    }
}

/**
 * Updates the like counter on an event with the number from the backend
 * @param {Object} likeCounter the JQuery html object of the card like number field
 * @param {Number} eventId the id of the event to get the like count for
 */
function updateLikeNumber(likeCounter, eventId) {
    const url = newsFeedRouter.controllers.backend.NewsFeedController.getLikeCount(
        eventId).url;
    get(url)
    .then(response => {
        response.json()
        .then(data => {
            if (response.status !== 200) {
                toast("Error", "Unable to get like count for an event",
                    "danger", 5000);
            } else {
                const numberOfLikes = data.likeCount;
                likeCounter.data('likes', numberOfLikes);
                likeCounter.text(countFormatter(numberOfLikes));
            }
        })
    });
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
    const day = `${date[2]}`;
    const month = `${date[1]}`.padStart(2, "0");
    const year = `${date[0]}`;
    const hour = `${date[3]}`.padStart(2, "0");
    const minute = `${date[4]}`.padStart(2, "0");

    return day + "/" + month + "/" + year + " " + hour + ":" + minute;
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
        this.formatDate(event.created), event.eventIds);
}

/**
 * Creates news Feed wrapper made by user
 *
 * @param {object} event newsfeed event item data
 */
function createUserWrapperCard(event) {
    const message = `
        <a href="${"/profile/" + event.eventerId}"> 
            ${event.name}
        </a>
        ${event.message}`;

    return createWrapperCard(event.thumbnail, message,
        this.formatDate(event.created), event.eventIds);
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
 * @param {object} event - News feed event item data
 */
function newProfilePhotoCard(event) {
    const card = createUserWrapperCard(event);

    const template = $("#photo-card-template").get(0);
    const photoCard = $(template.content.cloneNode(true));

    photoCard.find('.photo-picture').attr("src",
        "../user_content/" + event.data.filename);
    photoCard.find('.baguette-image').attr("href",
        "../user_content/" + event.data.filename);
    setTimeout(() => baguetteBox.run('.photo-row'), 100);
    card.find('.wrapper-body').append(photoCard);
    return card;
}

/**
 * Creates news Feed card for destination primary photo change
 *
 * @param {object} event - News feed event item data
 */
function newPrimaryPhotoCard(event) {
    return newProfilePhotoCard(event);
}

/**
 * Creates news Feed card for groups of trip photos
 *
 * @param {object} event - News feed event item data
 */
function groupedTripUpdates(event) {
    return multipleDestinations(event);
}

/**
 * Creates a news feed card with multiple destinations which is scrollable
 *
 * @param {Object} event - News feed event containing destination details
 * @returns {Object} card - News feed card
 */
function multipleDestinations(event) {
    const card = createUserWrapperCard(event);
    const template = $("#multipleDestinationCardTemplate").get(0);
    const destinationCard = $(template.content.cloneNode(true));
    const destinations = event.data.newDestinations;
    const eventId = event.id;
    const destinationCardId = "multiple-destination-carousel-" + eventId;
    const destinationObjects = destinationCard.find('.carousel-inner');

    for (let i = 0; i < destinations.length; i++) {
        destinationCard.find(".main-carousel").attr("id", destinationCardId);
        destinationCard.find(".carousel-control-prev").attr("href",
            "#" + destinationCardId);
        destinationCard.find(".carousel-control-next").attr("href",
            "#" + destinationCardId);
        destinationCard.find(".carousel-inner").attr("id",
            "inner-" + destinationCardId);

        const carouselWrapper = document.createElement("DIV");
        carouselWrapper.setAttribute("class",
            "carousel-item " + (i === 0 ? "active" : ""));

        const destCard = createDestinationCard(destinations[i]);

        carouselWrapper.append(destCard);
        destinationObjects.append(carouselWrapper);
    }

    card.find('.wrapper-body').append(destinationCard);
    return card;
}

/**
 * Creates news Feed card for groups of gallery photos
 *
 * @param {object} event - News feed event item data
 */
function multipleGalleryPhotos(event) {
    const card = createUserWrapperCard(event);
    const template = $("#multiplePhotoCardTemplate").get(0);
    const photoCard = $(template.content.cloneNode(true));
    const photos = event.data.photos;
    const eventId = event.eventIds[0];
    const photoCardId = "multiple-photo-carousel-" + eventId;
    const photoThumbnails = photoCard.find('.photo-thumbnails');
    const photoDatas = [];
    for (const i in photos) {
        photoDatas.push({
            eventId: event.eventIds[i],
            tags: photos[i].tags
        });

        photoCard.find(".main-carousel").attr("id", photoCardId);
        photoCard.find(".carousel-control-prev").attr("href",
            "#" + photoCardId);
        photoCard.find(".carousel-control-next").attr("href",
            "#" + photoCardId);
        photoCard.find(".carousel-inner").attr("id", "inner-" + photoCardId);

        const photo = photos[i];
        const carouselWrapper = document.createElement("DIV");
        carouselWrapper.setAttribute("class",
            "carousel-item " + (i == 0 ? "active" : ""));

        const baguetteWrapper = document.createElement("A");
        baguetteWrapper.setAttribute("class", "baguette-image");
        baguetteWrapper.setAttribute("href",
            "../user_content/" + photo.filename);

        const imageWrapper = document.createElement("IMG");
        imageWrapper.setAttribute("src",
            "../user_content/" + photo.thumbnailFilename);
        imageWrapper.setAttribute("class", "d-block w-100");

        baguetteWrapper.append(imageWrapper);
        carouselWrapper.append(baguetteWrapper);
        photoThumbnails.append(carouselWrapper);
    }

    addTags(card, photos[0].tags);

    setTimeout(() => {
        baguetteBox.run('#inner-' + photoCardId);
        $('#' + photoCardId).carousel({interval: false});
        $('#' + photoCardId).on('slide.bs.carousel', function (e) {
            updateMultyCard(photoDatas, e.direction,
                $(this).closest('.news-feed-wrapper'));
        });
    }, 100);
    card.find('.wrapper-body').append(photoCard);

    return card;
}

/**
 *
 * @param {Object} photoDatas list of photo data
 * @param {string} direction direction of scroll
 * @param {Object} card carousel jquery object
 */
function updateMultyCard(photoDatas, direction, card) {
    const carouselInner = card.find('.carousel-inner');
    const likeCounter = card.find('.likes-number');
    const likeButton = card.find('.likes-button');
    //Update get and update photoId
    let photoId = parseInt(carouselInner.data('photo-id'));
    photoId = direction === 'left' ? (photoId + 1) % photoDatas.length : photoId
        - 1;
    if (photoId < 0) {
        photoId = photoDatas.length - 1;
    }
    carouselInner.data('photo-id', photoId);

    //update event-id
    const eventId = photoDatas[photoId].eventId;
    likeButton.data('event-id', eventId);
    likeButton.attr('id', 'event-id-' + eventId);

    //get and update likes
    const url = newsFeedRouter.controllers.backend.NewsFeedController.toggleLikeStatus(
        eventId).url;
    get(url)
    .then(response => {
        response.json()
        .then(data => {
            if (response.status !== 200) {
                toast("Error", "Unable to get like status",
                    "danger", 5000);
            } else {
                likeButton.attr('data-liked', data);
                updateLikeButton(likeButton, true);
                updateLikeNumber(likeCounter, eventId);
            }
        })
    });
    likeButton.unbind('click');
    likeButton.click(function () {
        likeUnlikeEvent(eventId)
    });

    //Update tags
    const tags = photoDatas[photoId].tags;
    addTags(card, tags);

}

/**
 * Creates news Feed card for user creating new destination
 *
 * @param {object} event - News feed event item data
 */
function createdNewDestinationCard(event) {
    const card = createUserWrapperCard(event);
    const dest = event.data;
    const destinationCard = createDestinationCard(dest);
    card.find('.wrapper-body').append(destinationCard);

    return card
}

/**
 * Creates news Feed card for user updating destination
 *
 * @param {object} event - News feed event item data
 */
function updatedExistingDestinationCard(event) {
    return createdNewDestinationCard(event);
}

/**
 * Creates news Feed card for user changing cover photo
 *
 * @param {object} event - News feed event item data
 */
function newProfileCoverPhotoCard(event) {
    return newProfilePhotoCard(event);
}

