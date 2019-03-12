// Runs get countries method, then add country options to drop down
function fillCountryDropDown(getCountriesUrl) {
    // Run a get request to fetch all destinations
    get(url)
    // Get the response of the request
        .then(response => {
            // Convert the response to json
            response.json().then(data => {
                // Json data is an array of destinations, iterate through it
                for(let i = 0; i < data.length; i++) {
                    // For each destination, make a list element that is the json string of object
                    let item = document.createElement("OPTION");
                    item.innerHTML = data[i]['name'];
                    item.value = data[i]['id'];
                    // Add list element to list
                    document.getElementById("countryDropDown").appendChild(item);
                }
            });
        });
}

function addDestination(url, redirect) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("addDestinationForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
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
            }
        });
    });
}

function get(url) {
    return fetch(url, {
        method: "GET"
    })
}