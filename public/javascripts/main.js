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

function showErrors(json, formName="main") {
    const elements = document.getElementById(formName).getElementsByTagName("label");
    for (i in elements) {
        elements[i].innerHTML = "";
        for (const key of Object.keys(json)) {
            if (elements[i].id == (key+"Error")) {
                elements[i].innerHTML = json[key];
                break;
            }
        }
    }
}

function hideErrors(formName) {
    const elements = document.getElementById(formName).getElementsByTagName("label");
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