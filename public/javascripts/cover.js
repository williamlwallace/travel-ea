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

/*
 * Displays the form on the start page that had the error.
 */
$(document).ready(function() {
    if ($('#flashMessage1').hasClass("loginError")) {
        $("#loginBtn").click();
    } if ($('#flashMessage2').hasClass("signUpError")) {
        $("#signUpBtn").click();
    }
});

/*
 * Hides error message if cancel is pressed on start page.
 */
$('#cancelBtn1, #cancelBtn2').click(function () {
    $("#flashMessage1").hide();
    $("#flashMessage2").hide();
});
