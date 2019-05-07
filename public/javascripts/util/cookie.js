/**
 * Gets cookies value from name else empty string
 * @param {string} cname - Cookie name 
 */
function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
    return "";
}

/**
 * Checks if cookie exists
 * @param {stirng} cname - Cookie name
 */
function checkCookie(cname) {
    let cvalue = getCookie(cname);
    if (cvalue != "") {
      return true;
    } else {
      return false;
    }
}