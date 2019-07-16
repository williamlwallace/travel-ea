let tripTable;

/**
 * Initializes trip table and calls method to populate
 * @param {Number} userId - ID of user to get trips for
 */
function onPageLoad(userId) {
    tripTable = $('#tripTable').DataTable({
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-href', data[data.length - 1]);
            $(row).addClass("clickable-row");
        },
        order: []
    });
    populateTripTable(tripTable, userId);
}

/**
 * Populates trips table
 * @param {Object} table to populate
 * @param {Number} userId - ID of user to retrieve trips for
 */
function populateTripTable(table, userId) {
    get(tripRouter.controllers.backend.TripController.getAllUserTrips(userId).url)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showTripErrors(json);
            } else {
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
                    table.row.add(row).draw(false);
                }
            }
        })
    })
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
 * Trip table click listener
 */
$('#tripTable').on('click', 'tbody tr', function () {
    console.log("row clicked with trip id: " + this.dataset.href);
    //TODO: Show modal for the trip clicked
});
