/* Display gender drop down the same as the others */
$('#gender').picker();

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
    // Post json data to given uri
    put(uri, data)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showErrors(json);
            } else {
                updateProfileData(data);
                $("#editProfileModal").modal('hide');
                toast("Profile Updated!",
                    "The updated information will be displayed on your profile.",
                    "success");
            }
        })
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
        destinationRouter.controllers.backend.DestinationController.getAllCountries().url)
    .then(out => {
        document.getElementById("summary_nationalities").innerHTML = out;
    });
    arrayToString(data.passports, 'name',
        destinationRouter.controllers.backend.DestinationController.getAllCountries().url)
    .then(out => {
        document.getElementById("summary_passports").innerHTML = out;
    });
    arrayToString(data.travellerTypes, 'description',
        profileRouter.controllers.backend.ProfileController.getAllTravellerTypes().url)
    .then(out => {
        document.getElementById("summary_travellerTypes").innerHTML = out;
    });
}

/**
 * The javascript method to populate the select boxes on the edit profile scene
 * @param url the route/url to send the request to to get the profile data
 */
function populateProfileData(uri) {
    get(uri)
    .then(response => {
        // Read response from server, which will be a json object
        return response.json()
    })
    .then(json => {
        // Done this way because otherwise the json obbject is formatted really weirdly and you cant access stuff
        for (let i = 0; i < json.nationalities.length; i++) {
            // iterates through the list of nationalities and adds them to the dropdown via their id
            $('#nationalities').picker('set', json.nationalities[i].id);
        }
        for (let i = 0; i < json.passports.length; i++) {
            $('#passports').picker('set', json.passports[i].id);
        }
        for (let i = 0; i < json.travellerTypes.length; i++) {
            $('#travellerTypes').picker('set', json.travellerTypes[i].id);
        }
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

let usersPhotos = [];
let getAllPhotosUrl;
let profilePictureControllerUrl;
let canEdit;

function setPermissions(loggedUser, user) {
    canEdit = (loggedUser === user);
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
 * @param {string} url the url to post image to
 */
function uploadProfilePicture(url) {
    //Get the cropped image and set the size to 290px x 290px
    cropper.getCroppedCanvas({width: 350, height: 350}).toBlob(function (blob) {
        let formData = new FormData();
        formData.append("profilePhotoName", "profilepic.jpg");
        formData.append("file", blob, "profilepic.jpg");

        // Send request and handle response
        postMultipart(url, formData).then(response => {
            // Read response from server, which will be a json object
            response.json().then(data => {
                if (response.status === 201) {
                    //Sets the profile picture to the new image
                    getProfilePicture(profilePictureControllerUrl);
                    toast("Profile Picture Updated!",
                        "This will be displayed on your profile.", "success");
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

function togglePrivacy(guid, newPrivacy) {
    const label = document.getElementById(guid + "privacy");
    const data = {
        "isPublic": newPrivacy
    }
    patch(photoRouter.controllers.backend.PhotoController.togglePhotoPrivacy(
        guid).url, data)
    .then(res => {
        if (res.status === 200) {
            label.innerHTML = newPrivacy ? "Public" : "Private";
            if (newPrivacy) {
                label.setAttribute("src", "/assets/images/public.png");
            } else {
                label.setAttribute("src", "/assets/images/private.png");
            }
            label.setAttribute("onClick",
                "togglePrivacy(" + guid + "," + !newPrivacy + ")");
            toast("Picture privacy changed!",
                "The photo is now " + (newPrivacy ? "Public" : "Private"),
                "success");
        }
    })
}

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
                fillGallery(getAllPhotosUrl);
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
    console.log("upload clicked");
    $("#upload-image-file").click();
});

/**
 * Takes the users selected photo file and creates a url object out of it. This is then passed to cropper.
 * The appropriate modals are shown and hidden.
 */
function uploadNewPhoto() {
    console.log("upload new photo");
    const selectedFile = document.getElementById('upload-image-file').files[0];
    profilePictureToCrop.setAttribute('src',
        window.URL.createObjectURL(selectedFile));
    //Show the cropPPModal and hide the changePPModal
    $('#changeProfilePictureModal').modal('hide');
    $('#cropProfilePictureModal').modal('show');
}

/**
 * Takes the users selected photos and  creates a form from them
 * Sends this form to  the appropriate url
 *
 * @param {string} url the appropriate  photo backend controller
 */
function uploadNewGalleryPhoto(url) {
    const selectedPhotos = document.getElementById(
        'upload-gallery-image-file').files;
    let formData = new FormData();
    for (let i = 0; i < selectedPhotos.length; i++) {
        formData.append("file", selectedPhotos[i], selectedPhotos[i].name)
    }
    // Send request and handle response
    postMultipart(url, formData).then(response => {
        // Read response from server, which will be a json object
        response.json().then(data => {
            if (response.status === 201) {
                fillGallery(getAllPhotosUrl);
                toast("Photo Added!",
                    "The new photo will be shown in the picture gallery.",
                    "success");
            }
        })
    })
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
    fillGallery(getAllPhotosUrl);
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
$("#upload-gallery-image-button").click(function() {
    $("#upload-gallery-image-file").click();
});