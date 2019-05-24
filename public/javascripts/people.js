let countryDict = {};
let travellerTypeDict = {};
let table;

/**
 * Capitalise first letter of string
 * @param {String} string - input string to capitalise
 */
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

/**
 * Initialises the data table and adds the filter button to the right of the search field
 */
$(document).ready(function () {
    table = $('#dtPeople').DataTable( {
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-href', data[data.length-1]);
            $(row).addClass("clickable-row");
        },
        dom: "<'row'<'col-sm-12 col-md-2'l><'col-sm-12 col-md-9'bf><'col-sm-12 col-md-1'B>>" +
            "<'row'<'col-sm-12'tr>>" +
            "<'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",
        buttons: [
            {
                text: 'Filter',
                action: function ( e, dt, node, config ) {
                    $('#peopleFilterModal').modal('toggle');
                }
            },
            {
                text: 'Clear Filter',
                action: function (e, dt, node, config) {
                    $('#gender').val('');
                    $('#minAge').val(null);
                    $('#maxAge').val(null);
                    table.clear().draw();
                    populateTable(table, profileRouter.controllers.backend.ProfileController.searchProfilesJson().url);
                }
            }
        ]
    });
    populateTable(table, profileRouter.controllers.backend.ProfileController.searchProfilesJson().url);
});

/**
 * Populates people table
 * @param {Object} table to populate
 * @param {URL} url for lolling
 */
function populateTable(table, url){
    get(url)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if(response.status != 200) {
                showErrors(json);
            } else {
                for(const people of json) {
                    const profile = profileRouter.controllers.frontend.ProfileController.index(people.userId).url;
                    const firstName = people.firstName;
                    const lastName = people.lastName;
                    const gender = people.gender;
                    const age = calc_age(Date.parse(people.dateOfBirth));
                    getNationalityAndTravellerStrings(people)
                    .then(natAndTravArray => {
                        table.row.add([firstName, lastName, gender, age, natAndTravArray[0], natAndTravArray[1], profile]).draw(false);
                    });
                }
            }
        })
    })

}

/**
 * Gets the nationality and traveller types of a person as strings in a list.
 * @param {Object} people Json object including a person's details
 */
function getNationalityAndTravellerStrings(people) {
    return arrayToString(people.nationalities, 'name', destinationRouter.controllers.backend.DestinationController.getAllCountries().url)
    .then(nationalities => {
        return arrayToString(people.travellerTypes, 'description', profileRouter.controllers.backend.ProfileController.getAllTravellerTypes().url)
        .then(travellerTypes => {
            return [nationalities, travellerTypes];
        })
    })
}

/**
 * Filters the table with filtered results
 */
function searchParams() {
    let nationalityId = document.getElementById('nationalities').options[document.getElementById('nationalities').selectedIndex].value;
    let gender = document.getElementById('gender').value;
    let minAge = document.getElementById('minAge').value;
    let maxAge = document.getElementById('maxAge').value;
    let travellerTypeId = document.getElementById('travellerTypes').options[document.getElementById('travellerTypes').selectedIndex].value;
    let url = profileRouter.controllers.backend.ProfileController.searchProfilesJson(nationalityId, gender, minAge, maxAge, travellerTypeId).url;

    table.clear().draw();
    populateTable(table, url);
    $('#peopleFilterModal').modal('toggle');
}

/**
 * Redirect to users profile when row is clicked.
 */
$('#dtPeople').on('click', 'tbody tr', function() {
    window.location = this.dataset.href;
});