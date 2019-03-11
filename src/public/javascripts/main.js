function login(url, redirect) {
    const formData = new FormData(document.getElementById("login"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    post(url,data)
    .then(response => {
        //need access to response status, so cant return promise
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

/**
 * The JavaScript function to process a client signing up
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function signUp(url, redirect) {
    const formData = new FormData(document.getElementById("signUp"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]] : pair[1],
    }), {});
    post(url, data)
        .then(response => {
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

function createProfile(url, redirect) {
    const formData = new FormData(document.getElementById("profile"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    post(url,data)
    .then(response => {
        //need access to response status, so cant return promise
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

function getProfile(url) {
    // Run a get request to fetch profile
    get(url)
    .then(response => {
        response.json()
        .then(data => {
            if (response.status != 200) showErrors(data);
            else insertFieldData(data);
        });
    });
}

function updateProfile(url) {
    const formData = new FormData(document.getElementById("profile"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    post(url,data)
    .then(response => {
        //need access to response status, so cant return promise
        response.json()
        .then(json => {
            if (response.status != 200) showErrors(json);
        });
    });
}

function getAllDestinations(url) {
    // Run a get request to fetch all destinations
    get(url)
        // Get the response of the request
        .then(response => {
            // Convert the response to json
            response.json().then(data => {
                // Json data is an array of destinations, iterate through it
                for(var i = 0; i < data.length; i++) {
                    // For each destination, make a list element that is the json string of object
                    let item = document.createElement("LI");
                    item.appendChild(document.createTextNode(JSON.stringify(data[i], null, 1)));
                    // Add list element to list
                    document.getElementById("destinationList").appendChild(item);
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

function post(url, data) {
    return fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
}

function showErrors(json) {
    console.log("yooo");
    const elements = document.getElementsByTagName("pre");
    for (i in elements) {
        elements[i].innerHTML = "";
    }
    for (const key of Object.keys(json)) {
        document.getElementById(key + "Error").innerHTML = json[key];
    }
}

//insert
function insertFieldData(json) {
    for (const key of Object.keys(json)) {
        if (key == "gender") {
            document.getElementById(json[key]).checked = true;
        } else {
            const elements = document.getElementsByName(key);
            for (element in elements) {
                elements[element].value = json[key];
            }
        }
    }
}