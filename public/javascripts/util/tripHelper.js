/**
 * Updates a trips privacy when the toggle is used
 * @param {string} uri Route for updating trip privacy
 * @param {string} publicImageSrc Source of public icon image to use
 * @param {string} privateImageSrc Source of private icon image to use
 * @param {Number} tripId Id of trip to update
 */
function updateTripPrivacy(uri, publicImageSrc, privateImageSrc, tripId) {
    let currentPrivacyImgSrc = document.getElementById("privacy-img").getAttribute("src");

    let tripData = {
        "id": tripId
    };

    if (currentPrivacyImgSrc === publicImageSrc) {
        tripData["isPublic"] = false;
    }
    else if (currentPrivacyImgSrc === privateImageSrc) {
        tripData["isPublic"] = true;
    }
    else {
        return;
    }

    const URL = tripRouter.controllers.backend.TripController.updateTripPrivacy().url;
    const initialToggle = true;
    const handler = function(status, json) {
        if (status === 200) {
            currentPrivacyImgSrc = document.getElementById("privacy-img").getAttribute("src");
            if (currentPrivacyImgSrc === publicImageSrc) {
                document.getElementById("privacy-img").setAttribute("src",
                    privateImageSrc);
                document.getElementById("privacy-img").setAttribute("title",
                    "Private");
            } else {
                document.getElementById("privacy-img").setAttribute("src",
                    publicImageSrc);
                document.getElementById("privacy-img").setAttribute("title",
                    "Public");
            }
            if (this.initialToggle) {
                toast("Success", "Trip privacy updated", "success");
                this.initialToggle = false;
            }
        }
    }.bind({initialToggle});

    const reqData = new ReqData(requestTypes['UPDATE'], URL, handler, tripData);
    undoRedo.sendAndAppend(reqData);
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
