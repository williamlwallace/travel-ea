/*
 * Checks if the password field and the confirm password field are matching.
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
                console.log("Yeeeet");
                showErrors(json);
            } else {
                window.location.href = redirect;
            }
        });
    });
}

/**
 * The JavaScript function to process a client signing up
 * @param url The route/url to send the request to
 * @param redirect The page to redirect to if no errors are found
 */
function signUp(url, redirect) {
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
                    showErrors(json);
                } else {
                    window.location.href = redirect;
                }
            });
    });
}