var countryDict = {};
var travellerTypeDict = {};
var usersPhotos = [];
var profilePictureControllerUrl;

// Runs get countries method, then add country options to drop down
function fillCountryInfo(getCountriesUrl) {
    // Run a get request to fetch all destinations
    get(getCountriesUrl)
    // Get the response of the request
        .then(response => {
        // Convert the response to json
        response.json().then(data => {
            // Json data is an array of destinations, iterate through it
            for(let i = 0; i < data.length; i++) {
        // Also add the item to the dictionary
        countryDict[data[i]['id']] = data[i]['name'];
    }
    // Now fill the selects
    fillNationalityDropDown();
    fillPassportDropDown();
});
});
}

function fillTravellerTypes(getTravellerTypesUrl) {
    // Run a get request to fetch all travellers types
    get(getTravellerTypesUrl)
    // Get the response of the request
        .then(response => {
        // Convert the response to json
        response.json().then(data => {
            // "data" should now be a list of traveller type definitions
            // E.g data[0] = { id:1, description:"backpacker"}
            for(let i = 0; i < data.length; i++) {
        // Also add the item to the dictionary
        travellerTypeDict[data[i]['id']] = data[i]['description'];
    }
    // Now fill the drop down box, and list of destinations
    fillTravellerDropDown();

});
});
}

function fillNationalityDropDown() {
    for(let key in countryDict) {
        // For each destination, make a list element that is the json string of object
        let item = document.createElement("OPTION");
        item.innerHTML = countryDict[key];
        item.value = key;
        // Add list element to drop down list
        document.getElementById("nationalities").appendChild(item);
    }
    // implements the plug in multi selector
    $('#nationalities').picker();
}

function fillPassportDropDown() {
    for(let key in countryDict) {
        // For each destination, make a list element that is the json string of object
        let item = document.createElement("OPTION");
        item.innerHTML = countryDict[key];
        item.value = key;
        // Add list element to drop down list
        document.getElementById("passports").appendChild(item);
    }
    // implements the plug in multi selector
    $('#passports').picker();
}

function fillTravellerDropDown() {
    for(let key in travellerTypeDict) {
        // For each Traveller type, make a list element that is the json string of object
        let item = document.createElement("OPTION");
        item.innerHTML = travellerTypeDict[key];
        item.value = key;
        // Add list element to drop down list
        document.getElementById("travellerTypes").appendChild(item);
    }
    // implements the plug in multi selector
    $('#travellerTypes').picker();
}


/* Display gender drop down the same as the others */
$('#gender').picker();

/**
 * The JavaScript function to process a client updating there profile
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function updateProfile(url, redirect) {
    console.log("zza");
    // Read data from destination form
    const formData = new FormData(document.getElementById("updateProfileForm"));
    console.log("zza");
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    console.log("zza");
    // Convert nationalities, passports and Traveller Types to Correct JSON appropriate format
    data.nationalities = [];
    let nat_ids = $.map($(document.getElementById("nationalities")).picker('get'),Number);
    for (let i = 0; i < nat_ids.length; i++) {
        let nat = {};
        nat.id = nat_ids[i];
        data.nationalities.push(nat);
    }
    data.passports = [];
    let passport_ids = $.map($(document.getElementById("passports")).picker('get'),Number)
    for (let i = 0; i < passport_ids.length; i++) {
        let passport = {};
        passport.id = passport_ids[i];
        data.passports.push(passport);
    }
    data.travellerTypes  = [];
    let type_ids = $.map($(document.getElementById("travellerTypes")).picker('get'),Number);
    for (let i = 0; i < type_ids.length; i++) {
        let type = {};
        type.id = type_ids[i];
        data.travellerTypes.push(type);
    }
    console.log("zza");
    // Post json data to given url
    console.log(data);
    put(url,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
            .then(json => {
            if (response.status !== 200) {
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

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * The javascript method to populate the select boxes on the edit profile scene
 * @param url the route/url to send the request to to get the profile data
 */
function populateProfileData(url) {
    get(url)
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
                    console.log(data);
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
    var thumbnailPath = $(this).attr("src");
    //Convert the thumbnailPath to the fullPicturePath
    var fullPicturePath = thumbnailPath.replace('thumbnails/','');
    //Set the croppers image to this
    profilePictureToCrop.setAttribute('src', fullPicturePath);
    //Show the cropPPModal and hide the changePPModal
    $('#changeProfilePictureModal').modal('hide');
    $('#cropProfilePictureModal').modal('show');
});

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
                for(let i = 0; i < data.length; i++) {
                    // Also add the item to the dictionary
                    usersPhotos[i] = data[i];
                }
                // data.length is the total number of photos
                if (data.length > 0) {
                    // Now create gallery objects
                    var galleryObjects = createGalleryObjects(true);
                    // And populate the gallery!
                    addPhotos(galleryObjects, $("#main-gallery"), $('#page-selection'));
                }
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

            //Will only add full size links and removal buttons if requested
            if (hasFullSizeLinks === true) {
                // Create delete button
                var deleteButton = document.createElement("span");
                deleteButton.setAttribute("class", "close");
                deleteButton.innerHTML = "&times;";
                tile.appendChild(deleteButton);
                photo.href = "assets/" + filename;
            }

            // 6 * page + position finds the correct photo index in the dictionary
            var filename = usersPhotos[(6 * page + position)]["filename"];
            var guid = usersPhotos[(6 * page + position)]["guid"];
            photo.setAttribute("data-id", guid);
            photo.setAttribute("data-filename", "assets/" + filename);
            // thumbnail
            var thumbnail = usersPhotos[(6 * page + position)]["thumbnailFilename"];
            var thumb = document.createElement("img");
            thumb.src = "assets/" + thumbnail;
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
            galleryObjects[page] = newGallery;
        }
        return galleryObjects;
    }
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

    if (galleryObjects !== undefined && galleryObjects.length != 0) {
        // init bootpage
        $(pageSelectionId).bootpag({
            total: numPages,
            maxVisible: 5,
            leaps: false,
            href: "#gallery-page-{{number}}",
        }).on("page", function(event, num){
            var gallery = galleryObjects[(num-1)];
            $(galleryId).html(gallery);
            baguetteBox.run('.tz-gallery');
        });
        // set first page
        $(galleryId).html(galleryObjects[(0)]);
        baguetteBox.run('.tz-gallery');
        $('.img-wrap .close').on('click', function() {
            var guid = $(this).closest('.img-wrap').find('a').data("id");
            var filename = $(this).closest('.img-wrap').find('a').data("filename");

            removePhoto(guid, filename);
        });
    }
}

function removePhoto(guid, filename) {
    $('#deletePhotoModal').modal('show');
    document.getElementById("deleteMe").setAttribute("src", filename);
    document.getElementById("deleteMe").setAttribute("name", guid);
}

function deletePhoto() {
    var guid = document.getElementById("deleteMe").name;
    var deleteUrl = "api/photo/" + guid;
    _delete(deleteUrl).then(
        response => {
            $('#deletePhotoModal').modal('hide');
            fillGallery("/api/photo/getAll")
        });
}

/**
 * Sets up the dropzone properties, like having a remove button
 */
function setupDropZone() {

    var maxImageWidth = 350, maxImageHeight = 350;

    Dropzone.options.addPhotoDropzone = {
        acceptedFiles: '.jpeg,.png,.jpg',
        addRemoveLinks: true,
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
                if (file.width < maxImageWidth || file.height < maxImageHeight) {
                    file.rejectDimensions()
                }
                else {
                    file.acceptDimensions();
                }
            });


            submitButton.addEventListener("click", function() {
                if (submitButton.innerText === "Add") {
                    addPhotoDropzone.processQueue(); // Tell Dropzone to process all queued files.
                    submitButton.innerText = "Done";
                } else {
                    $('#uploadPhotoModal').modal('hide');
                    fillGallery("/api/photo/getAll");
                }
            });

            cancelButton.addEventListener("click", function() {
                addPhotoDropzone.removeAllFiles(true);
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
            console.log(response);
        },
        removedfile: function (file, data) {
            var deleteUrl = "api/photo/:25";
            _delete(deleteUrl)
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
 * Displays the users images in a change profile picture gallery modal
 */
function showProfilePictureGallery() {
    var galleryObjects = createGalleryObjects(false);
    addPhotos(galleryObjects, $("#profile-gallery"), $('#page-selection-profile-picture'));
    $('#changeProfilePictureModal').modal('show');
}

function changeImg() {

    if (document.getElementById("privacyImg").title === @routes.Assets.at("images/public.png"))
    {
        document.getElementById("privacyImg").src = @routes.Assets.at("images/private.png");
        document.getElementById("privacyImg").title = "Private";
    }
    else
    {
        document.getElementById("privacyImg").src = @routes.Assets.at("images/public.png");
        document.getElementById("privacyImg").title = "Public";
    }
}

