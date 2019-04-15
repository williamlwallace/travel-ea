$(document).ready(function () {
    //Initialises the data table and adds the data
    populateTable($('#dtUser').DataTable());
});

$('#dtUser').on('click', 'button', function() {
    let tableAPI = $('#dtUser').dataTable().api();
    let id = tableAPI.cell($(this).parents('tr'), 0).data();
    if ($(this).parents('td').index() == 2) {
        toggleAdmin(this, tableAPI, id);
    } else if ($(this).parents('td').index() == 3) {
        deleteUser(this, tableAPI, id);
    }
})

function deleteUser(button, tableAPI, id) {
    _delete('api/user/'+id)
    .then(response => {
        //need access to response status, so cant return promise
        response.json()
        .then(json => {
            if (response.status != 200) {
                document.getElementById("adminError").innerHTML = json;
            } else {
                console.log("Yeezy");
                tableAPI.row( $(button).parents('tr') ).remove().draw(false);
            }
        });
    });
}

function toggleAdmin(button, tableAPI, id) {
    let url;
    let innerHTML;
    if (button.innerHTML.trim().startsWith("Revoke")) {
        url = 'api/admin/revoke/' + id;
        innerHTML = "Grant admin";
    } else {
        url = 'api/admin/grant/' + id;
        innerHTML = "Revoke admin";
    }
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

function populateTable(table) {
    get('api/user/search')
    .then(response => {
        response.json()
        .then(json => {
            if (response.status != 200) {
                document.getElementById("adminError").innerHTML = json;
            } else {
                for (const user in json) {
                    const id = json[user].id;
                    const username = json[user].username;
                    let admin = "Master";
                    let deleteUser = "Master";
                    if (id != 1) {
                        admin = "<button class=\"btn btn-secondary\">" + ((json[user].admin) ? "Revoke admin" : "Grant admin") + "</button>";
                        deleteUser = "<button class=\"btn btn-danger\">Delete</button>"
                    }
                    
                    table.row.add([id,username,admin,deleteUser]).draw(false);
                }
            }
        });
    })
}