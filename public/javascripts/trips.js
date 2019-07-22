/**
 * Initializes trip table and calls method to populate
 * @param {Number} userId - ID of user to get trips for
 */
function onPageLoad(userId) {
    const tripGetURL = tripRouter.controllers.backend.TripController.getAllUserTrips(userId).url;
    const tripModal = {
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-id', data[data.length - 1]);
            $(row).addClass("clickable-row");
        },
        order: []
    };
    const tripTable = new EATable('tripTable', tripModal, tripGetURL, populate, showTripErrors);
    tripTable.initRowClicks(function () {
        populateModal(this);
        $('#trip-modal').modal();
    });
}

/**
 * Populates trips table
 *
 * @param {Object} json json object containing row data
 */
function populate(json) {
    const rows = [];
    
    for (const trip of json) {
        const id = trip.id;
        const startDestination = trip.tripDataList[0].destination.name;
        let endDestination;
        if (trip.tripDataList.length > 1) {
            endDestination = trip.tripDataList[trip.tripDataList.
                length - 1].destination.name;
        } else {
            endDestination = "-"
        }
        const tripLength = trip.tripDataList.length;
        let date;
        let firstDate = findFirstTripDate(trip);
        if (firstDate != null) {
            date = firstDate.toLocaleDateString();
        } else {
            date = "No Date"
        }
        const row = [startDestination, endDestination, tripLength,
            date, id];
        rows.push(row);
    }
    return rows;
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
 * Gets data relevent to trip and populates modal
 *
 * @param {Object} row row element
 */
function populateModal(row) {
    const tripId = row.dataset.id;
    get(tripRouter.controllers.backend.TripController.getTrip(tripId).url)
   .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                error(json);
            } else {
                createTimeline(json);
            }
        });
   });
    
}

/**
 * sets the apropriote data in the modal
 *
 * @param {Object} trip object containing all trip data
 */
function createTimeline(trip) {
    $('#timeline').html("");
    $('#edit-href').attr("href", tripRouter.controllers.frontend.TripController.editTrip(trip.id).url)
    if (trip.isPublic) {
        //Have to convert these to native DOM elements cos jquery dum
        $("#privacy-img")[0].setAttribute("src", "/assets/images/public.png");
    } else {
        $("#privacy-img")[0].setAttribute("src", "/assets/images/private.png");
    }
    
    for (dest of trip.tripDataList) {
        let timeline = `<article>
                            <div class="inner">\n`
        if (dest.arrivalTime != null) {
            timeline += `<span class="date">
                            <span class="day">${dest.arrivalTime.substring(8,10)}</span>
                            <span class="month">${dest.arrivalTime.substring(5,7)}</span>
                            <span class="year">${dest.arrivalTime.substring(0,4)}</span>
                        </span>\n`
        }
        timeline += `<h2>
                    ${dest.destination.name}<br>
                    ${dest.destination.country.name}
                </h2>
                <p>\n`
        if(dest.arrivalTime != null) {
            timeline += `Arrival: ${dest.arrivalTime.substring(11,13)}:${dest.arrivalTime.substring(14,16)}<br>\n`
        }
        if(dest.departureTime != null) {
            timeline += `Departure: ${dest.departureTime.substring(11,13)}:${dest.departureTime.substring(14,16)}<br>
            ${dest.departureTime.substring(8,10)}/${dest.departureTime.substring(5,7)}/${dest.departureTime.substring(0,4)}\n`
        }
        timeline += `
                </p>
            </div>
        </article>`
        $('#timeline').html($('#timeline').html() + timeline);
    }
}