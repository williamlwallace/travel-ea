let tripTable;

/**
 * Initializes trip table and calls method to populate
 * @param {Number} userId - ID of user to get destinations for
 */
function onPageLoad(userId) {
    tripTable = $('#tripTable').DataTable({
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-href', data[data.length - 1]);
            $(row).addClass("clickable-row");
        }
    });
    populateTripTable(tripTable,userId);
}

/**
 * Populates trips table
 * @param {Object} table to populate
 * @param {Number} userId - ID of user to retrieve trips for
 */
function populateTripTable(table, userId) {
    get(tripsRouter.controllers.backend.TripsController.getAllUserTrips(
        userId).url)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showTripErrors(json);
            } else {
                for (const trip of json) {
                    const id = trip.id;
                    const name = destination.name;
                    const type = destination.destType;
                    const district = destination.district;
                    const latitude = destination.latitude;
                    const longitude = destination.longitude;
                    const country = destination.country.name;
                    const button = '<button id="addDestination" class="btn btn-popup" type="button">Add</button>';
                    const row = [name, type, district, latitude, longitude,
                        country, button, id, destination.country.id];
                    table.row.add(row).draw(false);
                }
            }
        })
    })
}

/**
 * Click listener that handles clicks in trips table
 */
//TODO
$('#destTable').on('click', 'button', function () {
    let tableAPI = $('#destTable').dataTable().api();
    let name = tableAPI.cell($(this).parents('tr'), 0).data();
    let district = tableAPI.cell($(this).parents('tr'), 1).data();
    let type = tableAPI.cell($(this).parents('tr'), 2).data();
    let latitude = tableAPI.cell($(this).parents('tr'), 3).data();
    let longitude = tableAPI.cell($(this).parents('tr'), 4).data();
    let countryId = $(this).parents('tr').attr("data-countryId");
    let id = $(this).parents('tr').attr('id');

    addDestinationToTrip(id, name, district, type, latitude, longitude,
        countryId);
});