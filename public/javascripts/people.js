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
    const tableModal = {
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-href', data[data.length - 1]);
            $(row).addClass("clickable-row");
        }
    };
    const getURL = profileRouter.controllers.backend.ProfileController.searchProfilesJson().url;
    table = new EATable('dtPeople', tableModal, getURL, populate, showErrors);
    table.initRowClicks(function () {
        window.location = this.dataset.href;
    });
});

/**
 * Adds a person's information to the given table
 *
 * @param {Object} json of people
 */
function populate(json) {
    const rows = [];
    for (const person of json) {
        const profile = profileRouter.controllers.frontend.ProfileController.index(
            person.userId).url;
        const firstName = person.firstName;
        const lastName = person.lastName;
        const gender = person.gender;
        const age = calc_age(Date.parse(person.dateOfBirth));
        const row = getNationalityAndTravellerStrings(person)
        .then(natAndTravArray => {
            return [firstName, lastName, gender, age,
                natAndTravArray[0], natAndTravArray[1],
                profile]
        });
        rows.push(row)

    }
    return Promise.all(rows).then(finishedRows => {
        return finishedRows
    });
}

/**
 * Gets the nationality and traveller types of a person as strings in a list.
 * @param {Object} people Json object including a person's details
 */
function getNationalityAndTravellerStrings(people) {
    return arrayToString(people.nationalities, 'name',
        countryRouter.controllers.backend.CountryController.getAllCountries().url)
    .then(nationalities => {
        return arrayToString(people.travellerTypes, 'description',
            profileRouter.controllers.backend.ProfileController.getAllTravellerTypes().url)
        .then(travellerTypes => {
            return [nationalities, travellerTypes];
        })
    })
}

/**
 * Filters the table with filtered results
 */
function filterPeopleTable() {
    let nationalityId = document.getElementById(
        'nationalities').options[document.getElementById(
        'nationalities').selectedIndex].value;
    let gender = document.getElementById('gender').value;
    let minAge = document.getElementById('minAge').value;
    let maxAge = document.getElementById('maxAge').value;
    let travellerTypeId = document.getElementById(
        'travellerTypes').options[document.getElementById(
        'travellerTypes').selectedIndex].value;
    let url = profileRouter.controllers.backend.ProfileController.searchProfilesJson(
        nationalityId, gender, minAge, maxAge, travellerTypeId).url;

    table.populateTable(url);
}

/**
 * Clears the filter and repopulates the table
 */
function clearFilter() {
    $('#gender').val('');
    $('#minAge').val(null);
    $('#maxAge').val(null);
    $(document.getElementById('nationalities')).selectpicker('val', "");
    $(document.getElementById('travellerTypes')).selectpicker('val', "");
    table.populateTable();
}

/**
 * Toggles the filter button between being visible and invisible
 */
function toggleFilterButton() {
    const toggled = $('#filterButton').css("display") === "block";
    $('#filterButton').css("display", toggled ? "none" : "block");
}