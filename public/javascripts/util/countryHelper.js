const countryApiUrl = "https://restcountries.eu/rest/v2/all?fields=name;numericCode";

/**
 * Checks a countries validity against the RestCountries API
 * @param {String} countryName The full name of the country to check
 * @param {Number} countryCode The 3 digit country code
 * @returns {Boolean} a promise containing a boolean indicating if the country is valid
 */
function checkCountryValidity(countryName, countryCode) {
    let countryUrl = "https://restcountries.eu/rest/v2/name/" + countryName
        + "?fullText=true?fields=numericCode";
    return get(countryUrl).then(countryResponse => {
        if (countryResponse.status > 400) {
            return false;
        } else {
            return countryResponse.json().then(countryData => {
                return (parseInt(countryCode) === parseInt(
                    countryData["0"].numericCode));
            });
        }
    });
}

/**
 * Finds the name of a country according to restcountries.eu by id
 * @param {String} countryCode the country code/id
 * @returns The name of the country or null if not found
 */
function getCountryNameById(countryCode) {
    return get(countryApiUrl).then(response => {
        return response.json().then(data => {
            for (let country in data) {
                if (parseInt(data[country].numericCode) === parseInt(
                    countryCode)) {
                    return data[country].name;
                }
            }
            return null;
        });
    });
}

/**
 * Checks with the Team Proffat database if the country is present
 * @param id the id of the country to check
 * @returns {Boolean} whether or not the country exists
 */
function checkCountryExists(id) {
    return get(
        countryRouter.controllers.backend.CountryController.getCountryById(
            id).url)
    .then(response => {
        return response.status === 200;
    });
}

/**
 * Adds a list of countries to the database if they are not present already
 * @param {Array} countries an array of objects containing country id
 * @returns {Array} an array containing each country id and whether or not the
 * function was successful in ensuring it is in the database
 */
function addNonExistingCountries(countries) {
    let success = [];
    for (const country of countries) {
        let countryId = country.id;
        success.push(checkCountryExists(countryId).then(exists => {
            if (!exists) {
                return getCountryNameById(countryId).then(countryName => {
                    if (countryName === null) {
                        return {id: countryId, result: false};
                    } else {
                        let newCountry = {id: countryId, name: countryName};
                        return post(
                            countryRouter.controllers.backend.CountryController.addCountry().url,
                            newCountry).then(response => {
                            return {
                                id: countryId,
                                result: response.status === 200
                            };
                        });
                    }
                });
            } else {
                return {id: countryId, result: true};
            }
        }));
    }
    return Promise.all(success);
}