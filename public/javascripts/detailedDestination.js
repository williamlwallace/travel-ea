/**
 * Function to get the relevant destination and fill the HTML
 * @param {Long} destinationId  of the destination to display
 */
function populateDestinationDetails(destinationId) {
    get(destinationRouter.controllers.backend.DestinationController.getDestination(
        destinationId).url)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(destination => {
            if (response.status !== 200) {
                showErrors(destination);
            } else {
                document.getElementById(
                    "summary_name").innerText = destination.name;
                document.getElementById(
                    "destination_name").innerText = destination.name;
                document.getElementById(
                    "summary_type").innerText = destination._type;
                document.getElementById(
                    "summary_district").innerText = destination.district;
                document.getElementById(
                    "summary_country").innerText = destination.country.name;
                document.getElementById(
                    "summary_latitude").innerText = destination.latitude;
                document.getElementById(
                    "summary_longitude").innerText = destination.longitude;
            }
        })
    })
}

/**
 * Deletes the current destination
 * @param {Long} destinationId the id of the destination to delete
 * @param {string} redirect the url to redirect to if the destination is deleted successfully
 */
function deleteDestination(destinationId, redirect) {
    _delete(
        destinationRouter.controllers.backend.DestinationController.deleteDestination(
            destinationId).url)
    .then(response => {
        response.json().then(data => {
            if (response.status === 200) {
                $('#deleteDestinationModal').modal('hide');
                window.location.href = redirect;
            }
        });
    });
}

/**
 * Edits the current destination
 * @param {Long} destinationId the id of the destination to edit
 */
function editDestination(destinationId) {
    // Read data from destination form
    const formData = new FormData(
        document.getElementById("editDestinationForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    // Convert lat and long to double values, and id to int
    data.latitude = parseFloat(data.latitude);
    data.longitude = parseFloat(data.longitude);
    data.countryId = parseInt(data.countryId);

    // Convert country id to country object
    data.country = {"id": data.countryId};

    data.id = destinationId;
    delete data.countryId;
    // Post json data to given uri
    put(destinationRouter.controllers.backend.DestinationController.editDestination(
        destinationId).url, data)
    .then(response => {
        response.json().then(data => {
            if (response.status !== 200) {
                showErrors(data);
            } else if (response.status === 200) {
                populateDestinationDetails(destinationId);
                $('#editDestinationModal').modal('hide');
                toast("Destination Updated", "Updated Details are now showing",
                    'success');
            } else {
                toast("Not Updated",
                    "There was an error updating the destination details",
                    "danger");
            }
        });
    });
}

/**
 * Fills the edit destination modal with the information of that destination
 * @param {Long} destinationId
 */
function populateEditDestination(destinationId) {
    get(destinationRouter.controllers.backend.DestinationController.getDestination(
        destinationId).url)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(destination => {
            if (response.status !== 200) {
                showErrors(destination);
            } else {
                document.getElementById("name").value = destination.name;
                document.getElementById("_type").value = destination._type;
                document.getElementById(
                    "district").value = destination.district;
                document.getElementById(
                    "latitude").value = destination.latitude;
                document.getElementById(
                    "longitude").value = destination.longitude;
                //fills country picker
                $('#countryDropDown').picker('set', destination.country.id);
            }
        })
    })
}

let USERID = null;
let DESTINATIONID = null;
let canEdit = false;

/**
 * allows the upload image button call the link photo modal which then
 * fills the gallery with all the users photos, and indicates which are already linked
 */
$("#upload-gallery-image-button").click(function() {
    $("#linkPhotoToDestinationModal").modal('show');
    fillLinkGallery(photoRouter.controllers.backend.PhotoController.getAllUserPhotos(USERID).url, "link-gallery", "link-selection", DESTINATIONID);
});

/**
 * Retrieves the userId from the rendered scala which can then be accessed by various JavaScript methods
 * Also fills the initial gallery on photos
 * @param {Long} userId
 */
function sendUserIdAndFillGallery(userId, destinationId) {
    USERID = userId;
    DESTINATIONID = destinationId;
    //TODO update this to be the get linked destinations method
    fillGallery(photoRouter.controllers.backend.PhotoController.getAllUserPhotos(USERID).url, "main-gallery", "page-selection")
}

/**
 * Function to toggle the linked status of a photo.
 * Is used even though Intellij doesn't think so
 * @param guid of the photo to be linked
 * @param newLinked the new status of the photo
 * @param destinationId the destination to link (or unlink) the photo to/from
 */
function toggleLinked(guid, newLinked, destinationId) {
    const label = document.getElementById(guid + "linked");
    const data = {
        "isLinked": newLinked
    };
    patch(photoRouter.controllers.backend.PhotoController.linkPhotoToDest(destinationId, guid).url, data)
    .then(res => {
        if (res.status === 200) {
            label.innerHTML = newLinked ? "Linked" : "Not-Linked";
            if (newLinked) {
                label.setAttribute("src", "/assets/images/destination-linked.png");
            } else {
                label.setAttribute("src", "/assets/images/destination-unlinked.png");
            }
            label.setAttribute("onClick",
                "toggleLinked(" + guid + "," + !newLinked + ")");
            if (newLinked) {
                toast("Photo Linked", "Photo Successfully linked to this destination, success");
            } else {
                toast("Photo Unlinked", "Photo has been successfully removed from this destination", "success")
            }
        }
    })
}