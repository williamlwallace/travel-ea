let tripsRequestOrder = 0;
let tripsLastRecievedRequestOrder = -1;
let tripsPaginationHelper;
let carouselDataId = 0;

/**
 * Initializes trip pages
 * @param {Number} userId - ID of user to get trips for
 */
function onPageLoad(userId) {
    tripsPaginationHelper = new PaginationHelper(1, 1, getTripResults,
        "tripPagination");
    getTripResults();
}

/**
 * Gets url and creates trip cards
 */
function getTripResults() {
    const url = new URL(
        tripRouter.controllers.backend.TripController.getAllTrips().url,
        window.location.origin);

    getAndCreateTrips(url, tripsPaginationHelper);
}

/**
 * Filters the cards with filtered results
 */
function getAndCreateTrips(url, paginationHelper) {
    let pageSize = $('#tripPageSize');
    if (pageSize.val() > 100) {
        pageSize.val(100);
        toast("Results per page too large",
            "The maximum results per page is 100, only 100 results will be returned",
            "warning", 7500);
    } else if (pageSize < 1) {
        pageSize.val(1);
        toast("Results per page too small",
            "The minimum results per page is 1, 1 result will be returned",
            "warning", 7500);
    }

    // Append pagination params
    url.searchParams.append("pageNum", paginationHelper.getCurrentPageNumber());
    url.searchParams.append("pageSize", pageSize.val().toString());
    url.searchParams.append("searchQuery", $('#tripSearch').val());
    url.searchParams.append("ascending", $('#tripAscending').val());
    url.searchParams.append("requestOrder", tripsRequestOrder++);

    getUserId().then(userId => {

        if ($("#filterMyTrips").val() === "myTrips") {
            url.searchParams.append("userId", userId);
        }

        get(url).then(response => {
            response.json()
            .then(json => {
                if (response.status !== 200) {
                    toast("Error", "Error fetching people data", "danger")
                } else {
                    if (tripsLastRecievedRequestOrder < json.requestOrder) {
                        const totalNumberPages = json.totalNumberPages;
                        $("#tripCardsList").html("");
                        tripsLastRecievedRequestOrder = json.requestOrder;
                        json.data.forEach((item) => {
                            $("#tripCardsList").append(createTripCard(item));
                        });

                        $(".card-body").click((element) => {
                            if (!$(element.currentTarget).find(
                                ".title").data()) {
                                return;
                            }
                            populateModal($(element.currentTarget).find(
                                ".title").data().id);
                        });
                        paginationHelper.setTotalNumberOfPages(
                            totalNumberPages);

                    }
                }
            })
        });
    });
}

/**
 * Creates a html trip card
 *
 * @param trip is Json profile object
 */
function createTripCard(trip) {
    const template = $("#tripCardTemplate").get(0);
    const clone = template.content.cloneNode(true);

    //Gather details
    const startDestination = trip.tripDataList[0].destination.name;
    const endDestination = trip.tripDataList[trip.tripDataList.length
    - 1].destination.name;
    const tripLength = trip.tripDataList.length;
    let firstDate = findFirstTripDate(trip);
    if (!!firstDate) {
        firstDate = firstDate.toLocaleDateString();
    } else {
        firstDate = "No Date"
    }

    initCarousel(clone, trip);
    $(clone).find('.carousel').carousel();

    $(clone).find("#start-location").append(startDestination);
    $(clone).find("#end-location").append(endDestination);
    $(clone).find("#destinations").append(tripLength + " destinations");
    $(clone).find("#date").append(firstDate);
    $(clone).find(".title").attr("data-id", trip.id.toString());

    return $(clone);
}

/**
 * Creates a carousel for given clone and trip data
 *
 * @param clone is the card template clone
 * @param trip is the trip data
 */
function initCarousel(clone, trip) {
    $(clone).find("#card-thumbnail-div-trips").append(
        `<div id=tripCarousel-${carouselDataId} class="trip-carousel carousel slide trip-carousel-fade carousel-fade" data-ride=carousel data-id=tripCarousel-${carouselDataId}><div id=carousel-inner class= 'trip-carousel-inner carousel-inner' data-id=carousel-inner-${carouselDataId}></div></div>`
    );
    let photo = null;
    let photoNum = 0;
    trip.tripDataList.forEach(tripObject => {
        if (tripObject.destination.primaryPhoto === null) {
            return;
        } else {
            photo = "../user_content/"
                + tripObject.destination.primaryPhoto.thumbnailFilename;
            photoNum += 1;
        }
        if (photo) {
            $(clone).find(`[data-id="carousel-inner-${carouselDataId}"]`).append(
                "<div class=\"trip-carousel-item carousel-item\">\n"
                + "<img src=" + photo
                + " class=\"d-block w-100\">\n"
                + "</div>"
            )
        }
    });

    // If there is more than one photo, create the carousel arrow buttons
    if (photoNum >= 1) {
        $(clone).find('.carousel-item').first().addClass('active');
        $(clone).carousel();
        if (photoNum > 1) {
            $(clone).find(`[data-id="tripCarousel-${carouselDataId}"]`).append(
                `<a class=carousel-control-prev href=#tripCarousel-${carouselDataId} role=button data-slide=prev>
                <span class='carousel-control-prev-icon trip-carousel-control-prev-icon' aria-hidden=true></span>
                <span class=sr-only>Previous</span>
                </a>
                <a id=next-button class=carousel-control-next href=#tripCarousel-${carouselDataId} role=button data-slide=next>
                <span class='carousel-control-next-icon trip-carousel-control-next-icon' aria-hidden=true></span>
                <span class=sr-only>Next</span>
                </a>`
            );
        }
        $('#next-button').click()
    }
    carouselDataId++;
}

/**
 * Finds the first date in a trip, if there is one.
 * @return the date found as a JS Date or null if no date was found
 */
function findFirstTripDate(trip) {
    for (const tripDestination of trip.tripDataList) {
        if (tripDestination.arrivalTime != null) {
            return new Date(tripDestination.arrivalTime);
        } else if (tripDestination.departureTime != null) {
            return new Date(tripDestination.departureTime);
        }
    }

    return null;
}

/**
 * Gets data relevant to trip and populates modal
 *
 * @param {Number} tripId id of trip
 */
function populateModal(tripId) {
    if (tripId == null) {
        return;
    }
    get(tripRouter.controllers.backend.TripController.getTrip(tripId).url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                error(json);
            } else {
                createTimeline(json);
                $('#trip-modal').modal();
            }
        });
    });
}

/**
 * Sets the appropriate data in the modal depending on if the user owns the trip
 * or is an admin.
 *
 * @param {Object} trip object containing all trip data
 */
function createTimeline(trip) {
    tripTagDisplay.populateTags(trip.tags);
    $('#timeline').html("");
    getUserId().then(currentUserId => {
        if (isUserAdmin() || (trip.userId == currentUserId)) {
            $("#privacy-img").remove();
            const privacyToggle = $(
                "<input id=\"privacy-img\" class=\"privacy-image\" type=\"image\">");
            $("#trip-dropdown").append(privacyToggle);

            if (trip.isPublic) {
                //Have to convert these to native DOM elements cos jquery dum
                $("#privacy-img")[0].setAttribute("src",
                    "/assets/images/public.png");
                $("#privacy-img")[0].setAttribute("title", "Public");
            } else {
                $("#privacy-img")[0].setAttribute("src",
                    "/assets/images/private.png");
                $("#privacy-img")[0].setAttribute("title", "Private");
            }

            $("#privacy-img").click(function () {
                updateTripPrivacy(
                    tripRouter.controllers.backend.TripController.updateTripPrivacy().url,
                    "/assets/images/public.png", "/assets/images/private.png",
                    trip.id)
            });

            // Add edit and delete trip buttons
            $("#edit-href").remove();
            const editButton = $(
                "<a id=\"edit-href\" href=\"\"><button id=\"editTrip\" type=\"button\" class=\"btn btn-primary\">Edit Trip</button></a>");
            $("#edit-button-wrapper").append(editButton);
            $('#edit-href').attr("href",
                tripRouter.controllers.frontend.TripController.editTrip(
                    trip.id).url);

            $("#deleteTrip").remove();
            const deleteButton = $(
                "<button id=\"deleteTrip\" type=\"button\" class=\"btn btn-danger\">Delete Trip</button>");
            $("#delete-button-wrapper").append(deleteButton);
            $("#deleteTrip").click(function () {
                deleteTrip(trip.id, trip.userId);
            });
        }

        $("#copy-href").remove();
        const copyButton = $(
            "<a id=\"copy-href\"><button id=\"copyTrip\" type=\"button\" class=\"btn btn-primary\">Copy This Trip</button></a>");
        $("#copy-trip-wrapper").append(copyButton);

        $('#copy-href').click(function () {
            $('#trip-modal').modal('toggle');
            copyTrip(trip.id, currentUserId);
        });

        const promises = [];
        for (let dest of trip.tripDataList) {
            promises.push(checkCountryValidity(dest.destination.country.name,
                dest.destination.country.id)
            .then(valid => {
                if (!valid) {
                    dest.destination.country.name = dest.destination.country.name
                        + ' (invalid)'
                }
                let timeline = `<article>
                    <div class="inner">\n`
                if (dest.arrivalTime != null) {
                    timeline += `<span class="date">
                        <span class="day">${dest.arrivalTime.substring(8, 10)}</span>
                        <span class="month">${dest.arrivalTime.substring(5, 7)}</span>
                        <span class="year">${dest.arrivalTime.substring(0, 4)}</span>
                    </span>\n`
                }
                timeline += `<h2>
                <a href=`
                    + destinationRouter.controllers.frontend.DestinationController.detailedDestinationIndex(
                        dest.destination.id).url + `>${dest.destination.name}</a><br>
                ${dest.destination.country.name}
                </h2>
                <p>\n`
                if (dest.arrivalTime != null) {
                    timeline += `Arrival: ${dest.arrivalTime.substring(11,
                        13)}:${dest.arrivalTime.substring(14, 16)}<br>\n`
                }
                if (dest.departureTime != null) {
                    timeline += `Departure: ${dest.departureTime.substring(11,
                        13)}:${dest.departureTime.substring(14, 16)}<br>
                    ${dest.departureTime.substring(8,
                        10)}/${dest.departureTime.substring(5,
                        7)}/${dest.departureTime.substring(0, 4)}\n`
                }
                timeline += `
                </p>
                </div>
                </article>`
                return timeline
            }));
        }
        Promise.all(promises).then(result => {
            const timeline = result.join('\n');
            $('#timeline').html($('#timeline').html() + timeline);
        })
    });
}

/**
 * Creates and submits request to delete a trip
 *
 * @param {Number} tripId ID of trip to be deleted
 * @param {Number} userId ID of owner of trip to refresh trips for
 */
function deleteTrip(tripId, userId) {
    const URL = tripRouter.controllers.backend.TripController.deleteTrip(
        tripId).url;
    const initialDelete = true;
    const handler = function (status, json) {
        if (this.initialDelete) {
            if (status !== 200) {
                toast("Failed to delete trip", json, "danger");
            } else if (initialDelete) {
                toast("Success", "Trip deleted!");
            }
            this.initialDelete = false;
        }

        $('#trip-modal').modal('hide');
        if (window.location.href.includes("/profile/")) {
            profileLoadTrips();
        } else {
            getTripResults();
        }
    }.bind({initialDelete});
    const reqData = new ReqData(requestTypes["TOGGLE"], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Copies a trip
 * @param {Number} tripId ID of the trip to copy
 * @param {Number} userId ID of the user copying the trip
 */
function copyTrip(tripId, userId) {
    const URL = tripRouter.controllers.backend.TripController.copyTrip(
        tripId).url;
    post(URL, {}).then(response => {
        if (response.status !== 201) {
            toast("Failed to copy trip", "danger");
        } else {
            toast("Successfully copied trip",
                "The trip will now appear in your trips");
        }
        $('#trip-modal').modal('hide');
        if (window.location.href.includes("/profile/")) {
            profileLoadTrips();
        } else {
            getTripResults();
        }
    });
}

/**
 * Toggles the filter button between being visible and invisible
 */
function toggleFilterButton() {
    let tripsFilterButton = $("#tripsFilterButton");
    const toggled = tripsFilterButton.css("display") === "block";
    tripsFilterButton.css("display", toggled ? "none" : "block");
    $('#createTripButton').css("display", toggled ? "none" : "block");
    $('#tripPagination').css("margin-top", toggled ? "0rem" : "-1.5rem");
}

/**
 * Clears the filter and repopulates the cards
 */
function clearTripFilter() {
    $("#tripSearch").val("");
    $("#tripPageSize").val(10);
    $("#tripAscending").val("true");
    $("#filterMyTrips").val("allTrips");
    getTripResults();
}