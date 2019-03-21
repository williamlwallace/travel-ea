var destinationsInTrip = [];
var destCounter = 0;

function newDestination(url) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("addDestinationForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    // Post json data to given url
    post(url,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
            .then(json => {
            if (response.status != 200) {
                showErrors(json);
            } else {
                // Toggles aria-hidden to hide add destination form
                // TODO: Get toggle working
                document.getElementById("#modalContactForm").setAttribute("aria-hidden", "true");
            }
});
});
}

function addDestinationToTrip (dest) {
    console.log("destId");
    destinationsInTrip.push(dest);
    displayDestinations(dest[0], dest[1], dest[2], dest[3], dest[4], dest[5], dest[6]);
    destCounter += 1;
}

function createTrip() {
    var tripDestinations = document.getElementById("sortable");
    var destinations = tripDestinations.getElementsByClassName("sortable-card col-md-4");
    var destList = [];

    for (let i = 0; i < destinations.length; i++) {
        console.log(destinations[i]);
        destList.push(destinations[i].getAttribute("id"));
    }

    console.log(destList);

    return;
}

function displayDestinations(id, name, type, district, latitude, longitude, country) {
    document.getElementById('sortable').insertAdjacentHTML('beforeend', '<div class="sortable-card col-md-4" id=id>\n' +
            '                                <!-- Card -->\n' +
            '                            <div class="card mb-4">\n' +
            '                                    <!--Card image-->\n' +
            '                                <div class="view overlay">\n' +
            '                                    <img class="card-img-top" src="https://www.ctvnews.ca/polopoly_fs/1.1439646.1378303991!/httpImage/image.jpg_gen/derivatives/landscape_620/image.jpg" alt="Card image cap">\n' +
            '                                    <a href="#!">\n' +
            '                                        <div class="mask rgba-white-slight"></div>\n' +
            '                                    </a>\n' +
            '                                </div>\n' +
            '\n' +
            '                                    <!--Card content-->\n' +
            '                                <div class="card-body">\n' +
            '\n' +
            '                                        <!--Title-->\n' +
            '                                    <h4 class="card-title"> ' + name + '</h4>\n' +
            '                                        <!--Text-->\n' +
            '                                    <p class="card-text"><b>Type: </b> '+ type + '<br/><b>District: </b> '+ district + '<br/><b>Latitude: </b>' + latitude + '<br/><b>Longitude: </b>' + longitude + '<br/><b>Country: </b>' + country + '</p>\n' +
            '                                        <!-- Provides extra visual weight and identifies the primary action in a set of buttons -->\n' +
            '                                    <button type="button" class="btn btn-primary btn-md" data-toggle="modal" data-target="#modalEditForm">Edit</button>\n' +
            '\n' +
            '                                </div>\n' +
            '\n' +
            '                            </div>\n' +
            '                                <!-- Card -->\n' +
            '                        </div>');
}


function get(url) {
    return fetch(url, {
        method: "GET"
    })
}

let idCount = 0;
let tripId = -1;

function red(url) {
    window.location.href = url;
}

function addNewStageRow(destVal) { //, arrivalVal, departVal) {

    // Create list item to store controls
    let listItem = document.createElement("TR");
    listItem.id = idCount.toString();
    idCount++;

    // Create destination input
    let destinationInput = document.createElement("TD");
    destinationInput.setAttribute("type", "number");
    destinationInput.setAttribute("name", "destinationInput");
    destinationInput.value = destVal;

    /*
    // Create arrival time input
    let arrivalTimeInput = document.createElement("INPUT");
    arrivalTimeInput.setAttribute("type", "datetime-local");
    arrivalTimeInput.setAttribute("name", "arrivalInput");
    arrivalTimeInput.value = arrivalVal;

    // Create departure time input
    let departureTimeInput = document.createElement("INPUT");
    departureTimeInput.setAttribute("type", "datetime-local");
    departureTimeInput.setAttribute("name", "departureInput");
    departureTimeInput.value = departVal;
    */

    // Add fields to list item
    // Add label and destination id
    listItem.appendChild(document.createTextNode("Destination ID: "));
    listItem.appendChild(destinationInput);

    /*
    // Add label and arrival time
    listItem.appendChild(document.createTextNode("Arriving: "));
    listItem.appendChild(arrivalTimeInput);

    // Add label and departure time
    listItem.appendChild(document.createTextNode("Departing: "));
    listItem.appendChild(departureTimeInput);
    */
    /*
    // Add some space before buttons
    listItem.appendChild(document.createTextNode("        "));

    // Create button to delete this list item
    let deleteButton = document.createElement("BUTTON");
    deleteButton.innerHTML = 'Delete';
    deleteButton.onclick = deleteListItem(listItem.id);

    // Create button to move it up
    let moveUpButton = document.createElement("BUTTON");
    moveUpButton.innerHTML = 'Move up';
    moveUpButton.onclick = moveItemUp(listItem.id);

    // Create button to move it down
    let moveDownButton = document.createElement("BUTTON");
    moveDownButton.innerHTML = 'Move Down';
    moveDownButton.onclick = moveItemDown(listItem.id);

    // Add buttons to list item
    listItem.appendChild(moveUpButton);
    listItem.appendChild(moveDownButton);
    listItem.appendChild(deleteButton);
    */
    // Add list element to list
    document.getElementById("tripDestinations").appendChild(listItem);
    console.log("Added new stage row with id " + listItem.id);

}

function deleteListItem(itemID) {
    return () => {
        document.getElementById("tripStageList").removeChild(document.getElementById(itemID));
        console.log("Removed stage row with id " + itemID);
    }
}

function moveItemUp(itemID) {
    return () => {
        // First get index of LI in list
        let listArray = Array.from(document.getElementById("tripStageList").children);
        let index = listArray.indexOf(document.getElementById(itemID));

        // Check if highest element
        if(index == 0) {
            return;
        }

        // Swap array elements
        let b = listArray[index];
        listArray[index] = listArray[index-1];
        listArray[index-1] = b;

        // Clear the list
        document.getElementById("tripStageList").innerHTML = '';

        // Fill list from swapped array
        for (let i = 0; i < listArray.length; i++){
            document.getElementById("tripStageList").appendChild(listArray[i]);
        }
    }
}

function moveItemDown(itemID){
    return () => {
        // First get index of LI in list
        let listArray = Array.from(document.getElementById("tripStageList").children);
        let index = listArray.indexOf(document.getElementById(itemID));

        // Check if highest element
        if(index == listArray.length - 1) {
            return;
        }

        // Swap array elements
        let b = listArray[index];
        listArray[index] = listArray[index+1];
        listArray[index+1] = b;

        // Clear the list
        document.getElementById("tripStageList").innerHTML = '';

        // Fill list from swapped array
        for (let i = 0; i < listArray.length; i++){
            document.getElementById("tripStageList").appendChild(listArray[i]);
        }

    }
}

function listItemToTripData(listItem, index) {
    // Create json object to store data
    let json = {};

    // Assign position to be equal to given index
    json["position"] = index;

    // Read destination id
    json["destinationId"] = parseInt(listItem.children.destinationInput.value);

    // Fill in arrival time (if any)
    try{ json["arrivalTime"] = new Date(listItem.children.arrivalInput.value).toISOString(); }
    catch {json["arrivalTime"] = null }

    // Fill in departure time (if any)
    try{ json["departureTime"] = new Date(listItem.children.departureInput.value).toISOString(); }
    catch {json["departureTime"] = null }

    // Return created json object
    return json;
}

function addTripToServer(url, redirect) {
    // Create array (which will be serialized)
    let tripDataCollection = [];

    // Get user id to use
    let uid = document.getElementById("userIDInput").value;

    // For each list item in list, convert it to a json object and add it to json array
    var listItemArray = Array.of(document.getElementById("tripStageList").children);
    for(let i = 0; i < listItemArray[0].length; i++) {
        tripDataCollection.push(listItemToTripData(listItemArray[0][i], i));
    }

    // Now create final json object
    let data = {};
    data["uid"] = uid;
    data["tripDataCollection"] = tripDataCollection;
    console.log(data);
    // Post json object to server at given url
    post(url,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
        if(response.status == 400) {
        showErrors(json);
    } else if (response.status == 200) {
        window.location.href = redirect + JSON.parse(json);
    } else {
        document.getElementById("errorDisplay").innerHTML = "Error(s): " + Object.values(json).join(", ");
    }
});
});
}

function fillExistingTripData(url) {
    tripId = window.location.href.substr(window.location.href.lastIndexOf('/') + 1);
    console.log(url + tripId);
    // Run a get request to fetch all destinations
    get(url + tripId)
    // Get the response of the request
        .then(response => {
        // Convert the response to json
        response.json().then(data => {
        console.log(data);
    document.getElementById("userIDInput").value = data["uid"];
    document.getElementById("userIDInput").disabled = true;
    // Json data is an array of tripData, iterate through it
    for(var i = 0; i < data["tripDataCollection"].length; i++) {
        // For each tripData, add the corresponding row to tripStageList
        let obj = data["tripDataCollection"][i];

        addNewStageRow(
            obj["destinationId"],
            (obj["arrivalTime"] == null) ? null : new Date(obj["arrivalTime"]).toISOString().slice(0, -1),
            (obj["departureTime"] == null) ? null : new Date(obj["departureTime"]).toISOString().slice(0, -1));
    }
});
});
}

function updateTripOnServer(url) {
    // Create array (which will be serialized)
    let tripDataCollection = [];

    // Get user id to use
    let uid = document.getElementById("userIDInput").value;

    // For each list item in list, convert it to a json object and add it to json array
    var listItemArray = Array.of(document.getElementById("tripStageList").children);
    for(let i = 0; i < listItemArray[0].length; i++) {
        tripDataCollection.push(listItemToTripData(listItemArray[0][i], i));
    }

    // Now create final json object
    let data = {};
    data["uid"] = uid;
    data["tripDataCollection"] = tripDataCollection;
    data["id"] = tripId;
    console.log(data);
    // Post json object to server at given url
    put(url,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
        if(response.status == 400) {
        showErrors(json);
    } else if (response.status == 200) {
        document.getElementById("errorDisplay").innerHTML = "Successfully updated trip";
    } else {
        document.getElementById("errorDisplay").innerHTML = "Error(s): " + Object.values(json).join(", ");
    }
});
});
}

function getAllTripsOfUser(url, viewUrl) {
    userId = window.location.href.substr(window.location.href.lastIndexOf('/') + 1);
    // Run a get request to fetch all destinations
    get(url + userId)
    // Get the response of the request
        .then(response => {
        // Convert the response to json
        response.json().then(data => {
        // Json data is an array of tripData, iterate through it
        for(var i = 0; i < data["allTrips"].length; i++) {
        // For each tripData, add the corresponding row to tripStageList
        let obj = data["allTrips"][i];

        var a = document.createElement('a');
        var linkText = document.createTextNode("Trip ID: " + obj["id"] + ", total destinations: " + obj["tripDataCollection"].length);
        a.appendChild(linkText);
        a.title = "Trip ID: " + obj["id"] + ", total destinations: " + obj["tripDataCollection"].length;
        a.href = viewUrl + obj["id"];
        document.getElementById("tripList").appendChild(a);
        document.getElementById("tripList").appendChild(document.createElement("BR"));
    }
});
});
}

function post(url, data) {
    return fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
}

function put(url, data) {
    return fetch(url, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
}

function get(url) {
    return fetch(url, {
        method: "GET"
    })
}

function showErrors(json) {
    const elements = document.getElementsByTagName("pre");
    console.log(json);
    for (i in elements) {
        elements[i].innerHTML = "";
    }
    for (const key of Object.keys(json)) {
        document.getElementById("errorDisplay").innerHTML += (parseInt(key) + 1) + " " + json[key];
    }
}
