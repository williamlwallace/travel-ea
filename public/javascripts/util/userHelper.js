/**
 * return promise with value of user id from cookie or api else redirect
 */
function getUserId() {
    const CNAME = "User-ID";
    let value = getCookie(CNAME);
    if (value !== "") {
        return Promise.resolve(value);
    }
    return get('api/user/setid')
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
 * Checks the logged in user has permission to view/edit object data with owner userId.
 * @param userId ID of owner of object
 * @returns {boolean} True if user has permission to view/edit, otherwise false
 */
function hasPermission(userId) {
    return get(userRouter.controllers.backend.UserController.hasPermission(userId).url)
    .then(response => {
        //need access to response status, so cant return promise
        return response.json()
        .then(json => {
            if (response.status === 200) {
                return json.hasPermission;
            } else {
                return false;
            }
        });
    });
}
