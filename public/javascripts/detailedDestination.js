/**
 * Function to get the relevant destination and fill the HTML
 * @param {number} destinationId  of the destination to display
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

                if (false) {   // TODO: destination.travellerTypes.length > 0
                    let travellerTypes = "";
                    for (let i = 0; i < destination.travellerTypes.length; i++) {
                        travellerTypes += ", " + destination.travellerTypes[i].description;
                    }
                    document.getElementById("summary_traveller_types").innerText = travellerTypes.substr(2);
                } else {
                    document.getElementById("summary_traveller_types").innerText = "No associated traveller types";
                }

                createPrivacyButton(destination.isPublic);
            }
        })
    })
}

/**
 * Deletes the current destination
 * @param {number} destinationId the id of the destination to delete
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
 * Converts a private destination to a public destination. closes the makeDestinationPublicModal and shows
 * a success toast
 * @param {number} destinationId the id of the destination to make public
 */
function makeDestinationPublic(destinationId) {
    put(destinationRouter.controllers.backend.DestinationController.makeDestinationPublic(
        destinationId).url, {})
    .then(response => {
        if (response.status === 200) {
            $("#makeDestinationPublicModal").modal('hide');
            createPrivacyButton(true);
            toast('Destination Privacy Changed',
                'The destination is now public.', 'success');
        } else {
            toast('Error changing privacy', response.toString(), 'danger',
                5000);
        }
    });
}

/**
 * Creates the appropriate privacy button for the destination. Will only allow the user to change the privacy
 * from private to public.
 * @param {boolean} isPublic a boolean of the current destinations privacy, true if public, false if private
 */
function createPrivacyButton(isPublic) {
    const privacyWrapper = $("#privacy_wrapper");
    privacyWrapper.empty();
    if (isPublic) {
        const isPublicImage = document.createElement("img");
        isPublicImage.title = "Destination is Public";
        isPublicImage.src = "/assets/images/public.png";
        isPublicImage.setAttribute("style", "vertical-align: sub;");
        privacyWrapper.append(isPublicImage)
    } else {
        const makePublicButton = document.createElement("input");
        makePublicButton.type = "image";
        makePublicButton.classList.add("privacy-image");
        makePublicButton.title = "Destination is Private, Click to make public.";
        makePublicButton.src = "/assets/images/private.png";
        makePublicButton.setAttribute("data-toggle", "modal");
        makePublicButton.setAttribute("data-target",
            "#makeDestinationPublicModal");
        makePublicButton.setAttribute("style", "vertical-align: sub;");
        privacyWrapper.append(makePublicButton)
    }
}

/**
 * Edits the current destination
 * What the actual fuck is this
 * @param {number} destinationId the id of the destination to edit
 */
function editDestination(destinationId) {
    get(destinationRouter.controllers.backend.DestinationController.getDestination(
        destinationId).url)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(destination => {
            if (response.status !== 200) {
                showErrors(destination);
            } else {
                // Read data from destination form
                const formData = new FormData(
                    document.getElementById("editDestinationForm"));
                // Convert data to json object
                const data = Array.from(formData.entries()).reduce(
                    (memo, pair) => ({
                        ...memo,
                        [pair[0]]: pair[1],
                    }), {});
                // Convert lat and long to double values, and id to int
                destination.latitude = parseFloat(data.latitude);
                destination.longitude = parseFloat(data.longitude);
                data.countryId = parseInt(data.countryId);

                // Convert country id to country object
                destination.country.id = data.countryId;
                destination._type = data._type;
                destination.name = data.name;
                destination.district = data.district;

                // Post json data to given uri
                put(destinationRouter.controllers.backend.DestinationController.editDestination(
                    destinationId).url, destination)
                .then(response => {
                    response.json().then(data => {
                        if (response.status !== 200) {
                            if (data === "Duplicate destination") {
                                toast("Destination could not be edited!",
                                    "The destination already exists.", "danger",
                                    5000);
                                $('#editDestinationModal').modal('hide');
                            } else {
                                showErrors(data);
                            }
                        } else if (response.status === 200) {
                            populateDestinationDetails(destinationId);
                            $('#editDestinationModal').modal('hide');
                            toast("Destination Updated",
                                "Updated Details are now showing",
                                'success');
                        } else {
                            toast("Not Updated",
                                "There was an error updating the destination details",
                                "danger");
                        }
                    });
                });
            }
        });
    });
}

/**
 * Fills the edit destination modal with the information of that destination
 * @param {number} destinationId
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
                hideErrors("editDestinationForm");
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
    fillGallery(photoRouter.controllers.backend.PhotoController.getDestinationPhotos(destinationId).url, "main-gallery", "page-selection")
}

/**
 * Function to toggle the linked status of a photo.
 * Is used even though Intellij doesn't think so
 * @param {Long} guid of the photo to be linked
 * @param {boolean} newLinked the new status of the photo
 * @param {Long} destinationId the destination to link (or unlink) the photo to/from
 */
function toggleLinked(guid, newLinked, destinationId) {
    const label = document.getElementById(guid + "linked");
    const data = {
    };
    if (!newLinked) {
        const url = photoRouter.controllers.backend.PhotoController.deleteLinkPhotoToDest(destinationId, guid).url;
        _delete(url)
        .then(res => {
            if (res.status === 200) {
                label.innerHTML = "Not-Linked";
                label.setAttribute("src", "/assets/images/location-unlinked.png");
                label.setAttribute("onClick",
                    "toggleLinked(" + guid + "," + !newLinked + ")");
                toast("Photo Unlinked", "Photo has been successfully removed from this destination", "success")
            }
        })
    } else {
        const url = photoRouter.controllers.backend.PhotoController.linkPhotoToDest(destinationId, guid).url;
        put(url, data)
        .then(res => {
            if (res.status === 200) {
                label.innerHTML ="Linked";
                label.setAttribute("src", "/assets/images/location-linked.png");
                label.setAttribute("onClick",
                    "toggleLinked(" + guid + "," + !newLinked + ")");
                toast("Photo Linked", "Photo Successfully linked to this destination, success");
            }
        })
    }
    $("#linkPhotoToDestinationModal").modal('hide');
    window.location.href = '/destinations/' + destinationId;
}