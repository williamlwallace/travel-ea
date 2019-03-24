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

function showErrors(json, parentElement="main") {
    const elements = document.getElementById(parentElement).getElementsByTagName("label");
    for (i in elements) {
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
    for (i in elements) {
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

function setCookie(name, value) { //default expiry is 1 day in future
    let date = new Date()
    date.setTime(date.getTime()+(60*1000*60*24))
    document.cookie = name + '=' + value + "; expires=" + date.toUTCString() + ";path=/";
}

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
    if (cvalue != "") {
      return true;
    } else {
      return false;
    }
}

function deleteCookie(cname) {
    let date = new Date()
    date.setTime(date.getTime()-(60*1000*60*24)) //set date to past
    document.cookie = cname + "=; expires=" + date.toUTCString() + ";path=/";
}

function logout(url, redirect) {
    deleteCookie("JWT-Auth");
    post(url,"")
    .then(response => {
        window.location.href = redirect;
    });
}