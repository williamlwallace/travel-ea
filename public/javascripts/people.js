/**
 * Capatilize first letter of stirng
 * @param {string} string - input string to capitalise
 */
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

//Initialises the data table and adds the filter button to the right of the search field
$(document).ready(function () {
    const table = $('#dtPeople').DataTable({
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-href', data[data.length - 1]);
            $(row).addClass("clickable-row");
        },
        dom: "<'row'<'col-sm-12 col-md-2'l><'col-sm-12 col-md-9'bf><'col-sm-12 col-md-1'B>>"
            +
            "<'row'<'col-sm-12'tr>>" +
            "<'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",
        buttons: [
            {
                text: 'Filter',
                action: function (e, dt, node, config) {
                    $('#modalContactForm').modal('toggle');
                }
            }
        ]
    });
    populateTable(table);
});

/**
 * Populates people table
 * @param {Object} table to populate
 */
function populateTable(table) {
    get(profileRouter.controllers.backend.ProfileController.searchProfilesJson().url)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showErrors(json);
            } else {
                for (const person of json) {
                    populateTableHelper(table, person);
                }
            }
        })
    })
}

/**
 * Adds a person's information to the given table
 *
 * @param {Object} table Table to populate
 * @param {Object} person Person to pull information from
 */
function populateTableHelper(table, person) {
    const profile = profileRouter.controllers.frontend.ProfileController.index(
        person.userId).url;
    const firstName = person.firstName;
    const lastName = person.lastName;
    const gender = person.gender;
    const age = calc_age(Date.parse(person.dateOfBirth));
    getNationalityAndTravellerStrings(person)
    .then(natAndTravArray => {
        table.row.add([firstName, lastName, gender, age,
            natAndTravArray[0], natAndTravArray[1],
            profile]).draw(false);
    });
}

/**
 * Gets the nationality and traveller types of a person as strings in a list.
 * @param {Object} people Json object including a person's details
 */
function getNationalityAndTravellerStrings(people) {
    return arrayToString(people.nationalities, 'name',
        destinationRouter.controllers.backend.DestinationController.getAllCountries().url)
    .then(nationalities => {
        return arrayToString(people.travellerTypes, 'description',
            profileRouter.controllers.backend.ProfileController.getAllTravellerTypes().url)
        .then(travellerTypes => {
            return [nationalities, travellerTypes];
        })
    })
}

/**
 * Creates URL with search paramaters for filters
 */
function searchParams() {
    let nationality = document.getElementById('nationality').value;
    let gender = document.getElementById('gender').value;
    let minAge = document.getElementById('minAge').value;
    let maxAge = document.getElementById('maxAge').value;
    let travellerType = document.getElementById('travellerType').value;
    let url = '/people?';
    if (nationality) {
        url += "nationalityId=" + nationality + "&";
    }
    if (gender) {
        url += "gender=" + gender + "&";
    }
    if (minAge) {
        url += "minAge=" + minAge + "&";
    }
    if (maxAge) {
        url += "maxAge=" + maxAge + "&";
    }

    if (travellerType) {
        url += "travellerTypeId=" + travellerType + "&";
    }
    url = url.slice(0, -1);
    return url;
}

/**
 * Apply search filters
 */
function apply() {
    let url;
    url = searchParams();
    window.location = url;

}

/**
 * Redirect to users profile when row is clicked.
 */
$('#dtPeople').on('click', 'tbody tr', function () {
    window.location = this.dataset.href;
});