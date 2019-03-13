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
