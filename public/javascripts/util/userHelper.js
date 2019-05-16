/**
 * return promise with value of userid from cookie or api else redirect
 */
function getUserId() {
    const CNAME = "User-ID";
    let value = getCookie(CNAME);
    if (value != "") {
        return Promise.resolve(value);
    }
    return get('api/user/setid')
    .then(response => {
        //need access to response status, so cant return promise
        response.json()
        .then(json => {
            if (response.status != 200) {
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
 * @param {stirng} redirect - Succeful logout redirect URI
 */
function logout(uri, redirect) {
    // deleteCookie("JWT-Auth");
    post(uri,"")
    .then(response => {
        window.location.href = redirect;
    });
}

/**
 * Calculates the age of a user based on there birthdate
 * @param {Number} dt1 birthdate of user in epoch time
 */
function calc_age(dt1) {
    let diff =(Date.now() - dt1) / 1000;
    diff /= (60 * 60 * 24);
    // Best convertion method without moment etc
    return Math.abs(Math.floor(diff/365.25));
}