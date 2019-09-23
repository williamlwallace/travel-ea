/**
 * return promise with value of user id from cookie or api else redirect
 */
function getUserId() {
    const CNAME = "User-ID";
    let value = getCookie(CNAME);
    if (value !== "") {
        return Promise.resolve(value);
    }
    return get(userRouter.controllers.backend.UserController.setId().url)
    .then(response => {
        //need access to response status, so cant return promise
        return response.json()
        .then(json => {
            if (response.status !== 200) {
                window.location.href = '/'
            } else {
                return json;
            }
        });
    });
}

/**
 * Gets a user from the API.
 *
 * @param {Number} userId The id of the user to retrieve
 * @returns {Object} A promise containing either the user as JSON or null if
 * the requesting user does not have permission to view the requested
 * information
 */
function getUser(userId) {
    return get(
        userRouter.controllers.backend.UserController.getUser(userId).url).then(
        userResponse => {
            if (userResponse.status === 200) {
                return userResponse.json().then(userJson => {
                    return userJson;
                });
            } else {
                return null;
            }
        });
}

/**
 * return boolean of if the user is an admin
 */
function isUserAdmin() {
    const CNAME = "Is-Admin";
    let value = getCookie(CNAME);
    if (value !== "") {
        return value.toLowerCase() === "true";
    } else {
        return false;
    }
}

/**
 * Logs out user and redirects to given page
 * @param {string} uri - API logout URI
 * @param {string} redirect - Successful logout redirect URI
 */
function logout(uri, redirect) {
    post(uri, "")
    .then(response => {
        window.location.href = redirect;
    });
}

/**
 * Calculates the age of a user based on there birth date
 * @param {Number} dt1 birth date of user in epoch time
 */
function calc_age(dt1) {
    let diff = (Date.now() - dt1) / 1000;
    diff /= (60 * 60 * 24);
    // Best conversion method without moment etc
    return Math.abs(Math.floor(diff / 365.25));
}

/**
 * Shortens numbers by adding a 'k' on the end to represent thousands
 *
 * @param num is number to format
 * @returns {number} formatted number
 */
function countFormatter(num) {
    return Math.abs(num) > 999 ? Math.sign(num) * ((Math.abs(num)
        / 1000).toFixed(1)) + 'k' : Math.sign(num) * Math.abs(num)
}