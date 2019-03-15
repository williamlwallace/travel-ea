// function getAllDestinations (url) {
//     get(url).then(response => {
//         response.json().then(data => {
//             for(let i = 0; i < data.length; i++) {
//             // For each destination, make a list element that is the json string of object
//             let item = document.createElement("TD");
//             item.innerHTML = data[i]['name'];
//             item.value = data[i]['id'];
//             // Add list element to list
//             document.getElementById("countryDropDown").appendChild(item);
//         }
//     })
//     })
// }

var destinationsInTrip = [];

function addDestinationToTrip (destId) {
    console.log("destId");
    destinationsInTrip.push(destId);


}

function getDestinations() {
    return destinationsInTrip;
}

function displayDestinations () {
    for (var i=0; i < destinationsInTrip.length; i++) {
        var html = "<div class=\"sortable-card col-md-4\">";
        html += "<div class=\"card mb-4\">";
        html += "<div class=\"view overlay\">";
        html += "<a href=\"#!\">";
        html += "<div class=\"mask rgba-white-slight\"></div>";
        html += "</a>";
        html += "</div>";
        html += "<div class=\"card-body\">";
        html += "<h4 class=\"card-title\">" + destinationsInTrip[i]["name"] + "</h4>";
        html += "<p class=\"card-text\"><b>Type: </b>" + destinationsInTrip[i]["_type"] + "<br/><b>District: </b>" + destinationsInTrip[i]["district"] + "<br/><b>Latitude: </b>" + destinationsInTrip[i]["latitude"] + "<br/><b>Longitude: </b>" + destinationsInTrip[i]["longitude"] + "<br/><b>Country: </b>" + destinationsInTrip[i]["countryId"] + "</p>";
        html += "<button type=\"button\" class=\"btn btn-primary btn-md\" data-toggle=\"modal\" data-target=\"#modalEditForm\">Edit</button>";
        html += "</div>";
        html += "</div>";
        html += "</div>";

        document.getElementById("fillTripDestinations").innerHTML = html;
    }
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
