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
                let galleryObjects = createGalleryObjects(true);
                // And populate the gallery!
                addPhotos(galleryObjects, $("#main-gallery"), $('#page-selection'));
            });
        });
}

/**
 * allows the upload image button to act as an input field by clicking on the upload image file field
 * For a normal photo
 */
$("#upload-gallery-image-button").click(function() {
    $("#upload-gallery-image-file").click();
});

/**
 * Creates gallery objects from the users photos to display on picture galleries.
 *
 * @param hasFullSizeLinks a boolean to if the gallery should have full photo links when clicked.
 * @returns {Array} the array of photo gallery objects
 */
function createGalleryObjects(hasFullSizeLinks) {
    let galleryObjects = [];
    let numPages = Math.ceil(usersPhotos.length / 6);
    for(let page = 0; page < numPages; page++) {
        // page is the page number starting from 0
        // Create a gallery which will have 6 photos
        let newGallery = document.createElement("div");
        newGallery.id = "page" + page;
        newGallery.setAttribute("class", "tz-gallery");
        // create the row div
        let row = document.createElement("div");
        row.setAttribute("class", "row");
        // create each photo tile
        for (let position = 0; position <= 5 && (6 * page + position) < usersPhotos.length; position++) {
            let tile = document.createElement("div");
            tile.setAttribute("class", "img-wrap col-sm6 col-md-4");

            let photo = document.createElement("a");
            photo.setAttribute("class", "lightbox");

            // 6 * page + position finds the correct photo index in the dictionary
            const filename = usersPhotos[(6 * page + position)]["filename"];
            const guid = usersPhotos[(6 * page + position)]["guid"];
            const isPublic = usersPhotos[(6 * page + position)]["isPublic"];

            //Will only add full size links and removal buttons if requested
            if (hasFullSizeLinks === true) {
                if (canEdit === true) {
                    // Create delete button
                    let deleteButton = document.createElement("span");
                    deleteButton.setAttribute("class", "close");
                    deleteButton.innerHTML = "&times;";
                    tile.appendChild(deleteButton);

                    // Create toggle button TODO this is in an ugly position, will change
                    let toggleButton = document.createElement("span");
                    let toggleLabel = document.createElement("input");
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
            let thumbnail = usersPhotos[(6 * page + position)]["thumbnailFilename"];
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
 * Adds galleryObjects to a gallery with a gallryID and a pageSelectionID
 *
 * @param galleryObjects a list of photo objects to insert
 * @param galleryId the id of the gallery to populate
 * @param pageSelectionId the id of the page selector for the provided gallery
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
        }).on("page", function(event, num){
            currentPage = num;
            $(galleryId).html(galleryObjects[currentPage - 1]);
            baguetteBox.run('.tz-gallery');
            $('.img-wrap .close').on('click', function() {
                let guid = $(this).closest('.img-wrap').find('a').data("id");
                let filename = $(this).closest('.img-wrap').find('a').data("filename");

                removePhoto(guid, filename);
            });
        });
        // set first page
        $(galleryId).html(galleryObjects[currentPage - 1]);
        baguetteBox.run('.tz-gallery');
        $('.img-wrap .close').on('click', function() {
            let guid = $(this).closest('.img-wrap').find('a').data("id");
            let filename = $(this).closest('.img-wrap').find('a').data("filename");
            removePhoto(guid, filename);
        });
    } else {
        $(galleryId).html("There are no photos!");
    }
}
