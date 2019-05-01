// METHODS FOR CREATE TRIPS

let countryDict = {};

// Runs get countries method, then add country options to drop down
function fillCountryInfo(getCountriesUrl) {
    // Run a get request to fetch all destinations
    get(getCountriesUrl)
    // Get the response of the request
        .then(response => {
            // Convert the response to json
            response.json().then(data => {
                // Json data is an array of destinations, iterate through it
                for(let i = 0; i < data.length; i++) {
                    // Also add the item to the dictionary
                    countryDict[data[i]['id']] = data[i]['name'];
                }

                // Now fill the drop down box, and list of destinations
                updateCountryCardField();
                fillDropDown();
                updateDestinationsCountryField();
            });
        });
}

function updateCountryCardField() {
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

function updateDestinationsCountryField() {
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

function fillDropDown() {
    for(let key in countryDict) {
        // For each destination, make a list element that is the json string of object
        let item = document.createElement("OPTION");
        item.innerHTML = countryDict[key];
        item.value = key;
        // Add list element to drop down list
        document.getElementById("countryDropDown").appendChild(item);
    }
}


function newDestination(url) {
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
    // Post json data to given url
    post(url,data)
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

function addDestinationToTrip(dest) {
    let cards = $( "#list" ).sortable('toArray');
    let cardId = 0;

    while (cards.includes(cardId.toString())) {
        cardId += 1;
    }
    document.getElementById('list').insertAdjacentHTML('beforeend',
        '<div class="card flex-row" id=' + cardId + '>\n' +
            '<label id=' + dest[0] + '></label>' +
        '<div class="card-header border-0" style="height: 100%">\n' +
            '<img src="https://www.ctvnews.ca/polopoly_fs/1.1439646.1378303991!/httpImage/image.jpg_gen/derivatives/landscape_620/image.jpg" style="height: 100%";>\n' +
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
    '                        <label id="arrivalError"></label>\n' +
    '                    </div>\n' +
    '                    <div id="depart">Departure\n' +
    '                        <i class="fas prefix grey-text"></i>\n' +
    '                        <input id="departureDate" type="date" name="departureDate" class="form-control validate"><input id="departureTime" type="time" name="departureTime" class="form-control validate">\n' +
    '                        <label id="departureError"></label>\n' +
    '                    </div>\n' +
    '                </div>\n' +
    '            </form>\n' +
    '            <label id="destinationError" class="error-messages"></label><br/>\n' +
            '</div>\n' +
        '</div>'
    );
}

function removeDestinationFromTrip(cardId) {
    let destinations = Array.of(document.getElementById("list").children)[0];

    for (let i = 0; i < destinations.length; i++) {
        if (parseInt(destinations[i].getAttribute("id")) === cardId) {
            destinations[i].parentNode.removeChild(destinations[i]);
            break;
        }
    }
}

function createTrip(url, redirect) {
    let listItemArray = Array.of(document.getElementById("list").children);
    let tripDataList = [];

    for (let i = 0; i < listItemArray[0].length; i++) {
        tripDataList.push(listItemToTripData(listItemArray[0][i], i));
    }

    let tripData = {
        "tripDataList": tripDataList
    };

    let privacy = document.getElementById("tripPrivacy").checked;

    if (privacy) {
        tripData["tripPrivacy"] = 1;
    }
    else {
        tripData["tripPrivacy"] = 0;
    }

    post(url, tripData).then(response => {
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
        json["arrivalTime"] = DTInputs[0].value + "T" + DTInputs[1].value + ":00.000";

        if (json["arrivalTime"].length <= 18) {
            json["arrivalTime"] = null;
        }
    }
    catch {
        json["arrivalTime"] = null;
    }

    try {
        json["departureTime"] = DTInputs[2].value + "T" + DTInputs[3].value + ":00.000";

        if (json["departureTime"].length <= 18) {
            json["departureTime"] = null;
        }
    }
    catch {
        json["departureTime"] = null;
    }

    return json;
}

function showErrors(json) {
    let listItemArray = Array.of(document.getElementById("list").children);

    for (const key of Object.keys(json)) {
        let errors = json[key];

        if (errors.endsWith(", ")) {
            errors = errors.substr(0, Math.max(0, json[key].length - 2));
        }

        let errorList = errors.split(", ");

        document.getElementById("tripError").innerHTML = errorList[0];
        break;

        // if (errorList[0] != null && !isNaN(parseInt(key))) {
        //     let labels = listItemArray[0][parseInt(key)].getElementsByTagName("label");
        //
        //     for (let i in labels) {
        //         if (labels[i].getAttribute("id") === "destinationError") {
        //             listItemArray[0][parseInt(key)].getElementsByTagName("label")[2].innerHTML = errorList[0];
        //             break;
        //         }
        //     }
        // }
        // else if (errorList[0] != null) {
        //     document.getElementById("tripError").innerHTML = errorList[0];
        // }
    }
}

// METHODS FOR TRIPS

function viewTrip(url) {
    window.location.href = url;
}

function updateTrip(url, redirect, tripId) {
    let listItemArray = Array.of(document.getElementById("list").children);
    let tripDataList = [];

    for (let i = 0; i < listItemArray[0].length; i++) {
        tripDataList.push(listItemToTripData(listItemArray[0][i], i));
    }

    let tripData = {
        "id": tripId,
        "trip": {    // TODO: Is this necessary?
            "id": tripId
        },
        "tripDataList": tripDataList
    };

    put(url, tripData).then(response => {
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