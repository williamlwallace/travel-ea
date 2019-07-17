/**
 * Checks a countries validity against the RestCountries API
 * @param {string} countryName The full name of the country to check
 * @param {string} countryCode The 3 digit country code
 * @returns {boolean} a promise containing a boolean indicating if the country is valid
 */
function checkCountryValidity(countryName, countryCode) {
    let countryUrl = "https://restcountries.eu/rest/v2/name/" + countryName
        + "?fullText=true?fields=numericCode";
    return get(countryUrl).then(countryResponse => {
        console.log(countryResponse);
        if (countryResponse.status > 400) {
            return {result: false};
        } else {
            return countryResponse.json().then(countryData => {
                return (parseInt(countryCode) === parseInt(
                    countryData["0"].numericCode));
            });
        }
    });
}