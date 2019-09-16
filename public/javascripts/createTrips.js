let requestOrder = 0;
let lastRecievedRequestOrder = -1;
let paginationHelper;

/**
 * Initializes destination and trip table and calls methods to populate
 * @param {Number} userId - ID of user to get destinations for
 */
function onPageLoad(userId) {
    paginationHelper = new PaginationHelper(1, 1, refreshData, "pagination-destinations");

    refreshData();
}

/**
 * Refreshes the destination data and populates the destination cards with this.
 */
function refreshData() {
    getDestinations().then((dests) => createDestinationCards(dests));
}

/**
 * Resets the fields of the destinations filter
 */
function clearFilter() {
    $('#searchQuery').val('');
    $('#pageSize').val(10);
}

/**
 * Takes a list of destinations and creates destination cards out of these.
 * @param {Object} an array of destinations to display
 */
function createDestinationCards(dests) {
    $("#destinationCardList").html("");
    dests.data.forEach((dest) => {
        const template = $("#destinationCardTemplate").get(0);
        const clone = template.content.cloneNode(true);
        let tags = "";
        let travellerTypes = "";

        $(clone).find("#card-header").append(dest.name);
        if (dest.primaryPhoto) {
            $(clone).find("#card-thumbnail").attr("src", "../../user_content/" + dest.primaryPhoto.thumbnailFilename);
        }
        $(clone).find("#district").append(dest.district ? dest.district : "No district");
        $(clone).find("#country").append(dest.country.name);
        $(clone).find("#destType").append(dest.destType ? dest.destType : "No type");
        $(clone).find("#card-header").attr("data-id", dest.id.toString());
        $(clone).find("#card-header").attr("id", "destinationCard" + dest.id.toString());

        if (dest.primaryPhoto) {
            $($(clone).find('#destinationCard' + dest.id.toString())).click(function () {
                addDestinationToTrip(dest.id, dest.name, dest.destType,
                    dest.district, dest.latitude, dest.longitude,
                    dest.country.id, dest.primaryPhoto);
            });
        } else {
            $($(clone).find('#destinationCard' + dest.id.toString())).click(function () {
                addDestinationToTrip(dest.id, dest.name, dest.destType,
                    dest.district, dest.latitude, dest.longitude, dest.country.id);
            });
        }

        dest.tags.forEach(item => {
            tags += item.name + ", ";
        });
        tags = tags.slice(0, -2);

        dest.travellerTypes.forEach(item => {
            travellerTypes += item.description + ", ";
        });
        travellerTypes = travellerTypes.slice(0, -2);

        $(clone).find("#travellerTypes").append(travellerTypes ? travellerTypes : "No traveller types");
        $(clone).find("#tags").append(tags ? tags : "No tags");

        $("#destinationCardList").get(0).appendChild(clone);
    });
}

/**
 * Returns a boolean of the getonlymine selector tick box
 * @returns {boolean} True if the getonlymine text box it ticked, false otherwise
 */
function getOnlyGetMine() {
    const currentValue = $('#onlyGetMine').val();
    return currentValue === "On";
}

/**
 * Returns the string of the searchQuery field.
 * @returns {string} a string of the data in the searchQuery field
 */
function getSearchQuery() {
    return $('#searchQuery').val();
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
 * Gets the destinations from the database given the query parameters set by the
 * user with the destination filters.
 * @returns {object} Returns a promise of the get request sent
 */
function getDestinations() {
    const url = new URL(destinationRouter.controllers.backend.DestinationController.getPagedDestinations().url, window.location.origin);

    // Append non-list params
    if(getSearchQuery() !== "") { url.searchParams.append("searchQuery", getSearchQuery()); }
    if(getOnlyGetMine() !== "") { url.searchParams.append("onlyGetMine", getOnlyGetMine()); }

    // Append pagination params
    url.searchParams.append("pageNum", paginationHelper.getCurrentPageNumber());
    url.searchParams.append("pageSize", getPageSize().toString());
    url.searchParams.append("sortBy", getSortBy());
    url.searchParams.append("ascending", getAscending());
    url.searchParams.append("requestOrder", requestOrder++);

    return get(url)
    .then(response => {
        return response.json()
        .then(json => {
            if (response.status !== 200) {
                toast("Error with destinations", "Cannot load destinations", "danger")
            } else {
                if(lastRecievedRequestOrder < json.requestOrder) {
                    lastRecievedRequestOrder = json.requestOrder;
                    paginationHelper.setTotalNumberOfPages(json.totalNumberPages);
                    return json;
                }
            }
        });
    });
}

/**
 * Hides Create trip button if trip is empty
 */
function checkTripListEmpty() {
    if ($("#list").children().length > 1) {
        $("#createTripButton").css("display", "");
    } else if ($("#list").children().length <= 1) {
        $("#createTripButton").css("display", "none");
    }
}

/**
 * Adds destination card and fills data
 *
 * @param id Id of the destination
 * @param name Name of the destination
 * @param type Type of the destination
 * @param district District of the destination
 * @param latitude Latitude of the destination
 * @param longitude Longitude of the destination
 * @param countryId CountryID of the destination
 */
function addDestinationToTrip(id, name, type, district, latitude, longitude,
    countryId, primaryPhoto=null) {
    let cards = $("#list").sortable('toArray');
    let cardId = 0;
    let image = "/assets/images//default-destination-primary.png";
    if (primaryPhoto) {
         image = "../../user_content/" + primaryPhoto.thumbnailFilename;
    }
    // Finds id not used
    while (cards.includes(cardId.toString())) {
        cardId++;
    }

    getCountryNameById(countryId).then(countryName => {
        document.getElementById('list').insertAdjacentHTML('beforeend',
            '<div class="card card-dest-for-trip flex-row" id= ' + id + ' >\n'
            + '                                <label id=' + id + '></label>\n'
            + '                                <div class="card-header border-0" style="height: 100%">\n'
            + '                                    <img src=' + image + ' style="height: 100%"; alt="Happy travellers">\n'
            + '                                </div>\n'
            + '\n'
            + '                                <div class="container">\n'
            + '                                    <div id="topCardBlock" class="row">\n'
            + '                                        <h4 class="card-title card-title-dest-for-trip"> ' + name + ' </h4>\n'
            + '                                        <div id="removeTrip" onclick="setDestinationToRemove(' + id + ')"></div>\n'
            + '                                    </div>\n'
            + '                                    <div class="card-block card-block-dest-for-trip px-2 row">\n'
            + '                                        <div id="left" class="col-5">\n'
            + '                                            <p id="destinationDetails" class="card-text" id="card-text">\n'
            + '                                                <strong>Type: </strong>\n'
            + '                                                ' + type + ''
            + '                                                <br/>\n'
            + '                                                <strong>District: </strong>\n'
            + '                                                ' + district + ''
            + '                                                <br/>\n'
            + '                                                <strong>Latitude: </strong>\n'
            + '                                                ' + latitude + ''
            + '                                                <br/>\n'
            + '                                                <strong>Longitude: </strong>\n'
            + '                                                ' + longitude + ''
            + '                                                <br/>\n'
            + '                                                <strong>Country: </strong>\n'
            + '                                                <label id="countryField">' + countryName + '</label>\n'
            + '                                            </p>\n'
            + '                                        </div>\n'
            + '                                        <div id="right" class="col-7">\n'
            + '                                            <form id="arrivalDepartureForm">\n'
            + '                                                <div class="row">\n'
            + '                                                    <div class="col date-columns">\n'
            + '                                                        <div>Arrival</div>\n'
            + '                                                        <div id="arrival">\n'
            + '                                                            <em class="fas prefix grey-text"></em>\n'
            + '                                                            <input id="arrivalDate" type="date" name="arrivalDate" class="form-control validate">\n'
            + '                                                            <input id="arrivalTime" type="time" name="arrivalTime" class="form-control validate">\n'
            + '                                                            <label id="arrivalError"></label>\n'
            + '                                                        </div>\n'
            + '                                                    </div>\n'
            + '\n'
            + '                                                    <div class="col date-columns">\n'
            + '                                                        <div>Departure</div>\n'
            + '                                                        <div id="depart">\n'
            + '                                                            <em class="fas prefix grey-text"></em>\n'
            + '                                                            <input id="departureDate" type="date" name="arrivalDate" class="form-control validate">\n'
            + '                                                            <input id="departureTime" type="time" name="arrivalTime" class="form-control validate">\n'
            + '                                                            <label id="departureError"></label>\n'
            + '                                                        </div>\n'
            + '                                                    </div>\n'
            + '                                                </div>\n'
            + '                                            </form>\n'
            + '                                            <div class="text-center">\n'
            + '                                                <label id="destinationError"></label><br/>\n'
            + '                                            </div>\n'
            + '                                        </div>\n'
            + '                                    </div>\n'
            + '                                </div>\n'
            + '                            </div>'


        );
        checkTripListEmpty();
    });
}

/**
 * Removes card with given id
 */
function removeDestinationFromTrip() {
    const cardId = $('#removeDestinationFromTripModal').attr("destId");
    $('#' + cardId).remove();
    checkTripListEmpty();
}

/**
 * Toggles the privacy toggle button text between public and private.
 */
function toggleTripPrivacy() {
    if ($('#tripPrivacyStatus').is(':checked')) {
        $('#tripPrivacyStatus').siblings('label').html('Public');
    } else {
        $('#tripPrivacyStatus').siblings('label').html('Private');
    }
}

/**
 * Creates trip and posts to API
 * @param {string} redirect - URI to redirect page
 * @param {Number} userId - the id of the current user
 */
function createTrip(redirect, userId) {
    // Building request body
    $("#createTripButton").prop('disabled', true);
    let listItemArray = Array.of(document.getElementById("list").children);
    let tripDataList = [];

    for (let i = 0; i < listItemArray[0].length; i++) {
        tripDataList.push(listItemToTripData(listItemArray[0][i], i));
    }

    const tripTagObjects = createTripTagPicker.getTags().map((tag) => {
        return {
            name:tag
        }
    });

    let tripData = {
        "userId": userId,
        "tripDataList": tripDataList,
        "tags":tripTagObjects
    };

    const tripPrivacy = $('#tripPrivacy').html();
    tripData["isPublic"] = tripPrivacy === "Public";

    // Setting up undo/redo
    const URL = tripRouter.controllers.backend.TripController.insertTrip().url;
    const handler = function (status, json) {
        if (status !== 200) {
            $("#createTripButton").prop('disabled', false);
            showTripErrors(json);
        } else {
            window.location.href = redirect;
        }
    }.bind({redirect});
    const inverseHandler = (status, json) => {
        if (status === 200) {
            // Currently no implementation as undo for creating trip is not being used
        }
    };
    const reqData = new ReqData(requestTypes["CREATE"], URL, handler, tripData);

    // Send create trip request and store undo request
    undoRedo.sendAndAppend(reqData, inverseHandler);
}

/**
 * Gets data from trip and creates json
 * @param {Object} listItem - Html element
 * @param {Number} index - Index of trip data
 */
function listItemToTripData(listItem, index) {
    // Create json object to store data
    let json = {};

    // Assign position to be equal to given index
    json["position"] = index;

    // Read destination id
    json["destination"] = {
        "id": listItem.getElementsByTagName('label')[0].getAttribute("id")
    };

    let DTInputs = listItem.getElementsByTagName("input");

    try {
        json["arrivalTime"] = formatDateTime(DTInputs[0].value,
            DTInputs[1].value);
    } catch {
        json["arrivalTime"] = null;
    }

    try {
        json["departureTime"] = formatDateTime(DTInputs[2].value,
            DTInputs[3].value);
    } catch {
        json["departureTime"] = null;
    }

    return json;
}

/**
 * Formats the date retrieved from the destination cards
 * @param date Date entered by user
 * @param time Time entered by user
 * @returns {string|null} String representation of valid date or null if fields not filled in
 */
function formatDateTime(date, time) {
    if (date.length === 10 && time.length === 5) {
        return date + "T" + time + ":00.000";
    } else if (date.length === 10) {
        return date + "T" + "00:00:00.000";
    } else {
        return null;
    }
}

/**
 * Gathers trip data and sends to API to update
 * @param {string} uri - API URI to update trip
 * @param {string} redirect - URI to redirect if successful
 * @param {Number} tripId - ID of trip to update
 * @param {Number} userId - User ID of trip owner
 */
function updateTrip(uri, redirect, tripId, userId) {
    let listItemArray = Array.of(document.getElementById("list").children);
    let tripDataList = [];

    for (let i = 0; i < listItemArray[0].length; i++) {
        tripDataList.push(listItemToTripData(listItemArray[0][i], i));
    }

    const tripTagObjects = createTripTagPicker.getTags().map((tag) => {return {name:tag}});

    let tripData = {
        "id": tripId,
        "userId": userId,
        "trip": {
            "id": tripId
        },
        "tripDataList": tripDataList,
        "tags": tripTagObjects
    };

    const tripPrivacy = $('#tripPrivacy').html();
    tripData["isPublic"] = tripPrivacy === "Public";

    const handler = function (status, json) {
        if (status === 400) {
            $("#createTripButton").prop('disabled', false);
            showTripErrors(json);
        } else if (status === 200) {
            window.location.href = redirect;
        } else {
            document.getElementById(
                "destinationError").innerHTML = "Error(s): "
                + Object.values(json).join(", ");
        }
    }.bind({redirect});

    const reqData = new ReqData(requestTypes["UPDATE"], uri, handler, tripData);

    undoRedo.sendAndAppend(reqData);
}

/**
 * Sets the destination that will be removed from the trip when the remove
 * destination action in the modal is confirmed.
 *
 * @param cardId the id of the destination card to be removed.
 */
function setDestinationToRemove(cardId) {
    let destTripModal = $('#removeDestinationFromTripModal');
    destTripModal.attr("destId", cardId);
    destTripModal.modal('show');
}

/**
 * Create destination modal form cancel button click handler.
 */
$('#CreateDestinationCancelButton').click(function () {
    $('#createDestinationModal').modal('hide');
    resetDestinationModal();
});



