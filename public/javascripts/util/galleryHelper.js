$('#dismiss-modal').on('click', function () {
    const progressBarHolder = $('#progressBarHolder')
    progressBarHolder.attr('style', 'display:none;');

    $('#upload-modal').modal('hide');
});

/**
 * Takes the users selected photos and  creates a form from them
 * Sends this form to  the appropriate url
 */
$('#upload-img').on('click', function () {
    const url = photoRouter.controllers.backend.PhotoController.upload().url;
    const galleryId = $(this).data('gallery-id');
    const pageId = $(this).data('page-id');

    // Disable footer buttons while processing
    $('#upload-img').prop('disabled', true);
    $('#dismiss-modal').prop('disabled', true);

    let caption = $('#caption input').val();
    const tags = uploadTagPicker.getTags().map(tag => {
        return {
            name: tag
        }
    });

    const progressBarHolder = $('#progressBarHolder');
    progressBarHolder.attr('style', 'display:block;height:32px;');
    const progressBar = $('#progressBar');
    progressBar.text(`0%`);
    progressBar.attr('style', `width:0%;font-size:16px;height:32px;`);
    progressBar.attr('aria-valuenow', 0);
    progressBar.attr('class', 'progress-bar progress-bar-info');

    const selectedPhotos = document.getElementById(
        'upload-gallery-image-file').files;
    let formData = new FormData();
    for (let i = 0; i < selectedPhotos.length; i++) {
        formData.append("file", selectedPhotos[i], selectedPhotos[i].name);
        formData.append('caption', caption);
        formData.append("userUploadId", window.location.href.split("/").pop());
        formData.append('tags', JSON.stringify(tags));
    }

    postMultipartWithProgress(url, formData,
        (e) => { // Progress handler
            progressBarHolder.attr('style', 'display:block;height:32px');
            const percent = Math.round((e.loaded / e.total) * 100);
            progressBar.text(`${percent}%`);
            progressBar.attr('style', `width:${percent}%;font-size:16px;height:32px;`);
            progressBar.attr('aria-valuenow', percent);
            progressBar.attr('class', 'progress-bar');
        },
        () => { // End upload handler
            progressBar.attr('class', 'progress-bar progress-bar-animated progress-bar-striped bg-success');
            progressBar.html("Processing image, this may take a few seconds...");
            $('#modal-footer').prop('disabled', true);
        },
        (status, response) => { // On finished handler
            // Read response from server, which will be a json object
            const data = JSON.parse(response);
            if (status === 201) {
                fillGallery(getAllPhotosUrl, galleryId, pageId, mainGalleryPaginationHelper, () => {
                    $('#upload-modal').modal('hide');
                    progressBarHolder.attr('style', 'display:none');
                    // Re-enable footer buttons
                    $('#upload-img').prop('disabled', false);
                    $('#dismiss-modal').prop('disabled', false);
                });

                toast("Photo Added!",
                    "The new photo will appear in the photo gallery",
                    "success");
                getAndFillDD(tagRouter.controllers.backend.TagController.getAllUserPhotoTags(profileId).url, ["tagFilter"], "name", false, "name");
            }
        });
});

let usersPhotos = [];
let profileId = -1;
let getAllPhotosUrl;

/**
 * Sets the profileId as a global variable. Also sets the getAllPhotosUrl
 * @param {Number} profileID the profileId to set
 */
function setProfileId(profileID) {
    profileId = profileID;
    getAllPhotosUrl = photoRouter.controllers.backend.PhotoController.getAllUserPhotos(
        profileId).url;
}

/**
 * Function to filter gallery by tags
 */
$('#tagFilter').on('change', function() {
    const tags = $(this).val();
    const galleryId = "main-gallery";
    const pageId = "page-selection";

    fillGallery(photoRouter.controllers.backend.PhotoController.getAllUserPhotos(
        profileId).url, galleryId, pageId, mainGalleryPaginationHelper, null, tags);

});

/**
 * Function to populate gallery with current users photos
 *
 * @param getPhotosUrl the url from where photos are retrieved from, varies for each gallery case
 * @param {string} galleryId the id of the gallery to add the photo to
 * @param {string} pageId the id of the pagination that the gallery is in
 * @param {paginationHelper} pageHelper pagination helper for the specific gallery
 * @param {function} callback Callback to run after gallery has filled
 * @param {Object} filters the list of tags to filter the gallery by
 */
function fillGallery(getPhotosUrl, galleryId, pageId, pageHelper, callback=null, filters=null) {
    // Run a get request to fetch all users photos
    get(getPhotosUrl)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        response.json().then(data => {
            // "data" should now be a list of photo models for the given user
            // E.g data[0] = { id:1, filename:"example", thumbnail_filename:"anotherExample"}
            usersPhotos = [];
            for (const photo of data.data) {
                if (!filters || filters.length === 0) {
                    photo.isOwned = true;
                    usersPhotos.push(photo);
                } else {
                    for (const filter of filters) {
                        if (photo.tags.map(tag => tag.name).includes(filter)) {
                            photo.isOwned = true;
                            usersPhotos.push(photo);
                            break;
                        }
                    }

                }
            }

            pageHelper.setTotalNumberOfPages(data.totalNumberPages);
            const galleryObjects = createGalleryObjects(true, pageHelper);
            addPhotos(galleryObjects, $("#" + galleryId), $('#' + pageId));

            if(callback !== null) { callback(); }
        });
    });
}

/**
 * Function to populate gallery with current users photos with link destination functionality
 *
 * @param getPhotosUrl the url from where photos are retrieved from, varies for each gallery case
 * @param {String} galleryId the id of the gallery to add the photo to
 * @param {String} pageId the id of the pagination that the gallery is in
 * @param {Number} destinationId the id of the destination to link the photos to
 * @param {paginationHelper} pageHelper pagination helper for the specific gallery
 */
function fillLinkGallery(getPhotosUrl, galleryId, pageId, destinationId, pageHelper) {
    // Run a get request to fetch all users photos
    get(getPhotosUrl)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        response.json().then(photos => {
            usersPhotos = [];
            get(photoRouter.controllers.backend.PhotoController.getDestinationPhotos(
                destinationId).url)
            .then(response => {
                response.json().then(linkedPhotos => {
                    for (let i = 0; i < photos.data.length; i++) {
                        photos.data[i]["isLinked"] = false;
                        for (let photo of linkedPhotos) {
                            if (photo.guid === photos.data[i].guid) {
                                photos.data[i]["isLinked"] = true;
                            }
                        }
                        usersPhotos[i] = photos.data[i];
                    }
                    const galleryObjects = createGalleryObjects(false, pageHelper, true,
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
 * @param {String} getDestinationPhotosUrl the url from where destination photos are retrieved from
 * @param {String} getUserPhotosUrl the url where all users photos are from
 * @param {String} galleryId the id of the gallery to add the photo to
 * @param {String} pageId the id of the pagination that the gallery is in
 * @param {Number} destinationId the id of the destination to link the photos to
 * @param {paginationHelper} pageHelper pagination helper for the specific gallery
 */
function fillDestinationGallery(getDestinationPhotosUrl, getUserPhotosUrl,
    galleryId, pageId, pageHelper, destinationId) {
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
                        for (let photo of ownedPhotos.data) {
                            if (photo.guid === destinationPhotos[i].guid) {
                                destinationPhotos[i]["isOwned"] = true;
                            }
                        }
                        usersPhotos[i] = destinationPhotos[i];
                    }
                    const galleryObjects = createGalleryObjects(true, pageHelper);
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
 * @param {paginationHelper} pageHelper pagination helper for the specific gallery
 */
function fillSelectionGallery(getPhotosUrl, galleryId, pageId,
    selectionFunction, pageHelper) {
    // Run a get request to fetch all users photos
    get(getPhotosUrl)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        response.json().then(data => {
            // "data" should now be a list of photo models for the given user
            // E.g data[0] = { id:1, filename:"example", thumbnail_filename:"anotherExample"}
            usersPhotos = [];
            for (const photo of data.data) {
                photo.canSelect = true;
                usersPhotos.push(photo);
            }
            pageHelper.setTotalNumberOfPages(data.totalNumberPages);
            const galleryObjects = createGalleryObjects(false, pageHelper, false, null, selectionFunction);
            addPhotos(galleryObjects, $("#" + galleryId), $('#' + pageId));
        });
    });
}

/**
 * Creates gallery objects from the users photos to display on picture galleries.
 *
 * @param {boolean} hasFullSizeLinks a boolean to if the gallery should have full photo links when clicked.
 * @param {paginationHelper} pageHelper pagination helper for the specific gallery
 * @param {boolean} withLinkButton whether the gallery has the buttons to link to destination
 * @param {Number} destinationId the id of the destination to link the photos to
 * @param {function} clickFunction the function that will be called when a photo is clicked
 * @returns {Array} the array of photo gallery objects
 */
function createGalleryObjects(hasFullSizeLinks, pageHelper, withLinkButton = false,
    destinationId = null, clickFunction = null) {
    let galleryObjects = [];
    if (usersPhotos.length === 0) {
        return galleryObjects;
    }
    // page is the page number starting from 0
    // Create a gallery which will have 6 photos
    let newGallery = document.createElement("div");
    newGallery.id = "page" + pageHelper.getCurrentPageNumber();
    newGallery.setAttribute("class", "tz-gallery");
    // create the row div
    let row = document.createElement("div");
    row.setAttribute("class", "row");
    // create each photo tile
    for (const position in usersPhotos) {
        let tile = document.createElement("div");
        tile.setAttribute("class", "img-wrap col-sm6 col-md-4");

        let photo = document.createElement("a");
        photo.setAttribute("class", "lightbox");
        const filename = usersPhotos[position]["filename"];
        const guid = usersPhotos[position]["guid"];
        const caption = usersPhotos[position]["caption"];
        const isPublic = usersPhotos[position]["isPublic"];
        const isLinked = usersPhotos[position]["isLinked"];
        const isOwned = usersPhotos[position]["isOwned"];
        const tags = usersPhotos[position]["tags"].map(tag => tag.name);

        //Will only add full size links and removal buttons if requested
        if (hasFullSizeLinks === true) {
            if (canEdit === true && isOwned) {
                // Create toggle button
                const toggleButton = createToggleButton(isPublic, guid);
                tile.appendChild(toggleButton);

            }
            if (canDelete === true) {
                // Create delete button
                // const deleteButton = createDeleteButton();
                // tile.appendChild(deleteButton);
                const editCaptionButton = createEditButton();
                tile.appendChild(editCaptionButton)

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
        photo.setAttribute("data-caption", caption);
        photo.setAttribute("data-tags", tags.join(", "));
        photo.setAttribute("data-filename", filename);
        // thumbnail
        let thumbnail = usersPhotos[position]["thumbnailFilename"];
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
    galleryObjects.push(newGallery);
    return galleryObjects;
}

/**
 * Helper function to create the button on the photo that toggles privacy
 * @param {Boolean} isPublic current state of the photo
 * @param {Number} guid the id of the photo on which to create a toggle button
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
 * @param {Number} guid the id of the photo on which to create a link button
 * @param {Number} destinationId the id of the destination the button will link to
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
 * Creates the edit button for photos in gallery
 * @returns {HTMLElement}
 */
function createEditButton() {
    const editCaptionButton = document.createElement("span");
    const editCaptionIcon = document.createElement("i");
    editCaptionButton.setAttribute("id", "editCaption");
    editCaptionButton.setAttribute("class", "close");
    editCaptionIcon.setAttribute("class", "fas fa-pen fa-1x");
    editCaptionButton.appendChild(editCaptionIcon);
    return editCaptionButton;
}

/**
 * Adds galleryObjects to a gallery with a galleryID and a pageSelectionID
 * If galleryId is link-gallery the arrows to move between photos are removed
 *
 * @param {Array} galleryObjects a list of photo objects to insert
 * @param {string} galleryId the id of the gallery to populate
 * @param {string} pageSelectionId the id of the page selector for the provided gallery
 */
function addPhotos(galleryObjects, galleryId, pageSelectionId) {
    if (galleryObjects !== undefined && galleryObjects.length !== 0) {
        $(galleryId).html(galleryObjects[0]);
        baguetteBox.run('.tz-gallery', {
            captions: function (element) {
                return `${$(element).attr('data-caption')} - ${$(element).attr('data-tags')}`;
            }
        });
        $('.img-wrap .close').on('click', function () {
            let guid = $(this).closest('.img-wrap').find('a').data("id");
            let filename = $(this).closest('.img-wrap').find('a').data(
                "filename");
            populateEditPhoto(guid, filename);
        });
    } else {
        $(galleryId).html("There are no photos!");
    }
}

/**
 * Function that updates the privacy state of a photo
 *
 * @param {Number} guid of photo to update
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
                "The photo is now " + (this.newPrivacy ? "Public" : "Private"));
            this.newPrivacy = !this.newPrivacy;
        }
    }.bind({newPrivacy});
    const reqData = new ReqData(requestTypes['UPDATE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Sets the photo in the upload photo modal to the selected photo from file
 * browser
 */
$('#upload-gallery-image-file').on('change', function handleImage(e) {
    const reader = new FileReader();
    reader.onload = function (event) {

        $('.image-body img').attr('src', event.target.result);
        $('.image-body').css('display', 'block');
        $('.uploader').css('display', 'none');
    };
    reader.readAsDataURL(e.target.files[0]);
});

/**
 * Opens edit photo modal when clicking on edit icon in photo thumbnail
 */
$('#editCaption').on('click', function () {
    $('#upload-modal').show();
});
