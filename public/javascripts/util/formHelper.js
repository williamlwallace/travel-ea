/**
 * Displays error messages in appropriate error labels
 *
 * @param {JSON} json - json error response from API
 * @param {string} parentElement - Id of parent element
 */
function showErrors(json, parentElement = "main") {
    const elements = document.getElementById(
        parentElement).getElementsByTagName("label");
    for (let i in elements) {
        elements[i].innerHTML = "";
        for (const key of Object.keys(json)) {
            if (elements[i].id === (key + "Error")) {
                const data = json[key];
                if (data.startsWith("_")) {
                    elements[i].innerHTML = data.slice(1);
                } else if (data.endsWith("0")) {
                    const tempNum = data.split(".");
                    elements[i].innerHTML = tempNum[0];
                } else {
                    elements[i].innerHTML = data;
                }
                break;
            }
        }
    }
}

/**
 * Removes all error messages in given element
 *
 * @param {string} parentElement - id of parent element
 */
function hideErrors(parentElement) {
    const elements = document.getElementById(
        parentElement).getElementsByTagName("label");
    for (let i in elements) {
        elements[i].innerHTML = "";
    }
}

/**
 * Gets data from api and maps ids to given colName in a dictionary
 *
 * @param {string} URI - API URI to get data from
 * @param {string} colName - name of data column
 * @param {Boolean} capitalise - Whether to capitalised first letter
 * @param {Boolean} country - True if getting data for country from external API
 */
function getHardData(URI, colName, country = false, capitalise = false) {
    // Run a get request to fetch all destinations
    return get(URI)
    // Get the response of the request
    .then(response => {
        // Convert the response to json
        return response.json()
        .then(data => {
            // Json data is an array of destinations, iterate through it
            if (country) {
                addIdToJSON(data);
            }
            let dict = {};
            for (let i = 0; i < data.length; i++) {
                // Also add the item to the dictionary
                if (capitalise) {
                    dict[data[i]['id']] = capitalizeFirstLetter(
                        data[i][colName]);
                } else {
                    dict[data[i]['id']] = data[i][colName];
                }
            }
            console.log(dict);
            return dict;
        });
    });
}

/**
 * Gets data from API and fills given drop downs
 *
 * @param {string} URI - API URI to get data from
 * @param {Object} dropdowns - Array of dropdown ids
 * @param {string} colName - name of data column
 * @param {Boolean} capitalise - Wheather is capatilise first letter
 */
function getAndFillDD(URI, dropdowns, colName, capitalise = false) {
    getHardData(URI, colName, capitalise)
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
    for (let key in dict) {
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
        if (key === "gender") {
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
    let ids = $.map($(document.getElementById(dropdown)).picker('get'), Number);
    for (let i = 0; i < ids.length; i++) {
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
        out = out.slice(0, out.length - 2);
        return out
    });
}

/**
 * Adds a toast to the bottom right of the screen. This toast will display for 2 seconds by default.
 * @param {string} title A string of the toast title
 * @param {string} message a string of the inner message of the toast
 * @param {string} type a string of the type of toast to display
 * @param {number} delay an int of a custom delay in milliseconds before the toast disappears
 */
function toast(title, message, type = "primary", delay = 2000) {
    const toasterHTML = '<div id="toaster" aria-live="polite" aria-atomic="true" style="position: fixed; bottom: 5px; right: 5px;">'
        +
        '<div id="toasterWrapper" style="position: sticky; bottom: 0; right: 0;"></div></div>';

    const toastHTML = '<div class="toast toast-' + type
        + '" role="alert" aria-live="assertive" aria-atomic="true" data-delay='
        + delay + '>\n' +
        '      <div class="toast-header">\n' +
        '        <strong class="mr-auto">' + title + '</strong>\n' +
        '        <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">\n'
        +
        '          <span aria-hidden="true">&times;</span>\n' +
        '        </button>\n' +
        '      </div>\n' +
        '      <div class="toast-body">\n' + message +
        '      </div>\n' +
        '    </div>';

    // Create the toaster if it does not exist
    if (!document.getElementById("toaster")) {
        $("body").append(toasterHTML);
    }

    // Append the toast to the toaster wrapper and show the toast
    const toasterWrapper = $("#toasterWrapper");
    toasterWrapper.append(toastHTML);
    $("body .toast:last").toast('show');

    // Delete after hidden
    toasterWrapper.on('hidden.bs.toast', '.toast', function () {
        $(this).remove();
    });
}

// Id count for adding id's to JSON from external APIs
let iterator = 1;

/**
 * Adds an id to the target
 * @param target is a JSON object
 */
function addIdentifier(target){
    target.id = iterator;
    iterator++;
}

/**
 * Loops through JSON object and recursively adds id's to all entries
 * @param obj is a JSON object
 */
function addIdToJSON(obj){
    for (let i in obj) {
        let c = obj[i];
        if (typeof c === 'object') {
            if (c.length === undefined) {
                //c is not an array
                addIdentifier(c);
            }
            addIdToJSON(c);
        }
    }
}
