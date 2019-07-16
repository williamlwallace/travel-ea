/**
 * Initializes trip table and calls method to populate
 * @param {Number} userId - ID of user to get trips for
 */
function onPageLoad(userId) {
    const tripGetURL = tripRouter.controllers.backend.TripController.getAllUserTrips(userId).url;
    console.log(tripGetURL);
    
    const tripModal = {
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-href', data[data.length - 1]);
            $(row).addClass("clickable-row");
        },
        order: []
    };
    const tripTable = new EATable('tripTable', tripModal, tripGetURL, populate, showTripErrors);
    tripTable.initRowClicks(function () {
        console.log("row clicked with trip id: " + this.dataset.href);
        //TODO: Show modal for the trip clicked
    });
}

/**
 * Populates trips table
 *
 * @param {Object} json json object containing row data
 */
function populate(json) {
    const rows = [];
    console.log(json);
    
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