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

function postMultipart(url, formData) {
    return fetch(url, {
        method: 'POST',
        body: formData
    })
}

function put(url, data) {
    return fetch(url, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
}

function _delete(url) {
    return fetch(url, {
        method: "DELETE"
    })
}

function showErrors(json, parentElement="main") {
    const elements = document.getElementById(parentElement).getElementsByTagName("label");
    for (let i in elements) {
        elements[i].innerHTML = "";
        for (const key of Object.keys(json)) {
            if (elements[i].id == (key+"Error")) {
                const data = json[key]
                if (data.startsWith("_")) elements[i].innerHTML = data.slice(1);
                else elements[i].innerHTML = data;
                break;
            }
        }
    }
}

function hideErrors(parentElement) {
    const elements = document.getElementById(parentElement).getElementsByTagName("label");
    for (let i in elements) {
        elements[i].innerHTML = "";
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

// return promise with value of userid from cookie or api else redirect
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

// function setCookie(name, value) { //default expiry is 1 day in future
//     let date = new Date()
//     date.setTime(date.getTime()+(60*1000*60*24))
//     document.cookie = name + '=' + value + "; expires=" + date.toUTCString() + ";path=/";
// }

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

function checkCookie(cname) {
    let cvalue = getCookie(cname);
    return cvalue != "";
}

// function deleteCookie(cname) {
//     let date = new Date()
//     date.setTime(date.getTime()-(60*1000*60*24)) //set date to past
//     document.cookie = cname + "=; expires=" + date.toUTCString() + ";path=/";
// }

function logout(url, redirect) {
    // deleteCookie("JWT-Auth");
    post(url,"")
    .then(response => {
        window.location.href = redirect;
    });
}