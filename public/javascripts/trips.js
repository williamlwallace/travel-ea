let requestOrder = 0;
let lastRecievedRequestOrder = -1;
let paginationHelper;

/**
 * Initializes trip pages
 * @param {Number} userId - ID of user to get trips for
 */
function onPageLoad(userId) {
    paginationHelper = new PaginationHelper(1, 1, getTripResults, "tripPagination");
    getTripResults();
    
}

/**
 * Gets url and creates trips ha
 */
function getTripResults() {
    const url = new URL(tripRouter.controllers.backend.TripController.getAllTrips().url, window.location.origin);
    getAndCreateTrips(url);
}

/**
 * Filters the cards with filtered results
 */
function getAndCreateTrips(url) {

    // Append pagination params
    url.searchParams.append("pageNum", paginationHelper.getCurrentPageNumber());
    url.searchParams.append("pageSize", $('#pageSize').val().toString());
    url.searchParams.append("searchQuery", $('#search').val());
    url.searchParams.append("ascending", $('#ascending').val());
    url.searchParams.append("requestOrder", requestOrder++);

    get(url).then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                toast("Error", "Error fetching people data", "danger")
            } else {
                if(lastRecievedRequestOrder < json.requestOrder) {
                    const totalNumberPages = json.totalNumberPages;
                    $("#tripCardsList").html("");
                    lastRecievedRequestOrder = json.requestOrder;
                    json.data.forEach((item) => {
                        createTripCard(item);
                    });

                    $(".card").click((element) => {
                        populateModal($(element.currentTarget).find("#card-header").data().id);
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
    const endDestination = trip.tripDataList[trip.tripDataList.length -1].destination.name;
    const tripLength = trip.tripDataList.length;
    let firstDate = findFirstTripDate(trip);
    console.log (firstDate);
    if (!!firstDate) {
        firstDate = firstDate.toLocaleDateString();
    } else {
        firstDate = "No Date"
    }

    //insert in cards
    $(clone).find("#card-header").append(`${startDestination} - ${endDestination}`);
    //this will be the primary photo
    $(clone).find("#card-thumbnail").attr("src", null === null ? "/assets/images/default-profile-picture.jpg" : "/assets/images/default-profile-picture.jpg");
    $(clone).find("#start-location").append("Start Destination: " + startDestination);
    $(clone).find("#end-location").append("End Destination: " + endDestination);
    $(clone).find("#destinations").append("Trip Length: " + tripLength);
    $(clone).find("#date").append("Date: " + firstDate);
    $(clone).find("#card-header").attr("data-id", trip.id.toString());

    $("#tripCardsList").get(0).appendChild(clone);
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
    console.log("yoink")
    const toggled = $('#tripsFilterButton').css("display") === "block";
    console.log(toggled);
    $('#tripsFilterButton').css("display", toggled ? "none" : "block");
}

/**
 * Clears the filter and repopulates the cards
 */
function clearFilter() {
    $('#search').val('');
}