/* Displays the people table correctly */
$(document).ready(function() {
    $('#dtPeople').DataTable();
    $('.dataTables_length').addClass('bs-select');
});

/* Display profile dropdowns with cool tag style in profile */
$('#nationality-multiselect').picker();
$('#country-multiselect').picker();
$('#traveller-types').picker();
$('#genderPicker').picker();
$('#dobDay').picker();
$('#dobMonth').picker();
$('#dobYear').picker();

/* Display profile drop downs with cool tag style in edit profile */
$('#edit-nationality-multiselect').picker();
$('#edit-country-multiselect').picker();
$('#edit-traveller-types').picker();
$('#edit-gender-picker').picker();
$('#edit-dob-day').picker();
$('#edit-dob-month').picker();
$('#edit-dob-year').picker();


/* Preset drop downs - will read a jSON file from database */
$('#edit-nationality-multiselect').picker('set', "swedish");
$('#edit-country-multiselect').picker('set', "Sweden");
$('#edit-gender-picker').picker('set', "female");
$('#edit-traveller-types').picker('set', "Groupies");
$('#edit-dob-day').picker('set', '02');
$('#edit-dob-month').picker('set', "11");
$('#edit-dob-year').picker('set', "1989");

/* Automatically display profile form when signing up */
$('#createProfileForm').modal('show');

/*
 * Changing the Navbar colour depending on the situation
 */
$(document).ready(function() {
    var scroll_start = 0;
    var startchange = $('#startchange');
    var offset = startchange.offset();
    $(document).scroll(function() {
        scroll_start = $(this).scrollTop();
        if(scroll_start > offset.top  - $('#navbar').outerHeight()) {
            $('#navbar').css('background-color', '#5283B7');
        } else {
            $('#navbar').css('background-color', 'transparent');
        }
    });
});

$('.collapse').on('show.bs.collapse', function () {
    $('#navbar').css('background-color', '#5283B7');
})

$('.collapse').on('hide.bs.collapse', function () {
    $('#navbar').css('background-color', 'transparent');
})