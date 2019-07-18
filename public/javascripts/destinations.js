let table;

/**
 * Initializes destination table and calls method to populate
 * @param {Number} userId - ID of user to get destinations for
 */
function onPageLoad(userId) {
    const destinationGetURL = destinationRouter.controllers.backend.DestinationController.getAllDestinations(userId).url;
    const tableModal = {
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-href', data[data.length - 1]);
            $(row).addClass("clickable-row");
        }
    }
    table = new EATable('dtDestination', tableModal, destinationGetURL, populateDestinations, (json) => {document.getElementById("otherError").innerHTML = json;})
    populateMarkers(userId);
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

    // Post json data to given url
    addNonExistingCountries([data.country]).then(result => {
        post(url, data)
        .then(response => {
            // Read response from server, which will be a json object
            response.json()
            .then(json => {
                if (response.status !== 200) {
                    if (json === "Duplicate destination") {
                        toast("Destination could not be created!",
                            "The destination already exists.", "danger", 5000);
                        $('#createDestinationModal').modal('hide');
                    } else {
                        showErrors(json);
                    }
                } else {
                    toast("Destination Created!",
                        "The new destination will be added to the table.",
                        "success");
                    $('#createDestinationModal').modal('hide');

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

                    table.add(
                        [name, type, district, latitude, longitude, country,
                            destination]);
                    populateMarkers(userId);

                }
            });
        });
    });
}

/**
 * Insert destination data into table
 * @param {Object} json Json object containing destination data
 */
function populateDestinations(json) {
    const rows = []
    for (const dest in json) {
        const destination = destinationRouter.controllers.frontend.DestinationController.detailedDestinationIndex(
            json[dest].id).url;
        const name = json[dest].name;
        const type = json[dest].destType;
        const district = json[dest].district;
        const latitude = json[dest].latitude;
        const longitude = json[dest].longitude;
        const country = json[dest].country.name;

        rows.push(
            [name, type, district, latitude, longitude, country,
                destination]);
    }
    return rows;
}

// Maps marker list
let markers = [];

/**
 * Populates the markers list with props which can be iterated over to dynamically add destination markers
 * @param {Number} userId - ID of user to retrieve destinations for
 */
function populateMarkers(userId) {
    get(destinationRouter.controllers.backend.DestinationController.getAllDestinations(
        userId).url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById("otherError").innerHTML = json;
            } else {
                for (const dest in json) {
                    //Link to detailed destination in info window
                    const destination = destinationRouter.controllers.frontend.DestinationController.detailedDestinationIndex(
                        json[dest].id).url;

                    //Setting public and privacy icon in info window
                    let privacySrc;
                    json[dest].isPublic
                        ? privacySrc = "/assets/images/public.png"
                        : privacySrc = "/assets/images/private.png";

                    //Setting public and private marker images, resized
                    const marker = {
                        url: 'https://image.flaticon.com/icons/svg/149/149060.svg',
                        scaledSize: new google.maps.Size(30, 30)
                    };
                    const markerPrivate = {
                        url: 'https://image.flaticon.com/icons/svg/139/139012.svg',
                        scaledSize: new google.maps.Size(30, 30)
                    };

                    markers.push({
                        coords: {
                            lat: json[dest].latitude,
                            lng: json[dest].longitude
                        },
                        iconImage: json[dest].isPublic ? marker : markerPrivate,
                        content: '<a class="marker-link" title="View detailed destination" href="'
                            + destination + '"><h3 style="display:inline">'
                            + json[dest].name
                            + '</h3></a>&nbsp;&nbsp;&nbsp;<img src="'
                            + privacySrc
                            + '"height="20" style="margin-bottom:13px">'
                            + '<p><b>Type:</b> ' + json[dest].destType + '<br>'
                            + '<b>District:</b> ' + json[dest].district + '<br>'
                            + '<b>Latitude:</b> ' + json[dest].latitude + '<br>'
                            + '<b>Longitude:</b> ' + json[dest].longitude
                            + '<br>'
                            + '<b>Country:</b> ' + json[dest].country.name
                            + '</p>'
                    });
                }
                initMap();
            }
        });
    })
}

/**
 * Initialises google maps on destination page and dynamically adds destination markers
 */
function initMap() {
    // Initial map options
    let options = {
        zoom: 1.8,
        center: {lat: 2.0, lng: 2.0}
    };
    // New map
    let map = new google.maps.Map(document.getElementById('map'), options);

    /**
     * Inserts marker on map
     * @param {JSON} props contain destination coords, destination information, and styling
     */
    function addMarker(props) {
        let marker = new google.maps.Marker({
            position: props.coords,
            map: map
        });
        // Check for customicon
        if (props.iconImage) {
            // Set icon image
            marker.setIcon(props.iconImage);
        }
        // Check content
        if (props.content) {
            let infoWindow = new google.maps.InfoWindow({
                content: props.content
            });
            // if content exists then make a info window
            marker.addListener('click', function () {
                infoWindow.open(map, marker);
            });
        }
    }

    // Loop through markers list and add them to the map
    for (let i = 0; i < markers.length; i++) {
        addMarker(markers[i]);
    }
}

/**
 * Redirect to the destinations details page when row is clicked.
 */
$('#dtDestination').on('click', 'tbody tr', function () {
    window.location = this.dataset.href;
});
