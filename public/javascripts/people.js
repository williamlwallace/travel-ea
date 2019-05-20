let countryDict = {};
let travellerTypeDict = {};
let isFiltered = false;
let table;

/**
 * Capitalise first letter of string
 * @param {String} string - input string to capitalise
 */
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

//Initialises the data table and adds the filter button to the right of the search field
$(document).ready(function () {
    table = $('#dtPeople').DataTable( {
        dom: "<'row'<'col-sm-12 col-md-2'l><'col-sm-12 col-md-9'bf><'col-sm-12 col-md-1'B>>" +
            "<'row'<'col-sm-12'tr>>" +
            "<'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",
        buttons: [
            {
                text: 'Filter',
                action: function ( e, dt, node, config ) {
                    $('#modalContactForm').modal('toggle');
                }
            },
            {
                text: 'Clear Filter',
                action: function (e, dt, node, config) {
                    isFiltered = false;
                    $('#nationalities').val('');
                    $('#gender').val('');
                    $('#minAge').val('');
                    $('#maxAge').val('');
                    $('#travellerTypes').val('');
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
                    const firstName = "<a href=" + profileRouter.controllers.frontend.ProfileController.index(people.userId).url + ">" +people.firstName + "</a>";
                    const lastName = people.lastName;
                    const gender = people.gender;
                    const age = calc_age(Date.parse(people.dateOfBirth));
                    getNationalityAndTravellerStrings(people)
                    .then(natAndTravArray => {
                        table.row.add([firstName, lastName, gender, age, natAndTravArray[0], natAndTravArray[1]]).draw(false);
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
function searchParams(){
    isFiltered = true;
    const nationalityId = document.getElementById('nationalities').options[document.getElementById('nationalities').selectedIndex].value;
    const gender = document.getElementById('gender').value;
    const minAge = document.getElementById('minAge').value;
    const maxAge = document.getElementById('maxAge').value;
    const travellerTypeId = document.getElementById('travellerTypes').options[document.getElementById('travellerTypes').selectedIndex].value;
    const url = profileRouter.controllers.backend.ProfileController.searchProfilesJson(nationalityId, gender, minAge, maxAge, travellerTypeId).url;

    table.clear().draw();
    populateTable(table, url);
    $('#modalContactForm').modal('toggle');
}