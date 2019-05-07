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
 * @param {*} uri - API logout URI
 * @param {*} redirect - Succeful logout redirect URI
 */
function logout(uri, redirect) {
    // deleteCookie("JWT-Auth");
    post(uri,"")
    .then(response => {
        window.location.href = redirect;
    });
}