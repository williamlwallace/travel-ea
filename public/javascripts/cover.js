/**
 * Checks if the password field and the confirm password field are matching.
 */
$('#password, #confirm_password').on('keyup', function () {
    if ($('#password').val() === $('#confirm_password').val()) {
        $('#message').html('');
        $('#registerBtn').prop('disabled', false);
    } else {
        $('#registerBtn').prop('disabled', true);
        $('#message').html('Password not matching!').css('color', 'red');
    }
});

/**
 * Processes a client logging in
 *
 * @param {string} url  - The route/url to send the request to
 * @param {string} redirect - The page to redirect to if no errors are found
 */
function login(url, redirect) {
    const formData = new FormData(document.getElementById("loginForm"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    post(url, data)
    .then(response => {
        //need access to response status, so cant return promise
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showErrors(json, "loginForm");
            } else {
                // setCookie("JWT-Auth", json);
                window.location.href = redirect;
            }
        });
    });
}

/**
 * Processes a client signing up
 *
 * @param {string} url - The route/url to send the request to
 * @param {string} redirect - The page to redirect to if no errors are found
 */
function signup(url, redirect) {
    const formData = new FormData(document.getElementById("signupForm"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    post(url, data)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showErrors(json, "signupForm");
            } else {
                // setCookie("JWT-Auth", json);
                window.location.href = redirect;
            }
        });
    });
}

/**
 * Clear error messages and
 * fields on the start page when the cancel button is pressed
 */
function cancel() {
    const elements = document.getElementById("main").getElementsByTagName(
        "input");
    for (let i in elements) {
        elements[i].value = "";
    }
    hideErrors("signupForm");
    hideErrors("loginForm");
}

/**
 * Clicks the login button when enter is pressed while in the password field of login
 */
$("#login-password-field").keyup(function (event) {
    if (event.keyCode === 13) {
        $("#login-button").click();
    }
});

/**
 * Clicks the login button when enter is pressed while in the password field of login
 */
$("#confirm_password").keyup(function (event) {
    if (event.keyCode === 13) {
        if (!$("#registerBtn").prop('disabled')) {
            $("#registerBtn").click();
        }
    }
});