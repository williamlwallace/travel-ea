/**
 * The JavaScript method to fill the initial profile data
 *
 * @param {String} email the email of the logged in user, which may or may not be displayed
 */
function fillProfileData(email) {
    if (document.getElementById("summary_email")) {
        document.getElementById("summary_email").innerText = email;
    }
    get(profileRouter.controllers.backend.ProfileController.getProfile(
        profileId).url)
    .then(response => {
        response.json()
        .then(profile => {
            if (response.status !== 200) {
                showErrors(profile);
            } else {
                document.getElementById(
                    "summary_name").innerText = profile.firstName + ' '
                    + profile.lastName;
                document.getElementById(
                    "summary_age").innerHTML = calc_age(
                    Date.parse(profile.dateOfBirth));
                document.getElementById(
                    "summary_gender").innerText = profile.gender;
                arrayToCountryString(profile.nationalities, 'name',
                    countryRouter.controllers.backend.CountryController.getAllCountries().url)
                .then(out => {
                    document.getElementById(
                        "summary_nationalities").innerHTML = out;
                });
                arrayToCountryString(profile.passports, 'name',
                    countryRouter.controllers.backend.CountryController.getAllCountries().url)
                .then(out => {
                    // If passports were cleared, update html text to None: Fix for Issue #36
                    document.getElementById("summary_passports").innerHTML = out
                    === ""
                        ? "None" : out;
                });
                arrayToString(profile.travellerTypes, 'description',
                    profileRouter.controllers.backend.ProfileController.getAllTravellerTypes().url)
                .then(out => {
                    document.getElementById(
                        "summary_travellerTypes").innerHTML = out;
                });
            }
        })
    })
}

/**
 * The JavaScript function to process a client updating there profile
 * @param uri The route/uri to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function updateProfile(uri, redirect) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("updateProfileForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});

    // Convert nationalities, passports and Traveller Types to Correct JSON appropriate format
    data.nationalities = JSONFromDropDowns("nationalities");
    data.passports = JSONFromDropDowns("passports");
    data.travellerTypes = JSONFromDropDowns("travellerTypes");

    addNonExistingCountries(data.nationalities).then(nationalityResult => {
        addNonExistingCountries(data.passports).then(passportResult => {
            // Post json data to given uri
            const handler = function (status, json) {
                if (status !== 200) {
                    showErrors(json, "updateProfileForm");
                } else {
                    updateProfileData(this.data);
                    $("#editProfileModal").modal('hide');
                    toast("Profile Updated!",
                        "The updated information will be displayed on your profile.",
                        "success");
                }
                this.data = json;
            }.bind({data});
            const reqData = new ReqData(requestTypes['UPDATE'], uri, handler,
                data);
            undoRedo.sendAndAppend(reqData);
        });
    });
}

/**
 * Maps a json object to the profile summary data and updates it
 * @param {Object} data Json data object
 */
function updateProfileData(data) {
    document.getElementById("summary_name").innerHTML = data.firstName + " "
        + data.lastName;
    document.getElementById("summary_gender").innerHTML = data.gender;
    document.getElementById("summary_age").innerHTML = calc_age(
        Date.parse(data.dateOfBirth));
    //When the promises resolve, fill array data into appropriate fields
    arrayToString(data.nationalities, 'name',
        countryRouter.controllers.backend.CountryController.getAllCountries().url)
    .then(out => {
        document.getElementById("summary_nationalities").innerHTML = out;
    });
    arrayToString(data.passports, 'name',
        countryRouter.controllers.backend.CountryController.getAllCountries().url)
    .then(out => {
        // If passports were cleared, update html text to None: Fix for Issue #36
        document.getElementById("summary_passports").innerHTML = out === ""
            ? "None" : out;
    });
    arrayToString(data.travellerTypes, 'description',
        profileRouter.controllers.backend.ProfileController.getAllTravellerTypes().url)
    .then(out => {
        document.getElementById("summary_travellerTypes").innerHTML = out;
    });
}

/**
 * The javascript method to populate the select boxes on the edit profile scene
 * @param uri the route/url to send the request to to get the profile data
 */
function populateProfileData(uri) {
    get(uri)
    .then(response => {
        // Read response from server, which will be a json object
        return response.json();
    })
    .then(json => {
        const pickMapper = function (id, item) {
            $(`#${id}`).selectpicker('val', item.id);
        };
        //Maps the json data into the pickers
        json.nationalities.map(pickMapper.bind(null, 'nationalities'));
        json.passports.map(pickMapper.bind(null, 'passports'));
        json.travellerTypes.map(pickMapper.bind(null, 'travellerTypes'));

        $('#gender').selectpicker('val', json.gender);
        //tagsPickerTags = json.tags;
    });
}

/**
 * Variables for selecting and cropping the profile picture.
 */
const cropGallery = $('#profile-gallery');
const profilePictureToCrop = document.getElementById('image');
const profilePictureSize = 350;
let cropper;
let canEdit;
let canDelete;

/**
 * Sets the permissions used for creating the gallery
 * @param {User} loggedUser
 * @param {User} user
 */
function setPermissions(loggedUser, user) {
    canEdit = (loggedUser === user);
    canDelete = (loggedUser === user);
}

/**
 * Loads the cropper into the page when the cropProfilePictureModal opens
 * Ensures that a crop cannot be smaller than the profilePictureSize
 */
$(document).ready(function () {
    $('#cropProfilePictureModal').on('shown.bs.modal', function () {
        cropper = new Cropper(profilePictureToCrop, {
            autoCropArea: 1,
            aspectRatio: 1, //Makes crop area a square
            viewMode: 1,
            dragMode: 'none',
            zoomable: false,
            guides: false,
            minContainerWidth: profilePictureSize,
            minContainerHeight: profilePictureSize,

            cropmove: function (event) {
                const data = cropper.getData();
                if (data.width < profilePictureSize) {
                    event.preventDefault();
                    data.width = profilePictureSize;
                    data.height = profilePictureSize;
                    cropper.setData(data);
                }
            }
        });
    }).on('hidden.bs.modal', function () {
        cropper.destroy();
    });
});

/**
 * Handles uploading the new cropped profile picture, called by the confirm button in the cropping modal.
 * Creates the cropped image and stores it in the database. Reloads the users profile picture.
 */
function uploadProfilePicture() {
    //Get the cropped image and set the size to 290px x 290px
    cropper.getCroppedCanvas({width: 350, height: 350}).toBlob(function (blob) {
        const formData = new FormData();
        formData.append("profilePhotoName", "profilepic.jpg");
        formData.append("file", blob, "profilepic.jpg");
        formData.append("userUploadId", profileId.toString());

        const photoPostURL = photoRouter.controllers.backend.PhotoController.upload().url;
        const profilePicUpdateURL = photoRouter.controllers.backend.PhotoController.makePhotoProfile(
            profileId).url;

        // Send request and handle response
        postMultipart(photoPostURL, formData).then(response => {
            // Read response from server, which will be a json object
            response.json().then(data => {
                if (response.status === 201) {
                    const photoFilename = data["filename"];
                    const photoId = data["guid"];

                    $("#ProfilePicture").attr("src", photoFilename);

                    // Create reversible request to update profile photo to this new photo
                    const handler = (status, json) => {
                        if (status === 200) {
                            getProfileAndCoverPicture();
                            toast("Changes saved!",
                                "Profile picture changes saved successfully.",
                                "success");
                        } else {
                            toast("Error",
                                "Unable to update profile picture", "danger");
                        }
                    };
                    const requestData = new ReqData(requestTypes["UPDATE"],
                        profilePicUpdateURL, handler, photoId);
                    undoRedo.sendAndAppend(requestData);

                }
            });
        });
    });

    $('#cropProfilePictureModal').modal('hide');
    cropper.destroy();
}

/**
 * Displays the full size image of the thumbnail picture clicked in the cropper window.
 * Also hides the changeProfilePictureModal and shows the cropProfilePictureModal
 */
cropGallery.on('click', 'img', function () {
    //Get the path for the pictures thumbnail
    const fullPicturePath = $(this).parent().attr("data-filename");
    //Set the croppers image to this
    profilePictureToCrop.setAttribute('src', fullPicturePath);
    //Show the cropPPModal and hide the changePPModal
    $('#changeProfilePictureModal').modal('hide');
    $('#cropProfilePictureModal').modal('show');
});

/**
 * Populates the edit photo modal
 *
 * @param {Number} guid - the id of the photo
 * @param {String} filename - the filename of the photo
 */
function populateEditPhoto(guid, filename) {
    $('#modal-photo').attr('src', "");
    $('#modal-photo').attr('src', filename);
    $('#modal-photo').attr("name", guid);
    $('#update-caption input').val("");
    $('#edit-modal').modal('show');
    get(photoRouter.controllers.backend.PhotoController.getPhotoById(guid).url)
    .then(response => {
        response.json()
        .then(photo => {
            if (response.status !== 200) {
                toast("Error in retrieving photo data",
                    "Could not retrieve photo"
                    + "data properly", "danger", 5000);
            } else {
                $('#update-caption input').val(photo.caption);
                editTagPicker.populateTags(photo.tags);
            }
        })
    });
    $('#update-img').unbind('click');
    $('#update-img').bind('click', function () {
        updatePhotoCaptionAndTags(guid);
    });
}

/**
 * Updates the caption and tags of a photo
 *
 * @param {Number} guid - The ID of the photo being updated
 */
function updatePhotoCaptionAndTags(guid) {
    const caption = $('#update-caption input').val();
    const tags = editTagPicker.getTags().map(tag => {
        return {
            name: tag
        }
    });
    const reqBody = {
        caption: caption,
        tags: tags
    };
    const url = photoRouter.controllers.backend.PhotoController.updatePhotoDetails(
        guid).url;
    const initialUpdate = true;
    const handler = function(status, json) {
        if (this.initialUpdate) {
            if (status !== 200) {
                toast("Update failed", json, "danger", 5000);
            } else {
                toast("Update successful!",
                    "The photo's captions and tags have been updated",
                    "success");
            }
            this.initialUpdate = false;
        }

        if (status === 200) {
            $('[data-id="' + guid + '"]').attr("data-caption", caption);
            getAndFillDD(tagRouter.controllers.backend.TagController.getAllUserPhotoTags(profileId).url, ["tagFilter"], "name", false, "name");
            fillGallery(getAllPhotosUrl, 'main-gallery', 'page-selection');

        }
    }.bind({initialUpdate});
    const reqData = new ReqData(requestTypes["UPDATE"], url, handler, reqBody);
    console.log(reqBody);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Deletes a photo
 * @param route
 */
function deletePhoto(route) {
    const guid = document.getElementById("modal-photo").name;
    const deleteUrl = route.substring(0, route.length - 1) + guid;

    _delete(deleteUrl)
    .then(response => {
        response.json().then(data => {
            if (response.status === 200) {
                $('#edit-modal').modal('hide');
                fillGallery(getAllPhotosUrl, 'main-gallery', 'page-selection');
                toast("Picture deleted!",
                    "The photo will no longer be displayed in the gallery.",
                    "success");
                getProfileAndCoverPicture();
                getAndFillDD(tagRouter.controllers.backend.TagController.getAllUserPhotoTags(profileId).url, ["tagFilter"], "name", false, "name");
                undoRedo.undoStack.clear();
                undoRedo.redoStack.clear();
                updateUndoRedoButtons();
            }
        });
    });
}

/**
 * Takes the users selected photo file and creates a url object out of it. This is then passed to cropper.
 * The appropriate modals are shown and hidden.
 */
function uploadNewPhoto() {
    const selectedFile = document.getElementById('upload-image-file').files[0];
    profilePictureToCrop.setAttribute('src',
        window.URL.createObjectURL(selectedFile));

    //Show the cropPPModal and hide the changePPModal
    $('#changeProfilePictureModal').modal('hide');
    $('#cropProfilePictureModal').modal('show');
}

/**
 * Takes a url for the backend controller method to get the users profile picture. Sends a get for this file and sets
 * the profile picture and cover photo path to it.
 */
function getProfileAndCoverPicture() {
    const profileId = window.location.href.split("/").pop();
    get(profileRouter.controllers.backend.ProfileController.getProfile(
        profileId).url).then(response => {
        // Read response from server, which will be a json object
        if (response.status === 200) {
            response.json().then(data => {
                if (data.profilePhoto === null) {
                    $("#ProfilePicture").attr("src",
                        "/assets/images/default-profile-picture.jpg");
                } else {
                    getPictures();
                    $("#ProfilePicture").attr("src",
                        "../user_content/" + data.profilePhoto.filename);
                }

                if (data.coverPhoto === null) {
                    $("#CoverPhoto").css("background-image",
                        "url(/assets/images/profile-bg.jpg)");
                } else {
                    getPictures();
                    $("#CoverPhoto").css("background-image",
                        "url(../user_content/" + data.coverPhoto.filename
                        + ")");
                }
            });
        } else if (response.status === 404) {
            $("#ProfilePicture").attr("src",
                "/assets/images/default-profile-picture.jpg");
            $("#CoverPhoto").css("background-image",
                "/assets/images/profile-bg.jpg");
        }
    });
}

/**
 * Takes a url for the backend controller method to get the users pictures. Then uses this to fill the gallery.
 */
function getPictures() {
    fillGallery(
        photoRouter.controllers.backend.PhotoController.getAllUserPhotos(
            profileId).url, 'main-gallery', 'page-selection');
}

/**
 * Displays the users images in a change profile picture gallery modal
 */
function showProfilePictureGallery() {
    const galleryObjects = createGalleryObjects(false);
    addPhotos(galleryObjects, $("#profile-gallery"),
        $('#page-selection-profile-picture'));
    $('#changeProfilePictureModal').modal('show');
}

/**
 * Sets the users cover photo given a specific photoID
 * @Param {Number} photoId the id of the photo to set as the cover photo
 * @Param {Number} profileId the id of the user whos cover photo should change
 */
function setCoverPhoto(photoId) {
    const coverPicUpdateURL = photoRouter.controllers.backend.PhotoController.setCoverPhoto(
        profileId).url;

    // Create reversible request to update profile photo to this new photo
    const handler = (status, json) => {
        if (status === 200) {
            getProfileAndCoverPicture();
            $("#editCoverPhotoModal").modal('hide');
            toast("Changes saved!",
                "Cover photo changes saved successfully.",
                "success");
        } else {
            toast("Error",
                "Unable to update cover photo", "danger");
        }
    };

    const requestData = new ReqData(requestTypes["UPDATE"],
        coverPicUpdateURL, handler, photoId);
    undoRedo.sendAndAppend(requestData);
}

/**
 * allows the upload image button to act as an input field by clicking on the upload image file field
 * For a normal photo
 */
$("#upload-gallery-image-button").click(function () {
    $('#caption input').val("");
    $('.image-body img').attr('src', '');
    $('.image-body').css('display', 'none');
    $('.uploader').css('display', 'block');
    uploadTagPicker.clearTags();
});

/**
 * The editCoverPhotoButton click listener.
 * Shows the editCoverPhotoModal and fills the gallery with the available photos.
 * Sets the photos click listeners to call the setCoverPhoto method.
 */
$("#editCoverPhotoButton").click(function () {
    $("#editCoverPhotoModal").modal('show');
    fillSelectionGallery(
        photoRouter.controllers.backend.PhotoController.getAllUserPhotos(
            profileId).url, "cover-photo-gallery", "current-page", function () {
            setCoverPhoto(this.getAttribute("data-id"))
        });
});