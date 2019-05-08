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
    data.travellerTypes  = JSONFromDropDowns("travellerTypes");
    // Post json data to given uri
    put(uri,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
        if (response.status != 200) {
        showErrors(json);
    } else {
        hideErrors("updateProfileForm");
        let element = document.getElementById("SuccessMessage");
        element.innerHTML = "Successfully Updated!";
        return sleep(3000);
    }
})
.then(() => {
        let element = document.getElementById("SuccessMessage");
    element.innerHTML = "";
})
});
}

/**
 * Updates a trips privacy when the toggle is used
 * @param {string} uri Route for updating trip privacy
 * @param {string} imageSrc Source of new icon image to use
 * @param {Number} tripId Id of trip to update
 * @param {string} newPrivacy Privacy status selected by user
 */
function updateTripPrivacy(uri, imageSrc, tripId, newPrivacy) {
    let currentPrivacy = document.getElementById("privacyImg").title;

    // Don't need to update privacy to the same status
    if (currentPrivacy === newPrivacy) {
        return;
    }

    let tripData = {
        "id": tripId
    };

    if (newPrivacy === "Public") {
        tripData["privacy"] = 1;
    }
    else if (newPrivacy === "Private") {
        tripData["privacy"] = 0;
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
                    document.getElementById("privacyImg").title = newPrivacy;
                    document.getElementById("privacyImg").src = imageSrc;
                }
            });
    });
}

/**
 * Returns timout promise
 * @param {Number} ms - time in millieseconds
 */
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
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
        for (i = 0; i < json.nationalities.length; i++) {
        // iterates through the list of nationalities and adds them to the dropdown via their id
        $('#nationalities').picker('set', json.nationalities[i].id);
    }
    for (i = 0; i < json.passports.length; i++) {
        $('#passports').picker('set', json.passports[i].id);
    }
    for (i = 0; i < json.travellerTypes.length; i++) {
        $('#travellerTypes').picker('set', json.travellerTypes[i].id);
    }
    $('#gender').picker('set', json.gender);
});
}

/**
 * Variables for selecting and cropping the profile picture.
 */
var cropGallery = $('#profile-gallery');
var profilePictureToCrop = document.getElementById('image');
var profilePictureSize = 350;
var cropper;

var usersPhotos = [];
var getAllPhotosUrl;
var profilePictureControllerUrl;
var canEdit;

function setPermissions(loggedUser, user) {
    canEdit = (loggedUser === user);
}

/**
 * Loads the cropper into the page when the cropProfilePictureModal opens
 * Ensures that a crop cannot be smaller than the profilePictureSize
 */
$(document).ready(function() {
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

            cropmove: function(event) {
                var data = cropper.getData();
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
 function uploadProfilePicture(url) {
    //Get the cropped image and set the size to 290px x 290px
    cropper.getCroppedCanvas({width: 350, height: 350}).toBlob(function (blob) {
        var formData = new FormData();
        formData.append("profilePhotoName", "profilepic.jpg");
        formData.append("file", blob, "profilepic.jpg");

        // Send request and handle response
        postMultipart(url, formData).then(response => {
                // Read response from server, which will be a json object
                response.json().then(data => {
                    if (response.status === 201) {
                        //Sets the profile picture to the new image
                        getProfilePicture(profilePictureControllerUrl);
                    }
                });
        });
    });

    //TODO: Needs to also refresh the users picture gallery if the photo is new

    $('#cropProfilePictureModal').modal('hide');
    cropper.destroy();
}

/**
 * Displays the full size image of the thumbnail picture clicked in the cropper window.
 * Also hides the changeProfilePictureModal and shows the cropProfilePictureModal
 */
cropGallery.on('click','img',function() {
    //Get the path for the pictures thumbnail
    var fullPicturePath = $(this).parent().attr("data-filename");
    //Set the croppers image to this
    profilePictureToCrop.setAttribute('src', fullPicturePath);
    //Show the cropPPModal and hide the changePPModal
    $('#changeProfilePictureModal').modal('hide');
    $('#cropProfilePictureModal').modal('show');
});

function togglePrivacy(guid, newPrivacy) {
    const label = document.getElementById(guid + "privacy");
    const data = {
        "isPublic" : newPrivacy
    }
    patch(photoRouter.controllers.backend.PhotoController.togglePhotoPrivacy(guid).url, data)
    .then(res => {
        if (res.status === 200) {
            label.innerHTML = newPrivacy ? "Public" : "Private";
            if (newPrivacy) {
                label.setAttribute("src", "/assets/images/public.png");
            } else {
                label.setAttribute("src", "/assets/images/private.png");
            }
            label.setAttribute("onClick","togglePrivacy(" + guid + "," + !newPrivacy + ")");
        }
    })
}

/**
 * Function to populate gallery with current users photos
 */
function fillGallery(getPhotosUrl) {
    // Run a get request to fetch all users photos
    get(getPhotosUrl)
    // Get the response of the request
        .then(response => {
            // Convert the response to json
            response.json().then(data => {
                // "data" should now be a list of photo models for the given user
                // E.g data[0] = { id:1, filename:"example", thumbnail_filename:"anotherExample"}
                usersPhotos = [];
                for(let i = 0; i < data.length; i++) {
                    // Also add the item to the dictionary
                    usersPhotos[i] = data[i];
                }
                // Now create gallery objects
                var galleryObjects = createGalleryObjects(true);
                // And populate the gallery!
                addPhotos(galleryObjects, $("#main-gallery"), $('#page-selection'));
            });
        });
}

/**
 * Creates gallery objects from the users photos to display on picture galleries.
 *
 * @param hasFullSizeLinks a boolean to if the gallery should have full photo links when clicked.
 * @returns {Array} the array of photo gallery objects
 */
function createGalleryObjects(hasFullSizeLinks) {
    var galleryObjects = [];
    var numPages = Math.ceil(usersPhotos.length / 6);
    for(let page = 0; page < numPages; page++) {
        // page is the page number starting from 0
        // Create a gallery which will have 6 photos
        var newGallery = document.createElement("div");
        newGallery.id = "page" + page;
        newGallery.setAttribute("class", "tz-gallery");
        // create the row div
        var row = document.createElement("div");
        row.setAttribute("class", "row");
        // create each photo tile
        for (let position = 0; position <= 5 && (6 * page + position) < usersPhotos.length; position++) {
            var tile = document.createElement("div");
            tile.setAttribute("class", "img-wrap col-sm6 col-md-4");

            var photo = document.createElement("a");
            photo.setAttribute("class", "lightbox");

            // 6 * page + position finds the correct photo index in the dictionary
            const filename = usersPhotos[(6 * page + position)]["filename"];
            const guid = usersPhotos[(6 * page + position)]["guid"];
            const isPublic = usersPhotos[(6 * page + position)]["isPublic"];

            //Will only add full size links and removal buttons if requested
            if (hasFullSizeLinks === true) {
                if (canEdit === true) {
                    // Create delete button
                    var deleteButton = document.createElement("span");
                    deleteButton.setAttribute("class", "close");
                    deleteButton.innerHTML = "&times;";
                    tile.appendChild(deleteButton);

                    // Create toggle button TODO this is in an ugly position, will change
                    var toggleButton = document.createElement("span");
                    var toggleLabel = document.createElement("input");
                    toggleLabel.setAttribute("class", "privacy");
                    toggleLabel.setAttribute("id", guid + "privacy");
                    toggleLabel.setAttribute("type", "image");

                    if (isPublic) {
                        toggleLabel.setAttribute("src", "/assets/images/public.png");
                    } else {
                        toggleLabel.setAttribute("src", "/assets/images/private.png");
                    }

                    toggleLabel.innerHTML = isPublic ? "Public" : "Private";
                    toggleLabel.setAttribute("onClick","togglePrivacy(" + guid + "," + !isPublic + ")");
                    toggleButton.appendChild(toggleLabel);
                    tile.appendChild(toggleButton);
                }
                photo.href = filename;
            }

            photo.setAttribute("data-id", guid);
            photo.setAttribute("data-filename", filename);
            // thumbnail
            var thumbnail = usersPhotos[(6 * page + position)]["thumbnailFilename"];
            var thumb = document.createElement("img");
            thumb.src = thumbnail;
            // add image to photo a
            photo.appendChild(thumb);
            // add photo a to the tile div
            tile.appendChild(photo);
            // add the entire tile, with image and thumbnail to the row div
            row.appendChild(tile);
            // row should now have 6 or less individual 'tiles' in it.
            // add the row to the gallery div
            newGallery.appendChild(row);

            // Add the gallery page to the galleryObjects
        }
        galleryObjects[page] = newGallery;
    }
    return galleryObjects;
}



/**
 * Adds galleryObjects to a gallery with a gallryID and a pageSelectionID
 *
 * @param galleryObjects a list of photo objects to insert
 * @param galleryId the id of the gallery to populate
 * @param pageSelectionId the id of the page selector for the provided gallery
 */
function addPhotos(galleryObjects, galleryId, pageSelectionId) {
    var numPages = Math.ceil(usersPhotos.length / 6);
    var currentPage = 1;
    if (galleryObjects !== undefined && galleryObjects.length !== 0) {
        // init bootpage
        $(pageSelectionId).bootpag({
            total: numPages,
            maxVisible: 5,
            page: 1,
            leaps: false,
        }).on("page", function(event, num){
            currentPage = num;
            $(galleryId).html(galleryObjects[currentPage - 1]);
            baguetteBox.run('.tz-gallery');
            $('.img-wrap .close').on('click', function() {
                var guid = $(this).closest('.img-wrap').find('a').data("id");
                var filename = $(this).closest('.img-wrap').find('a').data("filename");

                removePhoto(guid, filename);
            });
        });
        // set first page
        $(galleryId).html(galleryObjects[currentPage - 1]);
        baguetteBox.run('.tz-gallery');
        $('.img-wrap .close').on('click', function() {
            var guid = $(this).closest('.img-wrap').find('a').data("id");
            var filename = $(this).closest('.img-wrap').find('a').data("filename");

            removePhoto(guid, filename);
        });
    } else {
        $(galleryId).html("There are no photos!");
    }
}

function removePhoto(guid, filename) {
    $('#deletePhotoModal').modal('show');
    document.getElementById("deleteMe").setAttribute("src", filename);
    document.getElementById("deleteMe").setAttribute("name", guid);
}

function deletePhoto(route) {
    var guid = document.getElementById("deleteMe").name;
    var deleteUrl = route.substring(0, route.length -1 ) + guid;

    _delete(deleteUrl)
        .then(response => {
            response.json().then(data => {
                if (response.status === 200) {
                    $('#deletePhotoModal').modal('hide');
                    fillGallery(getAllPhotosUrl);
                }
            });
        });
}

/**
 * Sets up the dropzone properties, like having a remove button
 */
function setupDropZone() {
    Dropzone.options.addPhotoDropzone = {
        acceptedFiles: '.jpeg,.png,.jpg',
        dictRemoveFile: "remove",
        thumbnailWidth: 200,
        thumbnailHeight: 200,
        dictDefaultMessage: '',
        autoProcessQueue: false,

        init: function() {
            var submitButton = document.querySelector("#submit-all");
            var cancelButton = document.querySelector("#remove-all");
            var addPhotoDropzone = this;

            this.on("addedfile", function () {
                // Enable add button
                submitButton.disabled = false;
                submitButton.innerText = "Add"
            });

            this.on("thumbnail", function(file) {
                // Do the dimension checks you want to do
                if (file.width < profilePictureSize || file.height < profilePictureSize) {
                    file.rejectDimensions()
                }
                else {
                    file.acceptDimensions();
                }
            });

            this.on("processing", function() {
                this.options.autoProcessQueue = true;
            });

            submitButton.addEventListener("click", function() {
                if (submitButton.innerText === "Add") {
                    addPhotoDropzone.processQueue(); // Tell Dropzone to process all queued files.
                    submitButton.innerText = "Done";
                    document.getElementById("remove-all").hidden = true;
                } else {
                    document.getElementById("remove-all").hidden = false;
                    addPhotoDropzone.options.autoProcessQueue = false;
                    addPhotoDropzone.removeAllFiles();
                    $('#uploadPhotoModal').modal('hide');
                    fillGallery(getAllPhotosUrl);
                }
            });

            cancelButton.addEventListener("click", function() {
                addPhotoDropzone.removeAllFiles();
                addPhotoDropzone.options.autoProcessQueue = false;
                submitButton.disabled = true;
                submitButton.innerText = "Add"
            });
        },
        accept: function(file, done) {
            file.acceptDimensions = done;
            file.rejectDimensions = function() {
                done("Image too small.");
            };
        },
        success: function(file, response) {
            file.serverFileName = response[0];
        }
    };
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
    var galleryObjects = createGalleryObjects(false);
    addPhotos(galleryObjects, $("#profile-gallery"), $('#page-selection-profile-picture'));
    $('#changeProfilePictureModal').modal('show');
}

