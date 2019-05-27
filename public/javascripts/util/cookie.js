/**
 * Gets cookies value from name else empty string
 * @param {string} cname - Cookie name
 */
function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

/**
 * Checks if cookie exists
 * @param {string} cname - Cookie name
 */
function checkCookie(cname) {
    return getCookie(cname) !== "";
}
