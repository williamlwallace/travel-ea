/* Display gender drop down the same as the others */
$('#gender').picker();

/**
 * The JavaScript method to fill the initial profile data
 * @param {Number} userId the id of the user who's profile to receive
 * @param {String} email the email of the logged in user, which may or may not be displayed
 */
function fillProfileData(userId, email) {
    if (document.getElementById("summary_email")) {
        document.getElementById("summary_email").innerText = email;
    }
    get(profileRouter.controllers.backend.ProfileController.getProfile(
        userId).url)
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
        return response.json()
    })
    .then(json => {
        const pickMapper = function (id, item) {
            $(`#${id}`).picker('set', item.id);
        }
        //Maps the json data into the pickers
        json.nationalities.map(pickMapper.bind(null, 'nationalities'));
        json.passports.map(pickMapper.bind(null, 'passports'));
        json.travellerTypes.map(pickMapper.bind(null, 'travellerTypes'));

        $('#gender').picker('set', json.gender);
    });
}

/**
 * Variables for selecting and cropping the profile picture.
 */
let cropGallery = $('#profile-gallery');
let profilePictureToCrop = document.getElementById('image');
let profilePictureSize = 350;
let cropper;

let getAllPhotosUrl;
let profilePictureControllerUrl;
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
                let data = cropper.getData();
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
function uploadProfilePicture(userId) {
    //Get the cropped image and set the size to 290px x 290px
    cropper.getCroppedCanvas({width: 350, height: 350}).toBlob(function (blob) {
        let formData = new FormData();
        formData.append("profilePhotoName", "profilepic.jpg");
        formData.append("file", blob, "profilepic.jpg");

        const photoPostURL = photoRouter.controllers.backend.PhotoController.upload().url;
        const profilePicUpdateURL = photoRouter.controllers.backend.PhotoController.makePhotoProfile(
            userId).url;

        // Send request and handle response
        postMultipart(photoPostURL, formData).then(response => {
            // Read response from server, which will be a json object
            response.json().then(data => {
                if (response.status === 201) {
                    const photoFilename = "/public/storage/photos/" + data;

                    // Create reversible request to update profile photo to this new photo
                    const handler = (status, json) => {
                        if (status === 200) {
                            getProfilePicture(profilePictureControllerUrl);
                            toast("Changes saved!",
                                "Profile picture changes saved successfully.",
                                "success");
                        } else {
                            toast("Error",
                                "Unable to update profile picture", "danger");
                        }
                    };
                    const requestData = new ReqData(requestTypes["UPDATE"],
                        profilePicUpdateURL, handler, photoFilename);
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
    let fullPicturePath = $(this).parent().attr("data-filename");
    //Set the croppers image to this
    profilePictureToCrop.setAttribute('src', fullPicturePath);
    //Show the cropPPModal and hide the changePPModal
    $('#changeProfilePictureModal').modal('hide');
    $('#cropProfilePictureModal').modal('show');
});

function removePhoto(guid, filename) {
    $('#deletePhotoModal').modal('show');
    document.getElementById("deleteMe").setAttribute("src", filename);
    document.getElementById("deleteMe").setAttribute("name", guid);
}

function deletePhoto(route) {
    let guid = document.getElementById("deleteMe").name;
    let deleteUrl = route.substring(0, route.length - 1) + guid;

    _delete(deleteUrl)
    .then(response => {
        response.json().then(data => {
            if (response.status === 200) {
                $('#deletePhotoModal').modal('hide');
                fillGallery(getAllPhotosUrl, 'main-gallery', 'page-selection');
                toast("Picture deleted!",
                    "The photo will no longer be displayed in the gallery.",
                    "success");
            }
        });
    });
}

/**
 * allows the upload image button to act as an input field by clicking on the upload image file field
 */
$("#upload-image-button").click(function () {
    $("#upload-image-file").click();
});

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
 * the profile picture path to it.
 *
 * @param url the backend PhotoController url
 */
function getProfilePicture(url) {
    profilePictureControllerUrl = url;
    get(profilePictureControllerUrl).then(response => {
        // Read response from server, which will be a json object
        if (response.status === 200) {
            response.json().then(data => {
                $("#ProfilePicture").attr("src", data.filename);
            });
        } else if (response.status === 404) {
            $("#ProfilePicture").attr("src",
                "/assets/images/default-profile-picture.jpg");
        }
    });
}

/**
 * Takes a url for the backend controller method to get the users pictures. Then uses this to fill the gallery.
 *
 * @param url the backend PhotoController url
 */
function getPictures(url) {
    getAllPhotosUrl = url;
    fillGallery(getAllPhotosUrl, 'main-gallery', 'page-selection');
}

/**
 * Displays the users images in a change profile picture gallery modal
 */
function showProfilePictureGallery() {
    let galleryObjects = createGalleryObjects(false);
    addPhotos(galleryObjects, $("#profile-gallery"),
        $('#page-selection-profile-picture'));
    $('#changeProfilePictureModal').modal('show');
}

/**
 * allows the upload image button to act as an input field by clicking on the upload image file field
 * For a normal photo
 */
$("#upload-gallery-image-button").click(function () {
    $("#upload-gallery-image-file").click();
});