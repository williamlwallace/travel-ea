/**
 * Updates a trips privacy when the toggle is used
 * @param {string} uri Route for updating trip privacy
 * @param {string} publicImageSrc Source of public icon image to use
 * @param {string} privateImageSrc Source of private icon image to use
 * @param {Number} tripId Id of trip to update
 */
function updateTripPrivacy(uri, publicImageSrc, privateImageSrc, tripId) {
    let currentPrivacy = document.getElementById("privacyImg" + tripId).title;

    let tripData = {
        "id": tripId
    };

    if (currentPrivacy === "Public") {
        tripData["isPublic"] = false;
    }
    else if (currentPrivacy === "Private") {
        tripData["isPublic"] = true;
    }
    else {
        return;
    }

    put(uri, tripData).then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            // On successful update
            if (response.status === 200) {
                if (currentPrivacy === "Public") {
                    document.getElementById(
                        "privacyImg" + tripId).title = "Private";
                    document.getElementById(
                        "privacyImg" + tripId).src = privateImageSrc;
                }
                else {
                    document.getElementById(
                        "privacyImg" + tripId).title = "Public";
                    document.getElementById(
                        "privacyImg" + tripId).src = publicImageSrc;
                }
            }
        });
    });
}

/**
 * Speciality show errors function for trips
 * @param {Object} json - Error Response Json
 */
function showTripErrors(json) {
    // Gets all the error key identifiers
    let keys = Object.keys(json);

    // Resets and sets tripError label
    let tripError = document.getElementById("tripError");
    if (keys.includes("trip")) {
        tripError.innerHTML = '<div class="alert alert-danger" role="alert">' +
            '<a class="close" data-dismiss="alert">×</a>' +
            '<span>' + json["trip"] + '</span></div>';
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
