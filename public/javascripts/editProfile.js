/* Display gender drop down the same as the others */
$('#gender').picker();

/**
 * The JavaScript function to process a client updating there profile
 * @param uri The route/uri to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function updateProfile(uri, redirect) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("updateProfileForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
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
    // Post json data to given uri
    put(uri,data)
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

/**
 * Returns timout promise
 * @param {Number} ms - time in millieseconds
 */
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * The javascript method to populate the slect boxes on the edit profile scene
 * @param {string} uri - the route/URI to send the request to to get the profile data
 */
function populateProfileData(uri) {
    get(uri)
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