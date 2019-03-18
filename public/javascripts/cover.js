/**
 * Checks if the password field and the confirm password field are matching.
 *
 */
$('#password, #confirm_password').on('keyup', function () {
    if ($('#password').val() == $('#confirm_password').val()) {
        $('#message').html('');
        $('#registerBtn').prop('disabled', false);
    } else {
        $('#registerBtn').prop('disabled', true);
        $('#message').html('Password not matching!').css('color', 'red');
    }
});

/**
 * The JavaScript function to process a client logging in
 *
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function login(url, redirect) {
    const formData = new FormData(document.getElementById("loginForm"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    post(url,data)
    .then(response => {
        //need access to response status, so cant return promise
        response.json()
        .then(json => {
            if (response.status != 200) {
                showErrors(json, "loginForm");
            } else {
                window.location.href = redirect;
            }
        });
    });
}

/**
 * The JavaScript function to process a client signing up
 *
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function signup(url, redirect) {
    const formData = new FormData(document.getElementById("signupForm"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]] : pair[1],
    }), {});
    post(url, data)
        .then(response => {
            response.json()
            .then(json => {
                if (response.status != 200) {
                    showErrors(json, "signupForm");
                } else {
                    window.location.href = redirect;
                }
            });
    });
}

/**
 * The JavaScript function to clear error messages and
 * fields on the start page when the cancel button is pressed
 */
function cancel() {
    const elements = document.getElementById("main").getElementsByTagName("input");
    console.log(elements);
    for (i in elements) {
        elements[i].value = "";
    }
    hideErrors("signupForm");
    hideErrors("loginForm");
}