/**
 * Function to get the relevant destination and fill the HTML
 *
 * @param {number} destinationId  of the destination to display
 */
function populateDestinationDetails(destinationId) {
    get(
        destinationRouter.controllers.backend.DestinationController.getDestination(
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
                    "summary_type").innerText = destination.destType;
                document.getElementById(
                    "summary_district").innerText = destination.district;
                document.getElementById(
                    "summary_country").innerText = destination.country.name;
                checkCountryValidity(destination.country.name,
                    destination.country.id)
                .then(result => {
                    if (result === false) {
                        document.getElementById(
                            "summary_country").innerText = destination.country.name
                            + ' (invalid)';
                    }
                });
                document.getElementById(
                    "summary_latitude").innerText = destination.latitude.toFixed(
                    2);
                document.getElementById(
                    "summary_longitude").innerText = destination.longitude.toFixed(
                    2);

                if (destination.travellerTypes.length > 0) {
                    let travellerTypes = "";
                    for (let i = 0; i < destination.travellerTypes.length;
                        i++) {
                        travellerTypes += ", "
                            + destination.travellerTypes[i].description;
                    }
                    document.getElementById(
                        "heading_traveller_types").style.display = "block";
                    document.getElementById(
                        "summary_traveller_types").innerText = travellerTypes.substr(
                        2);
                } else {
                    document.getElementById(
                        "heading_traveller_types").style.display = "none";
                    document.getElementById(
                        "summary_traveller_types").innerText = "";
                }

                // Populates tags
                tagDisplay.populateTags(destination.tags);
                if (destination.tags.length > 0) {
                    document.getElementById(
                        "heading_tags").style.display = "block";
                } else {
                    document.getElementById(
                        "heading_tags").style.display = "none";
                }

                createPrivacyButton(destination.isPublic);
            }
        })
    })
}

/**
 * Deletes the current destination
 *
 * @param {number} destinationId the id of the destination to delete
 * @param {string} redirect the url to redirect to if the destination is deleted successfully
 */
function deleteDestination(destinationId, redirect) {
    const handler = (status, json) => {
        if (status === 200) {
            $('#deleteDestinationModal').modal('hide');
            window.location.href = redirect;
        }
    };
    const URL = destinationRouter.controllers.backend.DestinationController.deleteDestination(
        destinationId).url;
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Converts a private destination to a public destination. closes the makeDestinationPublicModal and shows
 * a success toast
 *
 * @param {number} destinationId the id of the destination to make public
 */
function makeDestinationPublic(destinationId) {
    put(destinationRouter.controllers.backend.DestinationController.makeDestinationPublic(
        destinationId).url, {})
    .then(response => {
        if (response.status === 200) {
            $("#makeDestinationPublicModal").modal('hide');
            createPrivacyButton(true);
            initMap(destinationId);
            toast('Destination Privacy Changed',
                'The destination is now public');
        } else {
            toast('Error changing privacy', response.toString(), 'danger',
                5000);
        }
    });
}

/**
 * Creates the appropriate privacy button for the destination. Will only allow the user to change the privacy
 * from private to public.
 *
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
 * Builds the destination object and sends update request
 *
 * @param {number} destinationId the id of the destination to edit
 */
function editDestination(destinationId) {
    // Read data from destination form
    const formData = new FormData(
        document.getElementById("editDestinationForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce(
        (memo, pair) => ({
            ...memo,
            [pair[0]]: pair[1],
        }), {});

    // Builds destination object out of form data
    const destination = {
        name: data.name,
        destType: data.destType,
        district: data.district,
        latitude: parseFloat(data.latitude),
        longitude: parseFloat(data.longitude),
        country: {
            id: parseInt(data.countryId)
        }
    };

    // Add tags to destination
    const tags = tagPicker.getTags();
    destination.tags = tags.map((tag) => {
        return {
            name: tag
        }
    });

    addNonExistingCountries([destination.country]).then(result => {
        // Post json data to given uri
        const URL = destinationRouter.controllers.backend.DestinationController.editDestination(
            destinationId).url;
        const initialUpdate = true;
        const handler = function (status, json) {
            if (this.initialUpdate) {
                if (status === 200) {
                    toast("Update successful",
                        "The destination has been updated");
                } else if (status !== 400) {
                    toast("Destination could not be updated", json, "danger",
                        5000);
                }
                this.initialUpdate = false;
            }

            if (status === 200) {
                populateDestinationDetails(this.destinationId);
                $('#editDestinationModal').modal('hide');
                closeEdit();
            }
        }.bind({destinationId, initialUpdate});
        const reqData = new ReqData(requestTypes['UPDATE'], URL, handler,
            destination);
        undoRedo.sendAndAppend(reqData);
    });
}

/**
 * Fills the edit destination modal with the information of that destination
 *
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
                openEdit();
                hideErrors("editDestinationForm");
                document.getElementById("name").value = destination.name;
                document.getElementById(
                    "destType").value = destination.destType;
                document.getElementById(
                    "district").value = destination.district;
                document.getElementById(
                    "latitudeDeat").value = destination.latitude;
                document.getElementById(
                    "longitudeDeat").value = destination.longitude;
                // Fills country picker
                $('#countryDropDown').selectpicker('val', destination.country.id);
                // Fills tag input field
                tagPicker.populateTags(destination.tags);
            }
        })
    })
}

/**
 * The editCoverPhotoButton click listener.
 * Shows the editCoverPhotoModal and fills the gallery with the available photos.
 * Sets the photos click listeners to call the setCoverPhoto method.
 */
$("#changePrimaryPhotoButton").click(function () {
    $("#changePrimaryPhotoModal").modal('show');
    fillSelectionGallery(
        photoRouter.controllers.backend.PhotoController.getAllUserPhotos(
            USERID).url, "primary-photo-gallery", "current-page", function () {
            setPrimaryPhoto(this.getAttribute("data-id"))
        });
});

/**
 * Sets the destination primary photo given a specific photoID
 * @Param {Number} photoId the id of the photo to set as the cover photo
 */
function setPrimaryPhoto(photoId) {
    const primaryPicUpdateURL = destinationRouter.controllers.backend.DestinationController.changeDestinationPrimaryPhoto(
        DESTINATIONID).url;
    const destinationURL =  destinationRouter.controllers.backend.DestinationController.getDestination(
        DESTINATIONID).url;


    // Create reversible request to update primary photo to this new photo
    const handler = (status, json) => {
        if (status === 200) {
            getUserId().then(userId => {
                getPrimaryPicture();
                $("#changePrimaryPhotoModal").modal('hide');
                if(isUserAdmin() ) {
                    toast("Changes saved!",
                        "Primary photo changes saved successfully");
                } else {
                    toast("Suggestion requested",
                        "Your suggestion has been sent for approval");
                }
            })
        } else {
            toast("Error",
                "Unable to update photo", "danger");
        }
    };

    const requestData = new ReqData(requestTypes["UPDATE"],
        primaryPicUpdateURL, handler, photoId);
    undoRedo.sendAndAppend(requestData);

}

/**
 * Takes a url for the backend controller method to get the users profile picture. Sends a get for this file and sets
 * the profile picture and cover photo path to it.
 */
function getPrimaryPicture() {
    get(destinationRouter.controllers.backend.DestinationController.getDestination(
        DESTINATIONID).url).then(response => {
        // Read response from server, which will be a json object
        if (response.status === 200) {
            response.json().then(data => {
                if (data.primaryPhoto === null) {
                    $("#DestinationProfilePicture").attr("src",
                        "/assets/images/default-destination-primary.png");
                } else {
                    $("#DestinationProfilePicture").attr("src",
                        "../user_content/" + data.primaryPhoto.filename);
                }
            });
        } else if (response.status === 404) {
            $("#DestinationProfilePicture").attr("src",
                "/assets/images/default-destination-primary.png");
        }
    });
}

/**
 * Gets traveller type to modify for a destination and determines whether to add or remove, calls appropriate method
 *
 * @param {Number} destId - ID of destination to link traveller type to
 */
function updateTravellerTypes(destId) {
    let select = document.getElementById("travellerTypesSelect");
    let selected = select.options[select.selectedIndex];

    toggleTravellerType(destId, selected.value, false);
}

/**
 * Gets traveller types from API and populates drop down, includes checking to see if traveller
 * type is already linked with destination
 */
function fillTravellerTypeInfo() {
    get(profileRouter.controllers.backend.ProfileController.getAllTravellerTypes().url)
    .then(response => {
        response.json()
        .then(travellerTypes => {
            let existingTravellerTypes = document.getElementById(
                "summary_traveller_types").innerText.split(", ");
            $("#travellerTypesSelect").empty();
            let select = document.getElementById("travellerTypesSelect");
            for (let i = 0; i < travellerTypes.length; i++) {
                let option = document.createElement("option");

                if (!existingTravellerTypes.includes(
                    travellerTypes[i].description)) {
                    option.text = "Add " + travellerTypes[i].description;
                } else {
                    option.text = "Remove " + travellerTypes[i].description;
                }

                option.value = travellerTypes[i].id;
                select.add(option);
            }
        })
    });
}

let USERID;
let DESTINATIONID;
let canEdit = true;
let canDelete = false;

/**
 * Allows the upload image button call the link photo modal which then
 * fills the gallery with all the users photos, and indicates which are already linked
 */
$("#upload-gallery-image-button").click(function () {
    $("#linkPhotoToDestinationModal").modal('show');
    fillLinkGallery(
        photoRouter.controllers.backend.PhotoController.getAllUserPhotos(
            USERID).url, "link-gallery", "link-selection", DESTINATIONID);
});

/**
 * Closes all data related to editing a destination
 */
function closeEdit() {
    map.creativeMode = false;
    $('#destDeets').css('display', 'inline');
    $('#destEdit').css('display', 'none');
    $("#summary_name").animate({"opacity": "1"}, 700);

    map.setNewMarker($('#summary_latitude').html(),
        $('#summary_longitude').html());
    map.panToNewMarker();
}

/**
 * Opens all data related to editing a destination
 */
function openEdit() {
    map.creativeMode = true;
    $('#destDeets').css('display', 'none');
    $('#destEdit').css('display', 'inline');
    $("#summary_name").animate({"opacity": "0"}, 700);
}

/**
 * Cancel edit button on click listener
 */
$("#cancelEditButton").click(closeEdit);

/**
 * Retrieves the userId from the rendered scala which can then be accessed by various JavaScript methods
 * Also fills the initial gallery on photos
 *
 * @param {Number} userId of the logged in user
 * @param {Number} destinationId of the destination of photos to get
 */
function sendUserIdAndFillGallery(userId, destinationId) {
    USERID = userId;
    DESTINATIONID = destinationId;
    fillDestinationGallery(
        photoRouter.controllers.backend.PhotoController.getDestinationPhotos(
            destinationId).url,
        photoRouter.controllers.backend.PhotoController.getAllUserPhotos(
            USERID).url, "main-gallery", "page-selection")
}

/**
 * Function to toggle the linked status of a photo.
 * Is used even though Intellij doesn't think so
 *
 * @param {Number} guid of the photo to be linked
 * @param {boolean} newLinked the new status of the photo
 * @param {Number} destinationId the destination to link (or unlink) the photo to/from
 */
function toggleLinked(guid, newLinked, destinationId) {
    const label = document.getElementById(guid + "linked");
    const data = {};
    const URL = photoRouter.controllers.backend.PhotoController.deleteLinkPhotoToDest(
        destinationId, guid).url;
    const handler = function (status, json) {
        if (status === 200) {
            if (!this.newLinked) {
                label.innerHTML = "Not-Linked";
                label.setAttribute("src",
                    "/assets/images/location-unlinked.png");
                label.setAttribute("onClick",
                    "toggleLinked(" + guid + "," + !this.newLinked + ")");
                toast("Photo Unlinked",
                    "Photo has been successfully removed from this destination");
            } else {
                label.innerHTML = "Linked";
                label.setAttribute("src",
                    "/assets/images/location-linked.png");
                label.setAttribute("onClick",
                    "toggleLinked(" + guid + "," + !this.newLinked + ")");
                toast("Photo Linked",
                    "Photo Successfully linked to this destination");
            }
            getUserId().then(id => {
                sendUserIdAndFillGallery(id, destinationId);
            });
            getAndFillDD(tagRouter.controllers.backend.TagController.getAllDestinationPhotoTags(destinationId).url, ["tagFilter"], "name", false, "name");

            this.newLinked = !this.newLinked;
        }
    }.bind({newLinked});
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler, data);
    undoRedo.sendAndAppend(reqData);

    $("#linkPhotoToDestinationModal").modal('hide');
}

/**
 * Initialises google maps on detailed destinations page
 *
 * @param {Number} destinationId The destination id to center the map on
 */
function initMap(destinationId) {
    get(destinationRouter.controllers.backend.DestinationController.getDestination(
        destinationId).url)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(destination => {
            if (response.status !== 200) {
                showErrors(destination);
            } else {
                // Initial map options
                const options = {
                    zoom: 4,
                    center: {
                        lat: destination.latitude - 2,
                        lng: destination.longitude
                    },
                    disableDefaultUI: true,
                    styles: [{
                        "elementType": "labels",
                        "stylers": [{"visibility": "off"}, {"color": "#f49f53"}]
                    }, {
                        "featureType": "landscape",
                        "stylers": [{"color": "#f9ddc5"}, {"lightness": -7}]
                    }, {
                        "featureType": "road",
                        "stylers": [{"color": "#813033"}, {"lightness": 43}]
                    }, {
                        "featureType": "poi.business",
                        "stylers": [{"color": "#645c20"}, {"lightness": 38}]
                    }, {
                        "featureType": "water",
                        "stylers": [{"color": "#1994bf"}, {"saturation": -69},
                            {"gamma": 0.99}, {"lightness": 43}]
                    }, {
                        "featureType": "road.local",
                        "elementType": "geometry.fill",
                        "stylers": [{"color": "#f19f53"}, {"weight": 1.3},
                            {"visibility": "on"}, {"lightness": 16}]
                    }, {"featureType": "poi.business"}, {
                        "featureType": "poi.park",
                        "stylers": [{"color": "#645c20"}, {"lightness": 39}]
                    }, {
                        "featureType": "poi.school",
                        "stylers": [{"color": "#a95521"}, {"lightness": 35}]
                    }, {}, {
                        "featureType": "poi.medical",
                        "elementType": "geometry.fill",
                        "stylers": [{"color": "#813033"}, {"lightness": 38},
                            {"visibility": "off"}]
                    }, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {},
                        {"elementType": "labels"}, {
                            "featureType": "poi.sports_complex",
                            "stylers": [{"color": "#9e5916"}, {"lightness": 32}]
                        }, {}, {
                            "featureType": "poi.government",
                            "stylers": [{"color": "#9e5916"}, {"lightness": 46}]
                        }, {
                            "featureType": "transit.station",
                            "stylers": [{"visibility": "off"}]
                        }, {
                            "featureType": "transit.line",
                            "stylers": [{"color": "#813033"}, {"lightness": 22}]
                        }, {
                            "featureType": "transit",
                            "stylers": [{"lightness": 38}]
                        }, {
                            "featureType": "road.local",
                            "elementType": "geometry.stroke",
                            "stylers": [{"color": "#f19f53"},
                                {"lightness": -10}]
                        }, {}, {}, {}]
                };

                map = new DestinationMap(options, false);

                map.setNewMarker(
                    destination.latitude,
                    destination.longitude,
                    destination.isPublic ? map.markerPublic : map.markerPrivate
                );

                google.maps.event.addListener(map.map, 'click',
                    function (event) {
                        if (!map.creativeMode) {
                            return;
                        }
                        map.setNewMarker(event.latLng.lat(),
                            event.latLng.lng());

                        $('#latitudeDeat').val(event.latLng.lat);
                        $('#longitudeDeat').val(event.latLng.lng);
                    });
            }
        })
    })
}

/**
 * The latitude field listener. Enforces -90 < latitude < 90
 * Moves the marker on the map when the latitude changes
 */
$('#latitudeDeat').on('input', () => {
    if ($('#latitudeDeat').val() > 90) {
        $('#latitudeDeat').val('90');
    }
    if ($('#latitudeDeat').val() < -90) {
        $('#latitudeDeat').val('-90');
    }

    map.setNewMarker($('#latitudeDeat').val(), null);
});

/**
 * The longitude field listener. Enforces -180 < longitude < 180
 * Moves the marker on the map when the longitude changes
 */
$('#longitudeDeat').on('input', () => {
    if ($('#longitudeDeat').val() > 180) {
        $('#longitudeDeat').val('180');
    }
    if ($('#longitudeDeat').val() < -180) {
        $('#longitudeDeat').val('-180');
    }

    map.setNewMarker(null, $('#longitudeDeat').val());

});