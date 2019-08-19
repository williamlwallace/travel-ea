let activeInfoWindow;
let map;
let toggled;

/**
 * Initializes destination page and map
 * @param {Number} userId - ID of user to get destinations for
 */
function onPageLoad(userId) {
    const destinationGetURL = destinationRouter.controllers.backend.DestinationController.getAllDestinations(
        userId).url;
    const options = {
        zoom: 2.2,
        center: {lat: 0, lng: 0}
    };
    map = new DestinationMap(options, true, userId);
    map.populateMarkers();

    google.maps.event.addListener(map.map, 'click', function (event) {
        if (!map.creativeMode) return;
        if (map.newMarker) {
            map.newMarker.setPosition(event.latLng);
        } else {
            map.newMarker = map.placeMarker(event.latLng, null);
        }
        $('#latitude').val(event.latLng.lat);
        $('#longitude').val(event.latLng.lng);
        toggled = false;
        toggleDestinationForm();
    });

}

/**
 * Add destination to database
 * @param {string} url - API URI to add destination
 * @param {string} redirect - URI of redirect page
 */
function addDestination(url, redirect, userId) {
    // Read data from destination form
    const formData = new FormData(
        document.getElementById("addDestinationForm"));

    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});

    // Convert lat and long to double values, and id to int
    data.latitude = parseFloat(data.latitude);
    data.longitude = parseFloat(data.longitude);
    data.countryId = parseInt(data.countryId);
    data.user = {
        id: userId
    };

    // Convert country id to country object
    data.country = {"id": data.countryId};

    //Create response handler
    const handler = function (status, json) {
        if (status !== 200) {
            if (json === "Duplicate destination") {
                toast("Destination could not be created!",
                    "The destination already exists.", "danger", 5000);
                $('#createDestinationModal').modal('hide');
                resetDestinationModal();
            } else {
                showErrors(json);
            }
        } else {
            toast("Destination Created!",
                "The new destination will be added.",
                "success");
            $('#createDestinationModal').modal('hide');
            resetDestinationModal();

            // Add row to table
            const destination = destinationRouter.controllers.frontend.DestinationController.detailedDestinationIndex(
                json).url;
            const name = data.name;
            const type = data.destType;
            const district = data.district;
            const latitude = data.latitude;
            const longitude = data.longitude;
            let country = data.country.id;

            // Set country name
            let countries = document.getElementById(
                "countryDropDown").getElementsByTagName("option");
            for (let i = 0; i < countries.length; i++) {
                if (parseInt(countries[i].value) === data.country.id) {
                    country = countries[i].innerText;
                    break;
                }
            }

            //TODO:refresh cards
            toggled = true;
            toggleDestinationForm();
            this.map.populateMarkers();
            this.map.removeNewMarker();

        }
    }.bind({userId, data, map});
    const inverseHandler = function (status, json) {
        if (status === 200) {
            //TODO:refresh cards
            map.populateMarkers();
        }
    }.bind({map});
    // Post json data to given url
    addNonExistingCountries([data.country]).then(result => {
        const reqData = new ReqData(requestTypes['CREATE'], url, handler, data);
        undoRedo.sendAndAppend(reqData, inverseHandler);
    });
}

/**
 * Clears all fields and error labels in the create destination modal form
 */
function resetDestinationModal() {
    document.getElementById("addDestinationForm").reset();
    hideErrors("addDestinationForm");
}

/**
 * The latitude field listener. Enforces -90 < latitude < 90
 * Moves the marker on the map when the latitude changes
 */
$('#latitude').on('input', () => {
    if ($('#latitude').val() > 90) {
        $('#latitude').val('90');
    }
    if ($('#latitude').val() < -90) {
        $('#latitude').val('-90');
    }

    map.setNewMarker($('#latitude').val(), null);
});

/**
 * The longitude field listener. Enforces -90 < longitude < 90
 * Moves the marker on the map when the longitude changes
 */
$('#longitude').on('input', () => {
    if ($('#longitude').val() > 180) {
        $('#longitude').val('180');
    }
    if ($('#longitude').val() < -180) {
        $('#longitude').val('-180');
    }

    map.setNewMarker(null, $('#longitude').val());
});

/**
 * Opens and closes the create new destination form.
 */
function toggleDestinationForm() {
    toggled = toggled || false;
    if (toggled) {
        $("#mainSection").attr('class', 'col-md-12');
        $("#createDestinationPopOut").attr('class',
            'col-md-0 hideCreateDestinationPopOut');
        $("#arrow").attr('class', "fas fa-1x fa-arrow-left");
        toggled = false;
    } else {
        $("#mainSection").attr('class', 'col-md-9');
        $("#createDestinationPopOut").attr('class',
            'col-md-3 showCreateDestinationPopOut');
        $("#arrow").attr('class', "fas fa-1x fa-arrow-right");
        toggled = true;
    }
}

/**
 * On click listener for the create destinations cancel button
 */
$('#CreateDestinationCancelButton').click(function () {
    resetDestinationModal();
    map.removeNewMarker();
});

/**
 * Destination button on click
 */
$('#createNewDestinationButton').click(function () {
    getUserId().then(userId =>
        addDestination(
            destinationRouter.controllers.backend.DestinationController.addNewDestination().url,
            "/", userId)
    );
});
