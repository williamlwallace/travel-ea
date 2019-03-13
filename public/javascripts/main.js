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

function showErrors(json) {
    console.log("yooo");
    const elements = document.getElementsByTagName("pre");
    for (i in elements) {
        elements[i].innerHTML = "";
    }
    for (const key of Object.keys(json)) {
        document.getElementById(key + "Error").innerHTML = json[key];
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