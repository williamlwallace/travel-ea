$(document).ready(function () {
    //Initialises the data table and adds the filter button to the right of the search field
    $('#dtUser').DataTable();
});

$('#dtUser').on('click', 'button', function() {
    if ($(this).parents('td').index() != 3) {
        return;
    }
    let tableAPI = $('#dtUser').dataTable().api();
    let id = tableAPI.cell($(this).parents('tr'), 0).data();
    _delete('api/user/'+id)
    .then(response => {
        //need access to response status, so cant return promise
        response.json()
        .then(json => {
            if (response.status != 200) {
                document.getElementById("adminError").innerHTML = json;
            } else {
                tableAPI.row( $(this).parents('tr') ).remove().draw(false);
            }
        });
    });
})

function toggleAdmin(revokeURL, grantURL, button) {
    let url;
    let innerHTML;
    console.log("-"+button.innerHTML.trim());
    if (button.innerHTML.trim().startsWith("Revoke")) {
        url = revokeURL;
        innerHTML = "Grant admin";
    } else {
        url = grantURL;
        innerHTML = "Revoke admin";
    }
    console.log(url);
    console.log(innerHTML);
    post(url, "")   
    .then(response => {
        //need access to response status, so cant return promise
        response.json()
        .then(json => {
            if (response.status != 200) {
                document.getElementById("adminError").innerHTML = json;
            } else {
                button.innerHTML = innerHTML;
            }
        });
    });
}

deleteUser()