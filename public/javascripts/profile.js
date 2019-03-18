/* Display profile dropdowns with cool tag style in profile */
$('#nationality-multiselect').picker();
$('#country-multiselect').picker();
$('#traveller-types').picker();
$('#genderPicker').picker();
$('#dobDay').picker();
$('#dobMonth').picker();
$('#dobYear').picker();

/* Automatically display profile form when signing up */
$('#createProfileForm').modal('show');

/* Make the date picker pretty */
$('#dateOfBirth').datepicker({
    format: 'dd/mm/yyyy'
});

/**
 * The JavaScript function to process a client signing up
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function signUp(url, redirect) {
    const formData = new FormData(document.getElementById("signUp"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]] : pair[1],
    }), {});
    post(url, data)
        .then(response => {
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
