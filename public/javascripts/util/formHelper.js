/**
 * Displays error messages in appropriate errror labels
 * @param {JSON} json - json error reponse from API 
 * @param {stirng} parentElement - Id of parent element
 */
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

/**
 * Removes all error messages in given element
 * @param {string} parentElement - id of parent element
 */
function hideErrors(parentElement) {
    const elements = document.getElementById(parentElement).getElementsByTagName("label");
    for (let i in elements) {
        elements[i].innerHTML = "";
    }
}

/**
 * Gets data from api and maps ids to given colName in a dictionary
 * @param {string} URI - API URI to get data from
 * @param {string} colName - name of data column
 * @param {Boolean} capatisie - Wheather is capatilise first letter 
 */
function getHardData(URI, colName, capatilise=false) {
    // Run a get request to fetch all destinations
    return get(URI)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        return response.json()
        .then(data => {
            // Json data is an array of destinations, iterate through it
            dict = {};
            for(let i = 0; i < data.length; i++) {
                // Also add the item to the dictionary
                if (capatilise) {
                    dict[data[i]['id']] = capitalizeFirstLetter(data[i][colName]);
                } else {
                    dict[data[i]['id']] = data[i][colName];
                }
            }
            return dict;
        });
    });
}

/**
 * Gets data from API and fills given dropdowns
 * @param {string} URI - API URI to get data from
 * @param {Object} dropdowns - Array of dropdown ids
 * @param {string} colName - name of data column
 * @param {Boolean} capatisie - Wheather is capatilise first letter 
 */
function getAndFillDD(URI, dropdowns, colName, capatilise=false) {
    getHardData(URI, colName, capatilise)
    .then(dict => {
        // Now fill the selects
        dropdowns.forEach(element => {
            fillDropDown(element, dict);
        });
    });
}

/**
 * Gets dropdown element and fills its data
 * @param {string} dropdownName - id of dropdown element 
 * @param {Object} dict - dictionary of data to put in dropdown
 */
function fillDropDown(dropdownName, dict) {
    for(let key in dict) {
        // For each destination, make a list element that is the json string of object
        let item = document.createElement("OPTION");
        item.innerHTML = dict[key];
        item.value = key;
        // Add list element to drop down list
        document.getElementById(dropdownName).appendChild(item);
    }
    // implements the plug in multi selector
    $('#' + dropdownName).picker();
}

/**
 * Sets form fields values
 * @param {JSON} json - Json containing field ids and values 
 */
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

/**
 * Gets list of data from dropdowns to put in JSON object
 * @param {string} dropdown - Name of dropdown Id
 */
function JSONFromDropDowns(dropdown) {
    let data = [];
    let ids = $.map($(document.getElementById(dropdown)).picker('get'),Number);
    for (i = 0; i < ids.length; i++) {
        let dat = {};
        dat.id = ids[i];
        data.push(dat);
    }
    return data;
}

/**
 * Turns array into string whilst mapping the inner object ids to real values
 * @param {Object} array array to translate
 * @param {string} dataName column name
 * @param {string} URL Address to retrive
 */
function arrayToString(array, dataName, URL) {
    return getHardData(URL, dataName)
    .then(dict => {
        let out = "";
        for (const item of array) {
            out += dict[item.id] + ", ";
        }
        //remove extra separator
        out = out.slice(0,out.length-2);
        return out
    });
}