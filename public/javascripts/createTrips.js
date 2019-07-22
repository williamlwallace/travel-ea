let countryDict = {};
let destinationTable;

/**
 * Initializes destination and trip table and calls methods to populate
 * @param {Number} userId - ID of user to get destinations for
 */
function onPageLoad(userId) {
    const tripGetURL = destinationRouter.controllers.backend.DestinationController.getAllDestinations(
        userId).url;
    const tableModal = {
        createdRow: function (row, data, dataIndex) {
            $(row).attr('id', data[data.length - 2]);
            $(row).attr('data-countryId', data[data.length - 1]);
        }
    };
    destinationTable = new EATable('destTable', tableModal, tripGetURL,
        populate, showTripErrors)
    destinationTable.initButtonClicks({
        6: addDestClick
    });
}

function populate(json) {
    const rows = [];
    for (const destination of json) {
        const id = destination.id;
        const name = destination.name;
        const type = destination.destType;
        const district = destination.district;
        const latitude = destination.latitude;
        const longitude = destination.longitude;
        const country = destination.country.name;
        const button = '<button id="addDestination" class="btn btn-popup" type="button">Add</button>';
        const row = [name, type, district, latitude, longitude,
            country, button, id, destination.country.id];
        rows.push(row);
    }
    return rows;
}

/**
 * Click listener that handles clicks in destination table
 *
 * @param {Object} button button element
 * @param {Object} tableAPI table
 * @param {Number} cellId id of containing cell
 */
function addDestClick(button, tableAPI, cellId) {
    let name = tableAPI.cell($(button).parents('tr'), 0).data();
    let district = tableAPI.cell($(button).parents('tr'), 1).data();
    let type = tableAPI.cell($(button).parents('tr'), 2).data();
    let latitude = tableAPI.cell($(button).parents('tr'), 3).data();
    let longitude = tableAPI.cell($(button).parents('tr'), 4).data();
    let countryId = $(button).parents('tr').attr("data-countryId");
    let id = $(button).parents('tr').attr('id');

    addDestinationToTrip(id, name, district, type, latitude, longitude,
        countryId);
}

/**
 * Add destination to database
 * @param {string} url - API URI to add destination
 * @param {string} redirect - URI of redirect page
 * @param {Number} userId - The id of the current user
 */
function addDestination(url, redirect, userId) {
    // Read data from destination form
    const formData = new FormData(
        document.getElementById("addDestinationForm"));

    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});

    // Convert lat and long to double values, and id to int
    data.latitude = parseFloat(data.latitude);
    data.longitude = parseFloat(data.longitude);
    data.countryId = parseInt(data.countryId);
    data.user = {
        id: userId
    };

    // Convert country id to country object
    data.country = {"id": data.countryId};

    // Post json data to given url
    addNonExistingCountries([data.country]).then(result => {
        // Post json data to given url
        post(url, data)
        .then(response => {

            // Read response from server, which will be a json object
            response.json()
            .then(json => {
                if (response.status !== 200) {
                    showErrors(json);
                } else {
                    toast("Destination Created!",
                        "The new destination will be added to the table.",
                        "success");
                    $('#createDestinationModal').modal('hide');

                    // Add row to table
                    data.id = json;
                    addRow(data);
                }
            });
        });
    });
}

/**
 * Adds a row to the destinations table with the given data
 * @param {Object} data - Data object to be added to table
 */
function addRow(data) {
    const id = data.id;
    const name = data.name;
    const type = data.destType;
    const district = data.district;
    const latitude = data.latitude;
    const longitude = data.longitude;
    let country = data.country.id;

    // Set country name
    let countries = document.getElementById(
        "countryDropDown").getElementsByTagName("option");
    for (let i = 0; i < countries.length; i++) {
        if (parseInt(countries[i].value) === data.country.id) {
            country = countries[i].innerText;
            break;
        }
    }

    const button = '<button id="addDestination" class="btn btn-popup" type="button">Add</button>';
    const row = [name, type, district, latitude, longitude, country, button, id,
        data.country.id];
    destinationTable.add(row);
}

/**
 * Maps countries into destinations
 * @param {Object} countryDict - Dictionary of Countries
 */
function updateDestinationsCountryField(countryDict) {
    let tableBody = document.getElementsByTagName('tbody')[0];
    let rowList = tableBody.getElementsByTagName('tr');

    for (let i = 0; i < rowList.length; i++) {
        let dataList = rowList[i].getElementsByTagName('td');

        for (let j = 0; j < dataList.length; j++) {
            if (dataList[j].getAttribute("id") === "country") {
                dataList[j].innerHTML = countryDict[parseInt(
                    rowList[i].getAttribute("id"))];
            }
        }
    }
}

/**
 * Maps Countries into the Card fields
 * @param {Object} countryDict - Dictionary of Countries
 */
function updateCountryCardField(countryDict) {
    let cards = Array.of(document.getElementById("list").children)[0];

    for (let i = 0; i < cards.length; i++) {
        let labels = cards[i].getElementsByTagName("label");

        for (let j in labels) {
            if (labels[j].getAttribute("id") === "countryField") {
                let destinationId = parseInt(labels[0].getAttribute("id"));
                labels[j].innerHTML = countryDict[destinationId];
                break;
            }
        }
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
    countryId) {
    let cards = $("#list").sortable('toArray');
    let cardId = 0;

    // Finds id not used
    while (cards.includes(cardId.toString())) {
        cardId++;
    }

    getCountryNameById(countryId).then(countryName => {
        document.getElementById('list').insertAdjacentHTML('beforeend',
            '<div class="card flex-row" id=' + cardId + '>\n' +
            '<label id=' + id + '></label>' +
            '<div class="card-header border-0" style="height: 100%">\n' +
            '<img src="https://www.ctvnews.ca/polopoly_fs/1.1439646.1378303991!/httpImage/image.jpg_gen/derivatives/landscape_620/image.jpg" style="height: 100%";>\n'
            +    // TODO: Store default card image rather than reference
            '</div>\n' +
            '<div class="card-block px-2">\n' +
            '<div id="topCardBlock">\n' +
            '<h4 class="card-title">' + name + '</h4>\n' +
            '        <button id="removeTrip" type="button" onclick="removeDestinationFromTrip('
            + cardId + ')"></button>\n' +
            '</div>\n' +
            '<div id="left">\n' +
            '<p class="card-text" id="card-text">' +
            '<b>Type: </b> ' + type + '<br/>' +
            '<b>District: </b> ' + district + '<br/>' +
            '<b>Latitude: </b>' + latitude + '<br/>' +
            '<b>Longitude: </b>' + longitude + '<br/>' +
            '<b>Country: </b>' + countryName +
            '</p>\n' +
            '</div>' +
            '<div id="right">\n' +
            '<form id="arrivalDepartureForm">\n' +
            '                <div class="modal-body mx-3">\n' +
            '                    <div id="arrival">Arrival\n' +
            '                        <i class="fas prefix grey-text"></i>\n' +
            '                        <input id="arrivalDate" type="date" name="arrivalDate" class="form-control validate"><input id="arrivalTime" type="time" name="arrivalTime" class="form-control validate">\n'
            +
            '                    </div>\n' +
            '                    <div id="depart">Departure\n' +
            '                        <i class="fas prefix grey-text"></i>\n' +
            '                        <input id="departureDate" type="date" name="departureDate" class="form-control validate"><input id="departureTime" type="time" name="departureTime" class="form-control validate">\n'
            +
            '                    </div>\n' +
            '                </div>\n' +
            '            </form>\n' +
            '<div style="text-align: center;">\n' +
            '<label id="destinationError" class="error-messages" style="font-size: 15px;"></label>\n'
            +
            '<br/>\n' +
            '</div>\n' +
            '</div>\n' +
            '</div>'
        );
    });
}

/**
 * Removes card with given id
 * @param {Number} cardId - Id of card
 */
function removeDestinationFromTrip(cardId) {
    let destinations = Array.of(document.getElementById("list").children)[0];

    for (let i = 0; i < destinations.length; i++) {
        if (parseInt(destinations[i].getAttribute("id")) === cardId) {
            destinations[i].parentNode.removeChild(destinations[i]);
            break;
        }
    }
}

function toggleTripPrivacy() {
    let currentPrivacy = document.getElementById("tripPrivacyStatus").innerHTML;

    if (currentPrivacy === "Make Public") {
        document.getElementById("tripPrivacyStatus").innerHTML = "Make Private";
    }
    else if (currentPrivacy === "Make Private") {
        document.getElementById("tripPrivacyStatus").innerHTML = "Make Public";
    }
}

/**
 * Creates trip and posts to API
 * @param {string} uri - API URI to add trip
 * @param {string} redirect - URI to redirect page
 * @param {Number} userId - the id of the current user
 */
function createTrip(uri, redirect, userId) {
    $("#createTripButton").prop('disabled', true);
    let listItemArray = Array.of(document.getElementById("list").children);
    let tripDataList = [];

    for (let i = 0; i < listItemArray[0].length; i++) {
        tripDataList.push(listItemToTripData(listItemArray[0][i], i));
    }

    let tripData = {
        "userId": userId,
        "tripDataList": tripDataList
    };

    let tripPrivacy = document.getElementById("tripPrivacyStatus").innerHTML;

    // Value of 1 for public, 0 for private
    tripData["isPublic"] = tripPrivacy === "Make Private";

    post(uri, tripData).then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if (response.status === 400) {
                $("#createTripButton").prop('disabled', false);
                showTripErrors(json);
            } else if (response.status === 200) {
                window.location.href = redirect;
            }
        });
    });
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
    }
    catch {
        json["arrivalTime"] = null;
    }

    try {
        json["departureTime"] = formatDateTime(DTInputs[2].value,
            DTInputs[3].value);
    }
    catch {
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
    }
    else if (date.length === 10) {
        return date + "T" + "00:00:00.000";
    }
    else {
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

    let tripData = {
        "id": tripId,
        "userId": userId,
        "trip": {
            "id": tripId
        },
        "tripDataList": tripDataList
    };

    let tripPrivacy = document.getElementById("tripPrivacyStatus").innerHTML;

    // Value of 1 for public, 0 for private
    tripData["isPublic"] = tripPrivacy === "Make Private";

    put(uri, tripData).then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if (response.status === 400) {
                showTripErrors(json);
            } else if (response.status === 200) {
                window.location.href = redirect;
            } else {
                document.getElementById(
                    "destinationError").innerHTML = "Error(s): "
                    + Object.values(json).join(", ");
            }
        });
    });
}