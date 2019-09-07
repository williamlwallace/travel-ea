let tripsRequestOrder = 0;
let tripsLastRecievedRequestOrder = -1;
let tripsPaginationHelper;

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
    const url = new URL(tripRouter.controllers.backend.TripController.getAllTrips().url, window.location.origin);
    getAndCreateTrips(url, tripsPaginationHelper);
}

/**
 * Filters the cards with filtered results
 */
function getAndCreateTrips(url, paginationHelper) {

    // Append pagination params
    url.searchParams.append("pageNum", paginationHelper.getCurrentPageNumber());
    url.searchParams.append("pageSize", $('#tripPageSize').val().toString());
    url.searchParams.append("searchQuery", $('#tripSearch').val());
    url.searchParams.append("ascending", $('#tripAscending').val());
    url.searchParams.append("requestOrder", tripsRequestOrder++);

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
                        createTripCard(item);
                    });

                    $(".card-body").click((element) => {
                        if (!$(element.currentTarget).find(
                            ".title").data()) {
                            return;
                        }
                        console.log("work?");
                        populateModal($(element.currentTarget).find(
                            ".title").data().id);
                    });
                    paginationHelper.setTotalNumberOfPages(totalNumberPages);

                }
            }
        })
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

    $(clone).find("#start-location").append(startDestination);
    $(clone).find("#end-location").append(endDestination);
    $(clone).find("#destinations").append("No. of Destinations: " + tripLength);
    $(clone).find("#date").append("Date: " + firstDate);
    $(clone).find(".title").attr("data-id", trip.id.toString());

    $("#tripCardsList").get(0).appendChild(clone);
}

let i = 0; // Global counter for carousel data-id

/**
 * Creates a carousel for given clone and trip data
 *
 * @param clone is the card template clone
 * @param trip is the trip data
 */
function initCarousel(clone, trip) {
    $(clone).find("#card-thumbnail-div").append(
        `<div id=tripCarousel-${i} class="carousel slide" data-ride=carousel data-id=tripCarousel-${i}><div id=carousel-inner class=carousel-inner data-id=carousel-inner-${i}></div></div>`
    );
    let photo = null;
    let photoNum = 0;
    trip.tripDataList.forEach(tripObject => {
         if (tripObject.destination.primaryPhoto === null) {
             photo = null
         } else {
             photo = "../user_content/" + tripObject.destination.primaryPhoto.thumbnailFilename;
             photoNum += 1;
         }
        if (photo != null) {
            $(clone).find(`[data-id="carousel-inner-${i}"]`).append(
                "<div class=\"carousel-item\">\n"
                + "<img src=" + photo
                + " class=\"d-block w-100\">\n"
                + "</div>"
            )
        }
    });

    // If there is more than one photo, create the carousel arrow buttons
    if (photoNum > 1) {
        $(clone).find(`[data-id="tripCarousel-${i}"]`).append(
            `<a class=carousel-control-prev href=#tripCarousel-${i} role=button data-slide=prev>
        <span class=carousel-control-prev-icon aria-hidden=true></span>
        <span class=sr-only>Previous</span>
        </a>
        <a class=carousel-control-next href=#tripCarousel-${i} role=button data-slide=next>
        <span class=carousel-control-next-icon aria-hidden=true></span>
        <span class=sr-only>Next</span>
        </a>`
        );
        $(clone).find('.carousel-item').first().addClass('active');
        $(clone).carousel({
            interval: 5000,
        });
    }

    // if (photo === null) {
    //     photo = "https://images.pexels.com/photos/747964/pexels-photo-747964.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260";
    //     $(clone).find(`[data-id="carousel-inner-${i}"]`).append(
    //         "<div class=\"carousel-item\">\n"
    //         + "<img src=" + photo
    //         + " class=\"d-block w-100\" alt=\"...\">\n"
    //         + "</div>"
    //     )
    // }
    i++;
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

        const getTripURL = tripRouter.controllers.backend.TripController.getAllTrips(
            userId).url;
        getTripResults();
        $('#trip-modal').modal('hide');
    }.bind({initialDelete});
    const reqData = new ReqData(requestTypes["TOGGLE"], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Toggles the filter button between being visible and invisible
 */
function toggleFilterButton() {
    const toggled = $('#tripsFilterButton').css("display") === "block";
    $('#tripsFilterButton').css("display", toggled ? "none" : "block");
}

/**
 * Clears the filter and repopulates the cards
 */
function clearFilter() {
    $('#search').val('');
}