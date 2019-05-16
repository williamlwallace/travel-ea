//initilise datatable on load
$(document).ready(function () {
    populateDestinations(($('#dtDestination').DataTable()));
});

/**
 * Gets all countries and fills into dropdown
 * @param {stirng} getCountriesUrl - get all countries URI
 */
function fillCountryInfo(getCountriesUrl) {
    // Run a get request to fetch all destinations
    get(getCountriesUrl)
    // Get the response of the request
        .then(response => {
            // Convert the response to json
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
 * Add destination to databse
 * @param {stirng} url - API URI to add destination
 * @param {string} redirect - URI of redirect page
 */
function addDestination(url, redirect) {
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

    // Convert country id to country object
    data.country = {"id": data.countryId};
    delete data.countryId;
    // Post json data to given url
    post(url,data)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            if (response.status != 200) {
                showErrors(json);
            } else {
                window.location.href = redirect;
                location.reload(); // When we load destination data using js, we can change this to just reload the data and repopulate tables
            }
        });
    });
}

/**
 * Insert destination data into table
 * @param {Object} table - data table object
 */
function populateDestinations(table, userId) {
    //Query api to get all destinations
    console.log(destinationRouter.controllers.backend.DestinationController.getAllDestinations(userId).url);
    get(destinationRouter.controllers.backend.DestinationController.getAllDestinations().url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status != 200) {
                document.getElementById("otherError").innerHTML = json;
            } else {
                //Loop through json and insert into table
                for (const dest in json) {
                    const name = json[dest].name;
                    const type = json[dest]._type;
                    const district = json[dest].district;
                    const latitude = json[dest].latitude;
                    const longitude = json[dest].longitude;
                    const country = json[dest].country.name;
                    table.row.add([name, type, district, latitude, longitude, country]).draw(false);
                }
            }
        });
    })
}