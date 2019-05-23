/**
 * Function to get the relevant destination and fill the HTML
 * @param {Long} destinationId  of the destination to display
 */
function populateDestinationDetails(destinationId) {
    get(destinationRouter.controllers.backend.DestinationController.getDestination(destinationId).url)
        .then(response => {
            // Read response from server, which will be a json object
            response.json()
                .then(destination => {
                    if(response.status !== 200) {
                        showErrors(destination);
                    } else {
                        document.getElementById("summary_name").innerText = destination.name;
                        document.getElementById("destination_name").innerText = destination.name;
                        document.getElementById("summary_type").innerText = destination._type;
                        document.getElementById("summary_district").innerText = destination.district;
                        document.getElementById("summary_country").innerText = destination.country.name;
                        document.getElementById("summary_latitude").innerText = destination.latitude;
                        document.getElementById("summary_longitude").innerText = destination.longitude;
                        createPrivacyButton(destination.isPublic);
                    }
                })
        })
}

/**
 * Deletes the current destination
 * @param destinationId the id of the destination to delete
 * @param redirect the url to redirect to if the destination is deleted successfully
 */
function deleteDestination(destinationId, redirect) {
    _delete(destinationRouter.controllers.backend.DestinationController.deleteDestination(destinationId).url)
        .then(response => {
            response.json().then(data => {
                if (response.status === 200) {
                    $('#deleteDestinationModalModal').modal('hide');
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
   console.log("change privacy to public");
   put(destinationRouter.controllers.backend.DestinationController.makeDestinationPublic(destinationId).url)
       .then(response => {
           response.json().then(data => {
               if (response.status === 200) {
                   $("#makeDestinationPublicModal").modal('hide');
                   createPrivacyButton(true);
                   toast('Destination Privacy Changed', 'The destination is now public.', 'success');
               } else {
                   toast('Error changing privacy', response.toString(), 'danger', 5000);
               }
           });
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
        privacyWrapper.append(isPublicImage)
    } else {
        const makePublicButton = document.createElement("input");
        makePublicButton.type = "image";
        makePublicButton.classList.add("privacy-image");
        makePublicButton.title = "Destination is Private, Click to make public.";
        makePublicButton.src = "/assets/images/private.png";
        makePublicButton.setAttribute("data-toggle", "modal");
        makePublicButton.setAttribute("data-target", "#makeDestinationPublicModal");
        privacyWrapper.append(makePublicButton)
    }
}