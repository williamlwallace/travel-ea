/**
 * On click handler to change the selected buttons on followers tabs
 */
$('#dests-following-btn').on('click', function() {
    document.getElementById('dests-following-btn').className = "btn btn-primary";
    document.getElementById('users-following-btn').className = "btn btn-outline-primary";

});

$('#users-following-btn').on('click', function() {
    document.getElementById('users-following-btn').className = "btn btn-primary";
    document.getElementById('dests-following-btn').className = "btn btn-outline-primary";
});