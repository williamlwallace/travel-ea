let countryDict = {};

/**
 * Gets all countries and fills into dropdown
 * @param {stirng} getCountriesUrl - get all countries URI
 */
function fillCountryInfo(getCountriesUrl) {
    // Run a get request to fetch all destinations
    get(getCountriesUrl)
    // Get the response of the request
        .then(response => {
            // Convert the response to json
            response.json()
                .then(data => {
                    // Json data is an array of destinations, iterate through it
                    let countryDict = {};
                    for(let i = 0; i < data.length; i++) {
                        // Also add the item to the dictionary
                        countryDict[data[i]['id']] = data[i]['name'];
                    }
                    // Now fill the drop down box, and list of destinations
                    fillDropDown("countryDropDown", countryDict);
                });
        });
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
                dataList[j].innerHTML = countryDict[parseInt(rowList[i].getAttribute("id"))]; // No idea why tds[i].value is not working
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
 * Send new destination data to API
 * @param {string} uri - API URI to send destination
 */
function newDestination(uri) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("addDestinationForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});

    // Convert lat and long to double values, and id to int
    data.latitude = parseFloat(data.latitude);
    data.longitude = parseFloat(data.longitude);
    // Convert country id to country object
    data.countryId = parseInt(data.countryId);
    data.country = {"id": data.countryId};
    delete data.countryId;

    console.log(data);
    // Post json data to given uri
    post(uri,data)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
            .then(json => {
            if (response.status != 200) {
                showErrors(json);
            } else {
                // TODO: Get toggle working
                document.getElementById("modalContactForm").setAttribute("aria-hidden", "true");
            }
        });
    });
}

/**
 * Adds destination card and fills data
 * @param {Object} dest - List of destination data
 */
function addDestinationToTrip(dest) {
    let cards = $( "#list" ).sortable('toArray');
    let cardId = 0;

    // Finds id not used
    while (cards.includes(cardId.toString())) {
        cardId++;
    }

    document.getElementById('list').insertAdjacentHTML('beforeend',
        '<div class="card flex-row" id=' + cardId + '>\n' +
            '<label id=' + dest[0] + '></label>' +
        '<div class="card-header border-0" style="height: 100%">\n' +
            '<img src="https://www.ctvnews.ca/polopoly_fs/1.1439646.1378303991!/httpImage/image.jpg_gen/derivatives/landscape_620/image.jpg" style="height: 100%";>\n' +    // TODO: Store default card image rather than reference
        '</div>\n' +
        '<div class="card-block px-2">\n' +
            '<div id="topCardBlock">\n' +
                '<h4 class="card-title">' + dest[1] + '</h4>\n' +
        '        <button id="removeTrip" type="button" onclick="removeDestinationFromTrip(' + cardId + ')"></button>\n' +
            '</div>\n' +
            '<div id="left">\n' +
                '<p class="card-text" id="card-text">' +
                    '<b>Type: </b> '+ dest[2] + '<br/>' +
                    '<b>District: </b> '+ dest[3] + '<br/>' +
                    '<b>Latitude: </b>' + dest[4] + '<br/>' +
                    '<b>Longitude: </b>' + dest[5] + '<br/>' +
                    '<b>Country: </b>' + countryDict[dest[6]] +
                '</p>\n' +
            '</div>' +
            '<div id="right">\n' +
                '<form id="arrivalDepartureForm">\n' +
    '                <div class="modal-body mx-3">\n' +
    '                    <div id="arrival">Arrival\n' +
    '                        <i class="fas prefix grey-text"></i>\n' +
    '                        <input id="arrivalDate" type="date" name="arrivalDate" class="form-control validate"><input id="arrivalTime" type="time" name="arrivalTime" class="form-control validate">\n' +
    '                    </div>\n' +
    '                    <div id="depart">Departure\n' +
    '                        <i class="fas prefix grey-text"></i>\n' +
    '                        <input id="departureDate" type="date" name="departureDate" class="form-control validate"><input id="departureTime" type="time" name="departureTime" class="form-control validate">\n' +
    '                    </div>\n' +
    '                </div>\n' +
    '            </form>\n' +
                '<div style="text-align: center;">\n' +
                    '<label id="destinationError" class="error-messages" style="font-size: 15px;"></label>\n' +
                    '<br/>\n' +
                '</div>\n' +
            '</div>\n' +
        '</div>'
    );
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
 */
function createTrip(uri, redirect, userId) {
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
    if (tripPrivacy === "Make Private") {
        tripData["privacy"] = 1;
    }
    else {
        tripData["privacy"] = 0;
    }

    post(uri, tripData).then(response => {
        // Read response from server, which will be a json object
        response.json()
            .then(json => {
                if (response.status === 400) {
                    showErrors(json);
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
        json["arrivalTime"] = formatDateTime(DTInputs[0].value, DTInputs[1].value);
    }
    catch {
        json["arrivalTime"] = null;
    }

    try {
        json["departureTime"] = formatDateTime(DTInputs[2].value, DTInputs[3].value);
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
 * Speciality show errors function for trips
 * @param {Object} json - Error Response Json
 */
function showErrors(json) {
    // Gets all the error key identifiers
    let keys = Object.keys(json);

    // Resets and sets tripError label
    let tripError = document.getElementById("tripError");
    if (keys.includes("trip")) {
        tripError.innerHTML = '<div class="alert alert-danger" role="alert">' +
            '<a class="close" data-dismiss="alert">×</a>' +
            '<span>'+ json["trip"] +'</span></div>';
    }
    else {
        tripError.innerHTML = "";
    }

    // Resets and sets the card error labels
    let listItemArray = Array.of(document.getElementById("list").children)[0];

    for (let j = 0; j < listItemArray.length; j++) {
        let labels = listItemArray[j].getElementsByTagName("label");

        for (let i = 0; i < labels.length; i++) {
            if (labels[i].getAttribute("id") === "destinationError") {
                if (keys.includes(j.toString())) {
                    labels[i].innerText = json[j.toString()];
                }
                else {
                    labels[i].innerText = "";
                }
                break;
            }
        }
    }
}

// METHODS FOR TRIPS

/**
 * Relocate to individual trip page
 * @param {stirng} uri - URI of trip
 */
function viewTrip(uri) {
    window.location.href = uri;
}

/**
 * Gathers trip data and sends to API to update
 * @param {string} uri - API URI to update trip
 * @param {stirng} redirect - URI to redirect if succesful
 * @param {Number} tripId - ID of trip to update
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
    if (tripPrivacy === "Make Private") {
        tripData["privacy"] = 1;
    }
    else {
        tripData["privacy"] = 0;
    }

    put(uri, tripData).then(response => {
        // Read response from server, which will be a json object
        response.json()
            .then(json => {
                if (response.status === 400) {
                    showErrors(json);
                } else if (response.status === 200) {
                    window.location.href = redirect;
                } else {
                    document.getElementById("destinationError").innerHTML = "Error(s): " + Object.values(json).join(", ");
                }
            });
    });
}