var countryDict = {};
var travellerTypeDict = {};

// Capitalizes the first letter of a string
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

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
        travellerTypeDict[data[i]['id']] = capitalizeFirstLetter(data[i]['description']);
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
        document.getElementById("nationality").appendChild(item);
    }
}


function fillTravellerDropDown() {
    for(let key in travellerTypeDict) {
        // For each Traveller type, make a list element that is the json string of object
        let item = document.createElement("OPTION");
        item.innerHTML = travellerTypeDict[key];
        item.value = key;
        // Add list element to drop down list
        document.getElementById("travellerType").appendChild(item);
    }
    // implements the plug in multi selector
    $('#travellerTypes').picker();
}


$(document).ready(function () {
    //Initialises the data table and adds the filter button to the right of the search field
    $('#dtPeople').DataTable( {
        dom: "<'row'<'col-sm-12 col-md-2'l><'col-sm-12 col-md-9'bf><'col-sm-12 col-md-1'B>>" +
            "<'row'<'col-sm-12'tr>>" +
            "<'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",
        buttons: [
            {
                text: 'Filter',
                action: function ( e, dt, node, config ) {
                    $('#modalContactForm').modal('toggle');
                }
            }
        ]
    } );
});

function searchParams(){
    var nationality = document.getElementById('nationality').value;
    var gender = document.getElementById('gender').value;
    var minAge = document.getElementById('minAge').value;
    var maxAge = document.getElementById('maxAge').value;
    var travellerType = document.getElementById('travellerType').value;
    var url = '/people?';
    if (nationality) {
        url += "nationalityId=" + nationality + "&";
    }
    if (gender) {
        url += "gender=" + gender + "&";
    }
    if (minAge) {
        url += "minAge=" + minAge + "&";
    }
    if (maxAge) {
        url += "maxAge=" + maxAge + "&";
    }

    if (travellerType) {
        url += "travellerTypeId=" + travellerType + "&";
    }
    url = url.slice(0, -1);
    return url;
}

function apply(){
    var url;
    url = searchParams();
    window.location = url;

}