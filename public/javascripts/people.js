let requestOrder = 0;
let lastRecievedRequestOrder = -1;

/**
 * Capitalise first letter of string
 * @param {String} string - input string to capitalise
 */
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
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
 * Gets the ids all nationalities currently selected in the filter section
 * @returns {*[]} Array of nationality ids
 */
function getSelectedNationalityIds() {
    return [document.getElementById('nationalities').options[document.getElementById('nationalities').selectedIndex].value];
}

/**
 * Gets the ids all traveller types currently selected in the filter section
 * @returns {*[]} Array of traveller type ids
 */
function getSelectedTravellerTypeIds() {
    return [document.getElementById(
        'travellerTypes').options[document.getElementById(
        'travellerTypes').selectedIndex].value];
}

/**
 * Gets all genders currently selected in the filter section
 * @returns {*[]} Array of genders, all items will be in ('Male', 'Female', 'Other')
 */
function getSelectedGenders() {
    return [$('#gender').val()];
}

/**
 * Get the value of the currently selected minAge
 * @returns {*}
 */
function getSelectedMinAge() {
    return $('#minAge').val();
}

/**
 * Get the value of the currently selected maxAge
 * @returns {*}
 */
function getSelectedMaxAge() {
    return $('#maxAge').val();
}

/**
 * Get the value of the current page number being viewed
 * @returns {number} The page number being viewed
 */
function getPageNumber() {
    return 1;
}

/**
 * Get the value of the number of results to show per page
 * @returns {number} The number of results shown per page
 */
function getPageSize() {
    return $('#pageSize').val();
}

/**
 * Returns the name of the db column to search by
 * @returns {string} Name of db column to search by
 */
function getSortBy() {
    return $('#sortBy').val();
}

/**
 * Gets whether or not to sort by ascending
 * @returns {string} Either 'true' or 'false', where true is ascending, false is descending
 */
function getAscending() {
    return $('#ascending').val();
}

/**
 * Filters the table with filtered results
 */
function getPeopleResults() {
    const url = new URL(profileRouter.controllers.backend.ProfileController.searchProfilesJson().url, window.location.origin);

    // Append list params
    getSelectedNationalityIds().forEach((item) => { if(item !== "") url.searchParams.append("nationalityIds", item) });
    getSelectedTravellerTypeIds().forEach((item) => { if(item !== "") url.searchParams.append("travellerTypeIds", item) });
    getSelectedGenders().forEach((item) => { if(item !== "") url.searchParams.append("genders", item) });

    // Append non-list params
    if(getSelectedMinAge() !== "") { url.searchParams.append("minAge", getSelectedMinAge()); }
    if(getSelectedMaxAge() !== "") { url.searchParams.append("maxAge", getSelectedMaxAge()); }

    // Append pagination params
    url.searchParams.append("pageNum", getPageNumber().toString());
    url.searchParams.append("pageSize", getPageSize().toString());
    url.searchParams.append("sortBy", getSortBy());
    url.searchParams.append("ascending", getAscending());
    url.searchParams.append("requestOrder", requestOrder++);

    get(url).then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                toast("Error", "Error fetching people data", "danger")
            } else {
                if(lastRecievedRequestOrder < json.requestOrder) {
                    $("#peopleCardsList").html("");
                    lastRecievedRequestOrder = json.requestOrder;
                    json.data.forEach((item) => {
                        createPeopleCard(item);
                    });
                }
            }
        })
    });
}

/**
 * Creates a html people card cloning the template in the people.scala.html
 *
 * @param person is Json profile object
 */
function createPeopleCard(person) {
    let template = document.getElementById("personCardTemplate");
    let clone = template.content.cloneNode(true);

    $(clone).find("#card-header").append(`${person.firstName} ${person.lastName}`);
    $(clone).find("#card-thumbnail").attr("src", person.profilePhoto === null ? "/assets/images/default-profile-picture.jpg" : "user_content/" + person.profilePhoto.thumbnailFilename);
    $(clone).find("#age").append("Age: " + person.dateOfBirth);
    $(clone).find("#gender").append("Gender: " + person.gender);
    $(clone).find("#nationalities").append("Nationalities: " + person.nationalities.name);
    $(clone).find("#traveller-type").append("Traveller Types: " + person.travellerTypes);

    document.getElementById("peopleCardsList").appendChild(clone);
}

/**
 * Clears the filter and repopulates the table
 */
function clearFilter() {
    $('#gender').val('');
    $('#minAge').val(null);
    $('#maxAge').val(null);
    $(document.getElementById('nationalities')).picker('set', "");
    $(document.getElementById('travellerTypes')).picker('set', "");
    table.populateTable();
}

/**
 * Toggles the filter button between being visible and invisible
 */
function toggleFilterButton() {
    const toggled = $('#filterButton').css("display") === "block";
    $('#filterButton').css("display", toggled ? "none" : "block");
}