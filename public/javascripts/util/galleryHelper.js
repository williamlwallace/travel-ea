/**
 * Takes the users selected photos and  creates a form from them
 * Sends this form to  the appropriate url
 */
$('#upload-img').on('click', function() {
    const url = photoRouter.controllers.backend.PhotoController.upload().url
    //Get the the required ids from data tags in the button eleemnt
    const galleryId = $(this).data('gallery-id');
    const pageId = $(this).data('page-id');

    const caption = $('#caption input').val(); //This will be used when backend is merged in

    const selectedPhotos = document.getElementById(
        'upload-gallery-image-file').files;
    // const arrayOfCaptions = document.getElementById(
    //     'uploaded-photos-captions');            // this element does not exist atm, but i think its what will happen?!
    let formData = new FormData();
    for (let i = 0; i < selectedPhotos.length; i++) {
        formData.append("file", selectedPhotos[i], selectedPhotos[i].name);
        // formData.append('caption', arrayOfCaptions[i]) // and then this is where your send it in. Works with the backend.
    }
    // Send request and handle response
    postMultipart(url, formData).then(response => {
        // Read response from server, which will be a json object
        response.json().then(data => {
            if (response.status === 201) {
                fillGallery(getAllPhotosUrl, galleryId, pageId);
                toast("Photo Added!",
                    "The new photo will be shown in the picture gallery.",
                    "success");
            }
        })
    })
});

let usersPhotos = [];

/**
 * Function to populate gallery with current users photos
 *
 * @param getPhotosUrl the url from where photos are retrieved from, varies for each gallery case
 * @param {string} galleryId the id of the gallery to add the photo to
 * @param {string} pageId the id of the pagination that the gallery is in
 */
function fillGallery(getPhotosUrl, galleryId, pageId) {
    console.log(galleryId);
    // Run a get request to fetch all users photos
    get(getPhotosUrl)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        response.json().then(data => {
            // "data" should now be a list of photo models for the given user
            // E.g data[0] = { id:1, filename:"example", thumbnail_filename:"anotherExample"}
            usersPhotos = [];
            for (let i = 0; i < data.length; i++) {
                data[i]["isOwned"] = true;
                usersPhotos[i] = data[i];
            }
            let galleryObjects = createGalleryObjects(true);
            addPhotos(galleryObjects, $("#" + galleryId), $('#' + pageId));
        });
    });
}

/**
 * Function to populate gallery with current users photos with link destination functionality
 *
 * @param getPhotosUrl the url from where photos are retrieved from, varies for each gallery case
 * @param {string} galleryId the id of the gallery to add the photo to
 * @param {string} pageId the id of the pagination that the gallery is in
 * @param {Long} destinationId the id of the destination to link the photos to
 */
function fillLinkGallery(getPhotosUrl, galleryId, pageId, destinationId) {
    // Run a get request to fetch all users photos
    get(getPhotosUrl)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        response.json().then(data => {
            usersPhotos = [];
            get(photoRouter.controllers.backend.PhotoController.getDestinationPhotos(
                destinationId).url)
            .then(response => {
                response.json().then(linkedPhotos => {
                    for (let i = 0; i < data.length; i++) {
                        data[i]["isLinked"] = false;
                        for (let photo of linkedPhotos) {
                            if (photo.guid === data[i].guid) {
                                data[i]["isLinked"] = true;
                            }
                        }
                        usersPhotos[i] = data[i];
                    }
                    let galleryObjects = createGalleryObjects(false, true,
                        destinationId);
                    addPhotos(galleryObjects, $("#" + galleryId),
                        $('#' + pageId));
                })
            });
        });
    });
}

/**
 * Function to populate gallery with current photos from a certain destination
 * sets a isOwn variable
 *
 * @param {string} getDestinationPhotosUrl the url from where destination photos are retrieved from
 * @param {string} getUserPhotosUrl the url where all users photos are from
 * @param {string} galleryId the id of the gallery to add the photo to
 * @param {string} pageId the id of the pagination that the gallery is in
 * @param {Long} destinationId the id of the destination to link the photos to
 */
function fillDestinationGallery(getDestinationPhotosUrl, getUserPhotosUrl,
    galleryId, pageId, destinationId) {
    // Run a get request to fetch all users photos
    get(getDestinationPhotosUrl)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        response.json().then(destinationPhotos => {
            usersPhotos = [];
            get(getUserPhotosUrl)
            .then(response => {
                response.json().then(ownedPhotos => {
                    for (let i = 0; i < destinationPhotos.length; i++) {
                        destinationPhotos[i]["isOwned"] = false;
                        for (let photo of ownedPhotos) {
                            if (photo.guid === destinationPhotos[i].guid) {
                                destinationPhotos[i]["isOwned"] = true;
                            }
                        }
                        usersPhotos[i] = destinationPhotos[i];
                    }
                    let galleryObjects = createGalleryObjects(true);
                    addPhotos(galleryObjects, $("#" + galleryId),
                        $('#' + pageId));
                })
            });
        });
    });
}

/**
 * Function to populate gallery with current users photos.
 * Takes a selectionFunction that will be set to each photos on click.
 *
 * @param getPhotosUrl the url from where photos are retrieved from, varies for each gallery case
 * @param {string} galleryId the id of the gallery to add the photo to
 * @param {string} pageId the id of the pagination that the gallery is in
 * @param {function} selectionFunction, the function that will be called when a photo is clicked on
 */
function fillSelectionGallery(getPhotosUrl, galleryId, pageId, selectionFunction) {
    // Run a get request to fetch all users photos
    get(getPhotosUrl)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        response.json().then(data => {
            // "data" should now be a list of photo models for the given user
            // E.g data[0] = { id:1, filename:"example", thumbnail_filename:"anotherExample"}
            usersPhotos = [];
            for (let i = 0; i < data.length; i++) {
                data[i]["canSelect"] = true;
                usersPhotos[i] = data[i];
            }
            let galleryObjects = createGalleryObjects(false, false, null, selectionFunction);
            addPhotos(galleryObjects, $("#" + galleryId), $('#' + pageId));
        });
    });
}


/**
 * Creates gallery objects from the users photos to display on picture galleries.
 *
 * @param {boolean} hasFullSizeLinks a boolean to if the gallery should have full photo links when clicked.
 * @param {boolean} withLinkButton whether the gallery has the buttons to link to destination
 * @param {Long} destinationId the id of the destination to link the photos to
 * @param {function} clickFunction the function that will be called when a photo is clicked
 * @returns {Array} the array of photo gallery objects
 */
function createGalleryObjects(hasFullSizeLinks, withLinkButton = false,
    destinationId = null, clickFunction = null) {
    let galleryObjects = [];
    let numPages = Math.ceil(usersPhotos.length / 6);
    for (let page = 0; page < numPages; page++) {
        // page is the page number starting from 0
        // Create a gallery which will have 6 photos
        let newGallery = document.createElement("div");
        newGallery.id = "page" + page;
        newGallery.setAttribute("class", "tz-gallery");
        // create the row div
        let row = document.createElement("div");
        row.setAttribute("class", "row");
        // create each photo tile
        for (let position = 0;
            position <= 5 && (6 * page + position) < usersPhotos.length;
            position++) {
            let tile = document.createElement("div");
            tile.setAttribute("class", "img-wrap col-sm6 col-md-4");

            let photo = document.createElement("a");
            photo.setAttribute("class", "lightbox");

            // 6 * page + position finds the correct photo index in the dictionary
            const filename = usersPhotos[(6 * page + position)]["filename"];
            const guid = usersPhotos[(6 * page + position)]["guid"];
            const isPublic = usersPhotos[(6 * page + position)]["isPublic"];
            const isLinked = usersPhotos[(6 * page + position)]["isLinked"];
            const isOwned = usersPhotos[(6 * page + position)]["isOwned"];

            //Will only add full size links and removal buttons if requested
            if (hasFullSizeLinks === true) {
                if (canEdit === true && isOwned) {
                    // Create toggle button
                    const toggleButton = createToggleButton(isPublic, guid);
                    tile.appendChild(toggleButton);
                }
                if (canDelete === true) {
                    // Create delete button
                    const deleteButton = createDeleteButton();
                    tile.appendChild(deleteButton);
                }

                photo.href = filename;
            }
            if (clickFunction) {
                photo.addEventListener("click", clickFunction);
                photo.style.cursor = "pointer";
            }
            if (withLinkButton) {
                const linkButton = createLinkButton(isLinked, guid,
                    destinationId);
                tile.appendChild(linkButton)
            }
            photo.setAttribute("data-id", guid);
            photo.setAttribute("data-filename", filename);
            // thumbnail
            let thumbnail = usersPhotos[(6 * page
                + position)]["thumbnailFilename"];
            let thumb = document.createElement("img");
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
 * Helper function to greate the button on the photo that toggles privacy
 * @param {boolean} isPublic current state of the photo
 * @param {Long} guid the id of the photo on which to create a toggle button
 * @returns {HTMLElement} the created toggle button to add to the photo
 */
function createToggleButton(isPublic, guid) {
    const toggleButton = document.createElement("span");
    const toggleLabel = document.createElement("input");
    toggleLabel.setAttribute("class", "privacy");
    toggleLabel.setAttribute("id", guid + "privacy");
    toggleLabel.setAttribute("type", "image");

    if (isPublic) {
        toggleLabel.setAttribute("src",
            "/assets/images/public.png");
    } else {
        toggleLabel.setAttribute("src",
            "/assets/images/private.png");
    }
    toggleLabel.innerHTML = isPublic ? "Public" : "Private";
    toggleLabel.setAttribute("onClick",
        "togglePrivacy(" + guid + "," + !isPublic + ")");
    toggleButton.appendChild(toggleLabel);
    return toggleButton;
}

/**
 * Helper function to create the button on the photo that links a photo to a destination
 * @param {boolean} isLinked current state of the photo re being linked to a destination
 * @param {Long} guid the id of the photo on which to create a link button
 * @param {Long} destinationId the id of the destination the button will link to
 * @returns {HTMLElement} the created toggle button to add to the photo
 */
function createLinkButton(isLinked, guid, destinationId) {
    const linkButton = document.createElement("span");
    const linkLabel = document.createElement("input");
    linkLabel.setAttribute("class", "privacy");
    linkLabel.setAttribute("id", guid + "linked");
    linkLabel.setAttribute("type", "image");

    if (isLinked) {
        linkLabel.setAttribute("src",
            "/assets/images/location-linked.png");
    } else {
        linkLabel.setAttribute("src",
            "/assets/images/location-unlinked.png");
    }
    linkLabel.innerHTML = isLinked ? "Linked" : "Not-Linked";
    linkLabel.setAttribute("onClick",
        "toggleLinked(" + guid + "," + !isLinked + "," + destinationId + ")");
    linkButton.appendChild(linkLabel);
    return linkButton;
}

/**
 * Creates the delete button for photos in gallery
 * @returns {HTMLElement}
 */
function createDeleteButton() {
    let deleteButton = document.createElement("span");
    deleteButton.setAttribute("class", "close");
    deleteButton.innerHTML = "&times;";
    return deleteButton;
}

/**
 * Adds galleryObjects to a gallery with a galleryID and a pageSelectionID
 * If galleryId is link-gallery the arrows to move between photos are removed
 *
 * @param {List} galleryObjects a list of photo objects to insert
 * @param {string} galleryId the id of the gallery to populate
 * @param {string} pageSelectionId the id of the page selector for the provided gallery
 */
function addPhotos(galleryObjects, galleryId, pageSelectionId) {
    let numPages = Math.ceil(usersPhotos.length / 6);
    let currentPage = 1;
    if (galleryObjects !== undefined && galleryObjects.length !== 0) {
        // init bootpage
        $(pageSelectionId).bootpag({
            total: numPages,
            maxVisible: 5,
            page: 1,
            leaps: false,
        }).on("page", function (event, num) {
            currentPage = num;
            $(galleryId).html(galleryObjects[currentPage - 1]);
            baguetteBox.run('.tz-gallery');
            $('.img-wrap .close').on('click', function () {
                let guid = $(this).closest('.img-wrap').find('a').data("id");
                let filename = $(this).closest('.img-wrap').find('a').data(
                    "filename");
                removePhoto(guid, filename);
            });
        });
        // set first page
        $(galleryId).html(galleryObjects[currentPage - 1]);
        baguetteBox.run('.tz-gallery');
        $('.img-wrap .close').on('click', function () {
            let guid = $(this).closest('.img-wrap').find('a').data("id");
            let filename = $(this).closest('.img-wrap').find('a').data(
                "filename");
            removePhoto(guid, filename);
        });
    } else {
        $(galleryId).html("There are no photos!");
    }
}

/**
 * Function that updates the privacy state of a photo
 *
 * @param {Long} guid of photo to update
 * @param {boolean} newPrivacy
 */
function togglePrivacy(guid, newPrivacy) {
    const label = document.getElementById(guid + "privacy");

    const URL = photoRouter.controllers.backend.PhotoController.togglePhotoPrivacy(
        guid).url;
    const handler = function (status, json) {
        if (status !== 200) {
            toast("Failed to change privacy",
                "Privacy has not been changed",
                "error");
        } else {
            label.innerHTML = this.newPrivacy ? "Public" : "Private";
            if (this.newPrivacy) {
                label.setAttribute("src", "/assets/images/public.png");
            } else {
                label.setAttribute("src", "/assets/images/private.png");
            }
            label.setAttribute("onClick",
                "togglePrivacy(" + guid + "," + !this.newPrivacy + ")");
            toast("Picture privacy changed!",
                "The photo is now " + (this.newPrivacy ? "Public" : "Private"),
                "success");
            this.newPrivacy = !this.newPrivacy;
        }
    }.bind({newPrivacy});
    const reqData = new ReqData(requestTypes['UPDATE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

$('#upload-gallery-image-file').on('change', function handleImage(e) {
    const reader = new FileReader();
    reader.onload = function (event) {
        
        $('.image-body img').attr('src',event.target.result);
        $('.image-body').css('display', 'block');
        $('.uploader').css('display', 'none');
    }
    reader.readAsDataURL(e.target.files[0]);
});



