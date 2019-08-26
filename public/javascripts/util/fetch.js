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
 *
 * @param uri URI to send request to
 * @param formdata The already created formdata object to upload
 * @param progressHandler Function that takes one parameter, the event data given to it
 * @param responseHandler Function that deals with the response, (status, response)
 */
function postMultipartWithProgress(uri, formdata, progressHandler, endUploadHandler, responseHandler) {
    let xhr = new XMLHttpRequest();
    xhr.upload.onprogress = function(evt) {;
        progressHandler(evt);
    };

    xhr.upload.onloadend = endUploadHandler;

    xhr.open("POST", uri);

    xhr.onload = function() {
        responseHandler(xhr.status, xhr.response)
    };

    xhr.send(formdata);
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
    } catch (e) {
    }
}
;
