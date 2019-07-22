const MASTER_ADMIN_ID = 1;
let travellerTypeRequestTable;
let usersTable;

//Initialises the data table and adds the data
$(document).ready(function () {
    const errorRes = json => { document.getElementById('adminError').innerHTML = json; };
    //set table population urls
    const usersGetURL = userRouter.controllers.backend.UserController.userSearch().url
    const tripsGetURL = tripRouter.controllers.backend.TripController.getAllTrips().url
    ttGetURL = destinationRouter.controllers.backend.DestinationController.getAllDestinations(MASTER_ADMIN_ID).url

    const ttTableModal = {
        createdRow: function (row, data) {
            $(row).addClass("clickable-row");
            $(row).attr('data-id', data[0] + "," + data[4]);
        }
    }
    //create tables
    usersTable = new EATable('dtUser', {}, usersGetURL, populateUsers, errorRes);
    const tripsTable = new EATable('dtTrips', {}, tripsGetURL, populateTrips, errorRes);
    travellerTypeRequestTable = new EATable('dtTravellerTypeModifications', ttTableModal, ttGetURL, populateTravellerTypeRequests, errorRes)
    //set table click callbacks callbacks
    usersTable.initButtonClicks({
        2: toggleAdmin,
        3: deleteUser
    });
    tripsTable.initButtonClicks({
        5: deleteTrip
    });
    travellerTypeRequestTable.initRowClicks(function () {
        let idData = this.dataset.id.split(",");
        showTTSuggestion(idData[0], idData[1]);
    })
});

/**
 * Makes delete request with given user
 *
 * @param {Object} button - Html button element
 * @param {Object} tableAPI - data table api
 * @param {Number} id - user id
 */
function deleteUser(button, tableAPI, id) {
    const handler = (status, json) => {
        if (status !== 200) {
            document.getElementById("adminError").innerHTML = json;
        } else {
            usersTable.populateTable();
        }
    }
    const URL = userRouter.controllers.backend.UserController.deleteOtherUser(id).url;
    const reqData = new ReqData(requestTypes["DELETE"], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Adds or removes users admin powers and changes button text
 *
 * @param {Object} button - Html button element
 * @param {Object} tableAPI - data table api
 * @param {Number} id - user id
 */
function toggleAdmin(button, tableAPI, id) {
    const URL = adminRouter.controllers.backend.AdminController.toggleAdmin(id).url;
    const handler = function(status, json) {
        if (status !== 200) {
            document.getElementById('adminError').innerHTML = json;
        } else {
            const innerHTML = button.innerHTML.trim().startsWith('Revoke') ? 'Grant admin' : 'Revoke admin';
            this.button.innerHTML = innerHTML;
        }
    }.bind({button});
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Inserts users into admin table
 *
 * @param {Object} table - data table object
 */
function populateUsers(json) {
    const rows = []
    for (const user in json) {
        const id = json[user].id;
        const username = json[user].username;
        let admin = "Master";
        let deleteUser = "Master";
        if (id !== 1) {
            admin = "<button class=\"btn btn-secondary\">"
                + ((json[user].admin) ? "Revoke admin"
                    : "Grant admin") + "</button>";
            deleteUser = "<button class=\"btn btn-danger\">Delete</button>"
        }

        rows.push([id, username, admin, deleteUser])
    }
    return rows
}

/**
 * Insert trip data into table
 *
 * @param {Object} table - data table object
 */
function populateTrips(json) {
    rows = []
    for (const trip in json) {
        const id = json[trip].id;
        const tripDataList = json[trip].tripDataList;
        const startDest = tripDataList[0].destination.name;
        const endDest = tripDataList[(tripDataList.length
            - 1)].destination.name;
        const tripLength = tripDataList.length;
        const editURL = tripRouter.controllers.frontend.TripController.editTrip(
            id).url;

        let update = "<a href=\"" + editURL
            + "\" class=\"btn btn-secondary\">Update</a>";
        let removeTrip = "<button class=\"btn btn-danger\">Delete</button>";
        rows.push([id, startDest, endDest, tripLength, update,
            removeTrip]);
    }
    return rows;
}

/**
 * Populates the traveller type requests table
 */
function populateTravellerTypeRequests(json) {
   const rows = []
    for (const dest in json) {
        const destId = json[dest].id;
        const destName = json[dest].name;

        for (const ttRequest in json[dest].travellerTypesPending) {
            const username = "";
            const modification = json[dest].travellerTypesPending[ttRequest].description;
            const ttId = json[dest].travellerTypesPending[ttRequest].id;
            rows.push([destId, destName, modification, username, ttId])
        }
    }
    return rows   
}

/**
 * Sends delete request with trip id
 *
 * @param {Object} button - Html button element
 * @param {Object} tableAPI - data table api
 * @param {Number} id - user id
 */
function deleteTrip(button, tableAPI, id) {
    _delete(tripRouter.controllers.backend.TripController.deleteTrip(id).url)
    .then(response => {
        //need access to response status, so cant return promise
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById("adminError").innerHTML = json;
            } else {
                tableAPI.row($(button).parents('tr')).remove().draw(false);
            }
        });
    });
}

/**
 * User creation for admins
 *
 * @param {string} uri - api sign up uri
 * @param redirect the uri to redirect to
 */
function createUser(uri, redirect) {
    const formData = new FormData(document.getElementById("signupForm"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    post(uri, data)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showErrors(json, "signupForm");
            } else {
                window.location.href = redirect;
                location.reload();
            }
        });
    });
}

/**
 * Populates traveller type request modal on admin page
 *
 * @param {Number} destId - ID of destination in request
 * @param {Number} ttId - ID of traveller type in request
 */
function showTTSuggestion(destId, ttId) {
    get(destinationRouter.controllers.backend.DestinationController.getDestination(
        destId).url)
    .then(response => {
        response.json()
        .then(dest => {
            if (response.status !== 200) {
                toast("Could not retrieve request details", "", "danger", 5000);
            } else {
                document.getElementById(
                    "requestDestName").innerText = dest.name;
                document.getElementById(
                    "requestDestType").innerText = dest.destType;
                document.getElementById(
                    "requestDestDistrict").innerText = dest.district;
                document.getElementById(
                    "requestDestCountry").innerText = dest.country.name;

                if (dest.travellerTypes.length > 0) {
                    let travellerTypes = "";
                    for (let i = 0; i < dest.travellerTypes.length; i++) {
                        travellerTypes += ", "
                            + dest.travellerTypes[i].description;
                    }
                    document.getElementById(
                        "requestDestTT").innerText = travellerTypes.substr(2);
                } else {
                    document.getElementById("requestDestTT").innerText = "None";
                }

                for (const tt in dest.travellerTypesPending) {
                    if (dest.travellerTypesPending[tt].id.toString() === ttId) {
                        document.getElementById(
                            "requestDescription").innerText = dest.travellerTypesPending[tt].description;
                        break;
                    }
                }

                let found = false;
                for (const existingTT in dest.travellerTypes) {
                    if (dest.travellerTypes[existingTT].id.toString()
                        === ttId) {
                        document.getElementById(
                            "requestType").innerText = "Remove:";
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    document.getElementById("requestType").innerText = "Add:";
                }

                let rejButton = document.getElementById("rejectButton");
                if (rejButton != null) {
                    rejButton.parentNode.removeChild(rejButton);
                }

                let accButton = document.getElementById("acceptButton");
                if (accButton != null) {
                    accButton.parentNode.removeChild(accButton);
                }

                let rejectButton = document.createElement("button");
                rejectButton.setAttribute("id", "rejectButton");
                rejectButton.innerHTML = "Reject";
                rejectButton.setAttribute("class", "btn btn-danger");
                rejectButton.setAttribute("data-toggle", "modal");
                rejectButton.setAttribute("data-target", "#modal-destModify");
                rejectButton.addEventListener('click', function () {
                    rejectTravellerTypeRequest(destId, ttId);
                });

                let acceptButton = document.createElement("button");
                acceptButton.setAttribute("id", "acceptButton");
                acceptButton.innerHTML = "Accept";
                acceptButton.setAttribute("class", "btn btn-success");
                acceptButton.setAttribute("data-toggle", "modal");
                acceptButton.setAttribute("data-target", "#modal-destModify");
                acceptButton.addEventListener('click', function () {
                    if (document.getElementById("requestType").innerText
                        === "Remove:") {
                        deleteTravellerType(destId, ttId)
                        .then(status => {
                            if (status === 200) {
                                removeRow(destId, ttId);
                            }
                        });
                    } else {
                        addTravellerType(destId, ttId)
                        .then(status => {
                            if (status === 200) {
                                removeRow(destId, ttId);
                            }
                        });
                    }
                });

                document.getElementById("ttRequestButtons").appendChild(
                    rejectButton);
                document.getElementById("ttRequestButtons").appendChild(
                    acceptButton);
                $("#modal-destModify").modal("show");
            }
        })
    })
}

/**
 * Removes the requested change from the traveller type request table.
 *
 * @param {Number} destId The destination of the request to remove
 * @param {Number} ttId The traveler type id of the request to remove
 */
function removeRow(destId, ttId) {
    const element = document.getElementById(destId + "," + ttId);
    travellerTypeRequestTable.remove(element);
}

/**
 * Rejects a request made to add or remove a traveller type to a destination.
 *
 * @param {Number} destId The destination id
 * @param {Number} ttId The traveller type id to add/remove
 */
function rejectTravellerTypeRequest(destId, ttId) {
    put(destinationRouter.controllers.backend.DestinationController.rejectTravellerType(
        destId, ttId).url, {})
    .then(response => {
        response.json()
        .then(data => {
            if (response.status !== 200) {
                toast("Could not reject request", data, "danger", 5000);
            } else {
                toast("Request successfully rejected", data, "success");
                removeRow(destId, ttId);
            }
        })
    })
}