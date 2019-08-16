let requestOrder = 0;
let lastRecievedRequestOrder = -1;
let pageNum = 1;
let totalNumberPages = 0;

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
 * Changes the page we are viewing (if possible) and reloads data
 *
 * @param desiredPageNumber Page number to change to
 */
function goToPage(desiredPageNumber) {
    if(desiredPageNumber > totalNumberPages || desiredPageNumber < 1) {
      toast("No such page", "The page you have tried to go to does not exist", "danger");
      return;
    }
    pageNum = desiredPageNumber;

    getPeopleResults();
}

/**
 * Based on what page you are on, creates the correct pagination bar with up to 5 page buttons
 */
function createPaginationBar() {
    let pageNumbers = [];
    // Less than 5 pages
    if(totalNumberPages <= 5) {
        for(let i = 1; i <= totalNumberPages; i++) {
            pageNumbers.push(i);
        }
    }
    // In first 3 pages, and more than 5 total
    else if(totalNumberPages >= 5 && pageNum < 3) {
        for(let i = 1; i <= 5; i++) {
            pageNumbers.push(i);
        }
    }
    // In last 3, and more than 5 total
    else if(totalNumberPages >= 5 && pageNum > totalNumberPages - 2) {
        for(let i = totalNumberPages - 4; i <= totalNumberPages; i++) {
            pageNumbers.push(i);
        }
    } else {
        for(let i = pageNum - 2; i <= pageNum + 2; i++) {
            pageNumbers.push(i);
        }
    }
    $(".pagination").html("");
    $(".pagination").append("<li class=\"page-item\" style=\"cursor: pointer\" onclick='goToPage(" + (pageNum - 1) + ")'><a class=\"page-link\">Previous</a></li>");
    pageNumbers.forEach((item) => {
        $(".pagination").append("<li class=\"page-item\" style=\"cursor: pointer\" onclick='goToPage(" + item + ")'><a class=\"page-link\">" + item + " </a></li>");
    });
    $(".pagination").append("<li class=\"page-item\" style=\"cursor: pointer\" onclick='goToPage(" + pageNum + 1 + ")'><a class=\"page-link\">Next</a></li>");
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
    url.searchParams.append("pageNum", pageNum);
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
                    totalNumberPages = json.totalNumberPages;
                    $("#peopleCardsList").html("");
                    lastRecievedRequestOrder = json.requestOrder;
                    json.data.forEach((item) => {
                        createPeopleCard(item);
                    });
                    createPaginationBar();
                    $(".card").click((element) => {
                        location.href = `/profile/${$(element.currentTarget).find("#card-header").data().id}`;
                    })
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
    const template = $("#personCardTemplate").get(0);
    const clone = template.content.cloneNode(true);
    let nationalities = "";
    let travellerTypes = "";

    $(clone).find("#card-header").append(`${person.firstName} ${person.lastName}`);
    $(clone).find("#card-thumbnail").attr("src", person.profilePhoto === null ? "/assets/images/default-profile-picture.jpg" : "user_content/" + person.profilePhoto.thumbnailFilename);
    $(clone).find("#age").append("Age: " + person.dateOfBirth);
    $(clone).find("#gender").append("Gender: " + person.gender);
    $(clone).find("#card-header").attr("data-id", person.userId.toString());

    person.nationalities.forEach(item => {
        nationalities += item.name + ", ";
    });
    nationalities = nationalities.slice(0, -2);

    person.travellerTypes.forEach(item => {
        travellerTypes += item.description + ", ";
    });
    travellerTypes = travellerTypes.slice(0, -2);

    $(clone).find("#nationalities").append("Nationalities: " + nationalities);
    $(clone).find("#traveller-type").append("Traveller Types: " + travellerTypes);

    $("#peopleCardsList").get(0).appendChild(clone);
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