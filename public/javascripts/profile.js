var countryDict = {};
var travellerTypeDict = {};

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
/* Automatically display profile form when signing up */
$('#createProfileForm').modal('show');


/**
 * The JavaScript function to process a client signing up
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function signUp(id, url, redirect) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("signUp"));
    formData.append("userId", id);
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]] : pair[1],
    }), {});

    // Convert nationalities, passports and Traveller Types to Correct JSON appropriate format
    data.nationalities = [];
    let nat_ids = $.map($(document.getElementById("nationalities")).picker('get'),Number);
    for (i = 0; i < nat_ids.length; i++) {
        let nat = {};
        nat.id = nat_ids[i];
        data.nationalities.push(nat);
    }
    data.passports = [];
    let passport_ids = $.map($(document.getElementById("passports")).picker('get'),Number)
    for (i = 0; i < passport_ids.length; i++) {
        let passport = {};
        passport.id = passport_ids[i];
        data.passports.push(passport);
    }
    data.travellerTypes  = [];
    let type_ids = $.map($(document.getElementById("travellerTypes")).picker('get'),Number);
    for (i = 0; i < type_ids.length; i++) {
        let type = {};
        type.id = type_ids[i];
        data.travellerTypes.push(type);
    }
    // Post json data to given url
    post(url, data)
    .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
            console.log(json)
            if (response.status != 200) {
                showErrors(json);
            } else {
                window.location.href = redirect;
            }
        });
    });
}