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
 * Sends POST request to API with multipart data and no headers
 * @param {string} uri - API URI
 * @param {JSON} data - multipart request body
 */
function postMultipart(uri, data) {
    return fetch(uri, {
        method: "POST",
        body: data
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
 * Sends PUT request to API
 * @param {string} uri - API URI
 * @param {JSON} data - JSON request body
 */
function patch(uri, data) {
    return fetch(uri, {
        method: "PATCH",
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

//export for testing only
//this will only be imported if run by node
if (typeof module !== 'undefined' && module.exports) {
    var fetch = require('node-fetch');
    try {
        module.exports = {
            put,
            post,
            _delete
        };
    } catch (e) {}
};
