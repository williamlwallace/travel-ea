/**
 * Sends GET Request to API
 * @param {string} uri - API URI 
 */
function get(uri) {
    return fetch(uri, {
        method: "GET"
    })
}

/**
 * Sends POST request to API
 * @param {string} uri - API URI 
 * @param {JSON} data - JSON request body
 */
function post(uri, data) {
    return fetch(uri, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
}

/**
 * Sends PUT request to API
 * @param {string} uri - API URI 
 * @param {JSON} data - JSON request body
 */
function put(uri, data) {
    return fetch(uri, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
}

/**
 * Sends DELETE request to API
 * @param {string} uri - API URI 
 */
function _delete(uri) {
    return fetch(uri, {
        method: "DELETE"
    })
}