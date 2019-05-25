var markers = []; //wont let me use let for some reason?
var destJson = null;

/**
 * Initializes destination table and calls method to populate
 * @param {Number} userId - ID of user to get destinations for
 */
function onPageLoad(userId) {
    const destinationTable = $('#dtDestination').DataTable({
        createdRow: function (row, data, dataIndex) {
            $(row).attr('data-href', data[data.length - 1]);
            $(row).addClass("clickable-row");
        }
    });
    populateDestinations(destinationTable, userId);
    populateMarkers(userId);
}

/**
 * Gets all countries and fills into dropdown
 * @param {string} getCountriesUrl - get all countries URI
 */
function fillCountryInfo(getCountriesUrl) {
    // Run a get request to fetch all destinations
    get(getCountriesUrl)
        .then(response => {
            response.json()
            .then(data => {
                // Json data is an array of destinations, iterate through it
                let countryDict = {};

                for(let i = 0; i < data.length; i++) {
                    // Also add the item to the dictionary
                    countryDict[data[i]['id']] = data[i]['name'];
                }

                // Now fill the drop down box, and list of destinations
                fillDropDown("countryDropDown", countryDict);
            });
        });
}

/**
 * Add destination to database
 * @param {string} url - API URI to add destination
 * @param {string} redirect - URI of redirect page
 */
function addDestination(url, redirect, userId) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("addDestinationForm"));

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
    delete data.countryId;

    // Post json data to given url
    post(url, data)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showErrors(json);
            } else {
                toast("Destination Created!", "The new destination will be added to the table.", "success");
                $('#createDestinationModal').modal('hide');

                // Add row to table
                let table = $('#dtDestination').DataTable();
                const destination = destinationRouter.controllers.frontend.DestinationController.detailedDestinationIndex(json).url;
                const name = data.name;
                const type = data._type;
                const district = data.district;
                const latitude = data.latitude;
                const longitude = data.longitude;
                let country = data.country.id;

                // Set country name
                let countries = document.getElementById("countryDropDown").getElementsByTagName("option");
                for (let i = 0; i < countries.length; i++) {
                    if (parseInt(countries[i].value) === data.country.id) {
                        country = countries[i].innerText;
                        break;
                    }
                }

                table.row.add([name, type, district, latitude, longitude, country, destination]).draw(false);
            }
        });
    });
}

/**
 * Insert destination data into table
 * @param {Object} table - data table object
 * @param {Number} userId - ID of user to retrieve destinations for
 */
function populateDestinations(table, userId) {
    // Query API endpoint to get all destinations
    table.clear();
    get(destinationRouter.controllers.backend.DestinationController.getAllDestinations(userId).url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById("otherError").innerHTML = json;
            } else {
                // Populates table
                for (const dest in json) {
                    const destination = destinationRouter.controllers.frontend.DestinationController.detailedDestinationIndex(json[dest].id).url;
                    const name = json[dest].name;
                    const type = json[dest]._type;
                    const district = json[dest].district;
                    const latitude = json[dest].latitude;
                    const longitude = json[dest].longitude;
                    const country = json[dest].country.name;

                    table.row.add([name, type, district, latitude, longitude, country, destination]).draw(false);
                }
            }
        });
    })
}

function populateMarkers(userId) {
    get(destinationRouter.controllers.backend.DestinationController.getAllDestinations(userId).url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById("otherError").innerHTML = json;
            } else {
                // Populates table
                for (const dest in json) {
                    // console.log(json[dest].name);
                }
            }
        });
    })
}

/**
 * Initialises google maps
 */
function initMap() {
    // Map options
    let options = {
        zoom: 5,
        center: {lat:-40.9006, lng:174.8860}
    }

    // New map
    let map = new google.maps.Map(document.getElementById('map'), options);

    // Add Marker Function
    function addMarker(props){
        let marker = new google.maps.Marker({
            position:props.coords,
            map:map
        });

        // Check for customicon
        if(props.iconImage){
            // Set icon image
            marker.setIcon(props.iconImage);
        }

        // Check content
        if(props.content){
            let infoWindow = new google.maps.InfoWindow({
                content:props.content
            });

            marker.addListener('click', function(){
                infoWindow.open(map, marker);
            });
        }
    }
    // addMarker({
    //     coords:{lat:42.4668,lng:-70.9495},
    //     iconImage:'https://developers.google.com/maps/documentation/javascript/examples/full/images/beachflag.png',
    //     content:'<h1>Lynn MA</h1>'
    // })


}

/**
 * Redirect to the destinations details page when row is clicked.
 */
$('#dtDestination').on('click', 'tbody tr', function() {
    window.location = this.dataset.href;
});
