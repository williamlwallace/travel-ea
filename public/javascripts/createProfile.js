/* Automatically display profile form when signing up */
$('#createProfileForm').modal('show');

/**
 * The JavaScript function to process a client signing up
 * @param {Number} userId - ID of user to create a profile for
 * @param {string} uri - The route/uri to send the request to
 * @param {string} redirect - The page to redirect to if no errors are found
 */
function signUp(userId, uri, redirect) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("signUp"));

    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});

    // Sets userId
    data.userId = userId;

    // Convert nationalities, passports and Traveller Types to Correct JSON appropriate format
    data.nationalities = JSONFromDropDowns("nationalities");
    data.passports = JSONFromDropDowns("passports");
    data.travellerTypes = JSONFromDropDowns("travellerTypes");

    addNonExistingCountries(data.nationalities).then(nationalityResult => {
        addNonExistingCountries(data.passports).then(passportResult => {
            // Post json data to given uri
            post(uri, data)
            .then(response => {
                // Read response from server, which will be a json object
                response.json()
                .then(json => {
                    if (response.status !== 201) {
                        showErrors(json);
                    } else {
                        window.location.href = redirect;
                    }
                });
            });
        });
    });
}
