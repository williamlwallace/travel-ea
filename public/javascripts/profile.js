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

/**
 * The JavaScript function to process a client updating there profile
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function updateProfile(url, redirect) {
    console.log("zza");
    // Read data from destination form
    const formData = new FormData(document.getElementById("updateProfileForm"));
    console.log("zza");
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    console.log("zza");
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
    console.log("zza");
    // Post json data to given url
    console.log(data);
    put(url,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
            .then(json => {
            if (response.status != 200) {
        showErrors(json);
    } else {
        hideErrors("updateProfileForm");
        let element = document.getElementById("SuccessMessage");
        element.innerHTML = "Successfully Updated!";
        return sleep(3000);
    }
})
.then(() => {
        let element = document.getElementById("SuccessMessage");
    element.innerHTML = "";
})
});
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}
/**
 * The javascript method to populate the slect boxes on the edit profile scene
 * @param url the route/url to send the request to to get the profile data
 */
function populateProfileData(url) {
    get(url)
        .then(response => {
        // Read response from server, which will be a json object
        return response.json()
    })
.then(json => {
        // Done this way because otherwise the json obbject is formatted really weirdly and you cant access stuff
        for (i = 0; i < json.nationalities.length; i++) {
        // iterates through the list of nationalities and adds them to the dropdown via their id
        $('#nationalities').picker('set', json.nationalities[i].id);
    }
    for (i = 0; i < json.passports.length; i++) {
        $('#passports').picker('set', json.passports[i].id);
    }
    for (i = 0; i < json.travellerTypes.length; i++) {
        $('#travellerTypes').picker('set', json.travellerTypes[i].id);
    }
    $('#gender').picker('set', json.gender);
});
}
