/* Display profile dropdowns with cool tag style in profile */
$('#nationalities').picker();
$('#passports').picker();
$('#travellerTypes').picker();
$('#gender').picker();
/* Automatically display profile form when signing up */
$('#createProfileForm').modal('show');


/**
 * The JavaScript function to process a client signing up
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function signUp(url, redirect) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("signUp"));
    formData.append("userId", '1');
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]] : pair[1],
    }), {});

    // Post json data to given url
    post(url, data)
        .then(response => {
        // Read response from server, which will be a json object
            response.json()
                .then(json => {
                    if (response.status != 200) {
                        showErrors(json);
                    } else {
                        window.location.href = redirect;
                    }
                });
        });
}