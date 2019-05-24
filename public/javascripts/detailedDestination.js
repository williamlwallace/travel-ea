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
let canEdit = true;

/**
 * allows the upload image button call the link photo modal which then
 * calls the upload image file field
 * For a normal photo
 */
$("#upload-gallery-image-button").click(function() {
    $("#linkPhotoToDestinationModal").modal('show');
    fillGallery(photoRouter.controllers.backend.PhotoController.getAllUserPhotos(USERID).url, "link-gallery", "link-selection");
    // $("#upload-gallery-image-file").click();
});

/**
 * Retrieves the userId from the rendered scala which can then be accessed by various JavaScript methods
 * @param {Long} userId
 */
function sendUserIdAndFillGallery(userId) {
    USERID = userId;
    //TODO update this to be the get linked destinations method
    fillGallery(photoRouter.controllers.backend.PhotoController.getAllUserPhotos(USERID).url, "main-gallery", "page-selection")
}

function linkPhoto(currentIndex, imagesCount) {
    console.log("Suop");


}