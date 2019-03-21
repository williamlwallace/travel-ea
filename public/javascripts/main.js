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