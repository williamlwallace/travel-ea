//Initialises the data table and adds the data
$(document).ready(function () {
    populateTable($('#dtUser').DataTable());
    populateTrips($('#dtTrips').DataTable());
});

//Click listener that handles clicks in admin table
$('#dtUser').on('click', 'button', function() {
    let tableAPI = $('#dtUser').dataTable().api();
    let id = tableAPI.cell($(this).parents('tr'), 0).data();
    if ($(this).parents('td').index() == 2) {
        toggleAdmin(this, tableAPI, id);
    } else if ($(this).parents('td').index() == 3) {
        deleteUser(this, tableAPI, id);
    }
})

$('#dtTrips').on('click', 'button', function() {
    let tableAPI = $('#dtTrips').dataTable().api();
    let id = tableAPI.cell($(this).parents('tr'), 0).data();
    if ($(this).parents('td').index() == 4) {
        updateTrip(this, tableAPI, id);
    } else if ($(this).parents('td').index() == 5) {
        deleteTrip(this, tableAPI, id);
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
                tableAPI.row( $(button).parents('tr') ).remove().draw(false);
            }
        });
    });
}

/**
 * Adds or removes users admin powers and changes button text
 * @param {Object} button - Html button element
 * @param {Object} tableAPI - data table api
 * @param {Number} id - user id 
 */
function toggleAdmin(button, tableAPI, id) {
    let uri;
    let innerHTML;
    if (button.innerHTML.trim().startsWith("Revoke")) {
        uri = 'api/admin/revoke/' + id;
        innerHTML = "Grant admin";
    } else {
        uri = 'api/admin/grant/' + id;
        innerHTML = "Revoke admin";
    }
    post(uri, "")   
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

/**
 * Inserts users into admin table
 * @param {Object} table - data table object
 */
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

function populateTrips(table) {
    get('api/trip/getAllTrips/')
        .then(response => {
        response.json()
            .then(json => {
            if (response.status != 200) {
                document.getElementById("adminError").innerHTML = json;
            } else {
                console.log(json);
                for (const trip in json) {
                    const id = json[trip].id;
                    const tripDataList = json[trip].tripDataList;
                    const startDest = tripDataList[0].destination.name;
                    const endDest = tripDataList[(tripDataList.length - 1)].destination.name;
                    const tripLength = tripDataList.length;

                    update = "<button class=\"btn btn-secondary\">Update</button>";
                    removeTrip = "<button class=\"btn btn-danger\">Delete</button>"
                    table.row.add([id,startDest,endDest,tripLength,update,removeTrip]).draw(false);
                }
            }
        });
    })
}


function updateTrip(button, tableAPI, id) {
    window.location.href = '/trips/edit/' + id;
}

function deleteTrip(button, tableAPI, id) {
    console.log(id);
    _delete('api/trip/' + id)
        .then(response => {
        //need access to response status, so cant return promise
        response.json()
            .then(json => {
            if (response.status != 200) {
                document.getElementById("adminError").innerHTML = json;
            } else {
                tableAPI.row( $(button).parents('tr') ).remove().draw(false);
            }
        });
    });
}

/**
 * User creation for admins
 * @param {stirng} uri - api singup uri
 */
function createUser(uri, redirect) {
    const formData = new FormData(document.getElementById("signupForm"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]] : pair[1],
    }), {});
    post(uri, data)
        .then(response => {
            response.json()
            .then(json => {
                if (response.status != 200) {
                    showErrors(json, "signupForm");
                } else {
                    window.location.href = redirect;
                    location.reload();
                }
            });
    });
}