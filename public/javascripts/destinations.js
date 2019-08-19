let activeInfoWindow;
let map;
let toggled;
let requestOrder = 0;
let lastRecievedRequestOrder = -1;
let paginationHelper;

/**
 * Initializes destination page and map
 * @param {Number} userId - ID of user to get destinations for
 */
function onPageLoad(userId) {
    paginationHelper = new PaginationHelper(1, 1, "destinationPagination", getDestinations);
    const options = {
        zoom: 1.8,
        center: {lat: 2, lng: 2}
    };
    map = new DestinationMap(options, true, userId);
    getDestinations().then((dests) => {
        map.populateMarkers(dests);
        createDestinationCards(dests);
    }).then(() => {
        map.addDestinations()
    });

    google.maps.event.addListener(map.map, 'click', function (event) {
        if (this.newMarker) {
            this.newMarker.setPosition(event.latLng);
        } else {
            this.newMarker = this.placeMarker(event.latLng, null);
        }
        $('#latitude').val(event.latLng.lat);
        $('#longitude').val(event.latLng.lng);
        toggled = false;
        toggleDestinationForm();
    }.bind(map));

    toggleDestinationForm();

}

/**
 * //TODO
 * @returns {*}
 */
function getOnlyGetMine() {
    return $('#onlyGetMine').val();
}

/**
 * //TODO
 * @returns {*}
 */
function getSearchQuery() {
    return $('#searchQuery').val();
}

/**
 * Get the value of the number of results to show per page
 * @returns {number} The number of results shown per page
 */
function getPageSize() {
    return $('#pageSize').val();
}

/**
 * Returns the name of the db column to search by
 * @returns {string} Name of db column to search by
 */
function getSortBy() {
    return $('#sortBy').val();
}

/**
 * Gets whether or not to sort by ascending
 * @returns {string} Either 'true' or 'false', where true is ascending, false is descending
 */
function getAscending() {
    return $('#ascending').val();
}

//TODO: Doc
function getDestinations() {
    const url = new URL(destinationRouter.controllers.backend.DestinationController.getPagedDestinations().url, window.location.origin);

    // Append non-list params
    if(getSearchQuery() !== "") { url.searchParams.append("searchQuery", getSearchQuery()); }
    if(getOnlyGetMine() !== "") { url.searchParams.append("onlyGetMine", getOnlyGetMine()); }

    // Append pagination params
    url.searchParams.append("pageNum", paginationHelper.getCurrentPageNumber());
    url.searchParams.append("pageSize", getPageSize().toString());
    url.searchParams.append("sortBy", getSortBy());
    url.searchParams.append("ascending", getAscending());
    url.searchParams.append("requestOrder", requestOrder++);

    return get(url)
        .then(response => {
            return response.json()
                .then(json => {
                    if (response.status !== 200) {
                        toast("Error with destinations", "Cannot load destinations", "danger")
                    } else {
                        if(lastRecievedRequestOrder < json.requestOrder) {
                            lastRecievedRequestOrder = json.requestOrder;
                            paginationHelper.setTotalNumberOfPages(json.totalNumberPages);
                            return json;
                        }
                    }
                });
        });
}

//TODO: Doc
function createDestinationCards(dests) {
    dests.data.forEach((dest) => {
        const template = $("#destinationCardTemplate").get(0);
        const clone = template.content.cloneNode(true);
        let tags = "";
        let travellerTypes = "";

        $(clone).find("#card-header").append(dest.name);
        //TODO: need destination primary photo $(clone).find("#card-thumbnail").attr("src",);
        $(clone).find("#district").append("District: " + dest.district);
        $(clone).find("#country").append("Country: " + dest.country.name);
        $(clone).find("#destType").append("Type: " + dest.destType);
        $(clone).find("#card-header").attr("data-id", dest.id.toString());

        dest.tags.forEach(item => {
            tags += item.name + ", ";
        });
        tags = tags.slice(0, -2);

        dest.travellerTypes.forEach(item => {
            travellerTypes += item.description + ", ";
        });
        travellerTypes = travellerTypes.slice(0, -2);

        $(clone).find("#travellerTypes").append("Traveller Types: " + travellerTypes);
        $(clone).find("#tags").append("Tags: " + tags);

        $("#destinationCardList").get(0).appendChild(clone);
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
        $("#mainSection").attr('class', 'col-md-8');
        $("#createDestinationPopOut").attr('class',
            'col-md-4 showCreateDestinationPopOut');
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
