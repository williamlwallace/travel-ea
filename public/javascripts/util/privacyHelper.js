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
