const MASTER_ADMIN_ID = 1;
let travellerTypeRequestTable;
let usersTable;
let tripsTable;
let requestOrder = 0;
let lastRecievedRequestOrder = -1;
let paginationHelper;
let tripTagDisplay;

//Initialises the data table and adds the data
$(document).ready(function () {
    tripTagDisplay = new TagDisplay("trip-tag-display");
    getUserId().then(userId => {
        onPageLoad(userId);
    });

    const errorRes = json => {
        document.getElementById('adminError').innerHTML = json;
    };
    //set table population urls
    // const tripsGetURL = tripRouter.controllers.backend.TripController.getAllTrips().url;
    // const ttGetURL = destinationRouter.controllers.backend.DestinationController.getPagedDestinations().url;
    const ttGetURL = destinationRouter.controllers.backend.DestinationController.getAllDestinationsWithRequests().url;
    const ttTableModal = {
        createdRow: function (row, data) {
            $(row).addClass("clickable-row");
            $(row).attr('data-id', data[0] + "," + data[4]);
        }
    };

    clearFilter();
    paginationHelper = new PaginationHelper(1, 1, getUserResults,
        "userPagination");
    getUserResults();

    travellerTypeRequestTable = new EATable('dtTravellerTypeModifications',
        ttTableModal, ttGetURL, populateTravellerTypeRequests, errorRes);

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
function deleteUser(button, id) {
    const handler = (status, json) => {
        if (status !== 200) {
            document.getElementById("adminError").innerHTML = json;
        } else {
            getUserResults();
        }
    };
    const URL = userRouter.controllers.backend.UserController.deleteOtherUser(
        id).url;
    const reqData = new ReqData(requestTypes["TOGGLE"], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

function clearFilter() {
    $("#searchQuery").val("");
    $("#pageSize").val(5);
}

/**
 * Get the value of the number of results to show per page
 * @returns {number} The number of results shown per page
 */
function getPageSize() {
    return $('#pageSize').val();
}

/**
 * Returns the name of the db column to search by
 * @returns {string} Name of db column to search by
 */
function getSortBy() {
    return $('#sortBy').val();
}

/**
 * Gets whether or not to sort by ascending
 * @returns {string} Either 'true' or 'false', where true is ascending, false is descending
 */
function getAscending() {
    return $('#ascending').val();
}

/**
 * Gets whether or not to sort by ascending
 * @returns {string} Either 'true' or 'false', where true is ascending, false is descending
 */
function getSearchQuery() {
    return $('#searchQuery').val();
}

function createUserCard(user) {
    const template = $("#userCardTemplate").get(0);
    const clone = template.content.cloneNode(true);
    let adminBtn = "Master";
    let deleteUser = "Master";
    if (user.id !== 1) {
        adminBtn = `<button id=\"toggleAdminBtn\" data-id=${user.id} class=\"admin btn btn-secondary\">`
            + ((user.admin) ? "Revoke admin"
                : "Grant admin") + "</button>";
        deleteUser = `<button data-delete=${user.id} class="user-delete btn btn-danger">Delete</button>`
    }

    const admin = (user.admin ? "True" : "False");
    const nonAdminIcon = "<em class=\"fas fa-user fa-8x\ style=\"vertical-align:middle\"></em>";
    const adminIcon = "<em class=\"fas fa-user-shield fa-8x\ style=\"vertical-align:middle\"><em>";

    $(clone).find("#card-thumbnail").attr("src",
        "/assets/images/default-profile-picture.jpg");
    $(clone).find("#username").append("Email: " + user.username);
    $(clone).find("#id").append("User Id: " + user.id);
    $(clone).find("#admin").text("Admin: " + admin);
    $(clone).find("#admin").html(
        "<em class=\"fas fa-user-shield\"\"></em>" + "Admin: " + admin);
    $(clone).find("#adminBtn").append(adminBtn);
    $(clone).find("#deleteBtn").append(deleteUser);

    if (user.admin) {
        $(clone).find("#card-thumbnail-div").css("padding-right", "0rem");
        $(clone).find(".card-header").css("padding-right", "0rem");
        $(clone).find("#card-thumbnail-div-body").html(adminIcon);
    } else {
        $(clone).find(".card-header").css("padding-right", "1.55rem");
        $(clone).find("#card-thumbnail-div-body").html(nonAdminIcon);
    }

    $("#userCardsList").get(0).appendChild(clone);
}

function getUserResults() {
    const url = new URL(
        userRouter.controllers.backend.UserController.userSearch().url,
        window.location.origin);
    url.searchParams.append("searchQuery", getSearchQuery());
    url.searchParams.append("pageNum", paginationHelper.getCurrentPageNumber());
    url.searchParams.append("pageSize", getPageSize().toString());
    url.searchParams.append("sortBy", getSortBy());
    url.searchParams.append("ascending", getAscending());
    url.searchParams.append("requestOrder", requestOrder++);
    get(url).then(response => {
        response.json().then(json => {
            if (response.status !== 200) {
                toast("Error", "Error fetching user data", "danger")
            } else {
                if (lastRecievedRequestOrder < json.requestOrder) {
                    const totalNumberPages = json.totalNumberPages;
                    $("#userCardsList").html("");
                    lastRecievedRequestOrder = json.requestOrder;
                    json.data.forEach((item) => {
                        createUserCard(item);
                    });

                    paginationHelper.setTotalNumberOfPages(totalNumberPages);

                    //Set click handler for toggle admin using data-id
                    $(".admin").click(event => {
                        toggleAdmin(event.target, $(event.target).data().id);
                    });
                    $(".user-delete").click(event => {
                        deleteUser(event.target, $(event.target).data().delete);
                    });

                    $(".user-card").click(event => {
                        location.href = `/profile/${$(
                            event.currentTarget).find(
                            "#toggleAdminBtn").data().id}`;
                    });
                }
            }
        });
    });
}

/**
 * Toggles the filter button between being visible and invisible
 */
function toggleFilterButton() {
    const toggled = $('#filterButton').css("display") === "block";
    $('#filterButton').css("display", toggled ? "none" : "block");
}

/**
 * Adds or removes users admin powers and changes button text
 *
 * @param {Object} button - Html button element
 * @param {Object} tableAPI - data table api
 * @param {Number} id - user id
 */
function toggleAdmin(button, id) {
    const URL = adminRouter.controllers.backend.AdminController.toggleAdmin(
        id).url;
    const initialToggle = true;
    const handler = function (status, json) {
        if (this.initialToggle) {
            if (status !== 200) {
                toast("Could not toggle admin privileges", json, "danger",
                    5000);
            } else {
                toast("Success", "Admin privileges toggled");
            }
            this.initialToggle = false;
        }
        if (status === 200) {
            this.button.innerHTML = button.innerHTML.trim().startsWith('Revoke')
                ? 'Grant admin' : 'Revoke admin';

            const adminLabel = $(button).closest('div').siblings("#admin");
            const adminIcon = "<em class=\"fas fa-user-shield\"\"></em>";

            adminLabel.text(adminLabel.text() === "Admin: True" ? "Admin: False"
                : "Admin: True");
            adminLabel.html(adminIcon + adminLabel.text());

        }
    }.bind({button, initialToggle});
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

let destPhoto;
let photoId;

/**
 * Populates the traveller type requests table
 */
function populateTravellerTypeRequests(json) {
    const rows = [];
    for (const dest in json) {
        const destId = json[dest].id;
        const destName = json[dest].name;


        for (const ttRequest in json[dest].travellerTypesPending) {
            const username = "";
            const modification = json[dest].travellerTypesPending[ttRequest].description;
            const ttId = json[dest].travellerTypesPending[ttRequest].id;
            rows.push([destId, destName, modification, username, ttId])
        }

        for (const photoRequest in json[dest].pendingPrimaryPhotos) {
            photoId = json[dest].pendingPrimaryPhotos[photoRequest].guid;
            destPhoto = json[dest].pendingPrimaryPhotos[photoRequest].filename;
            const modification = "Photo";
            rows.push([destId, destName, modification])
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
    const URL = tripRouter.controllers.backend.TripController.deleteTrip(
        id).url;
    const initialToggle = true;
    const handler = function (status, json) {
        if (this.initialToggle) {
            if (status !== 200) {
                toast("Could not delete trip", json, "danger", 5000);
            } else {
                toast("Success", "Trip deleted");
            }
            this.initialToggle = false;
        }
        if (status === 200) {
            tripsTable.populateTable();
        }
    }.bind({initialToggle});
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * User creation for admins
 *
 * @param {string} URL - api sign up uri
 */
function createUser(URL) {
    const formData = new FormData(document.getElementById("signupForm"));
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    const handler = (status, json) => {
        if (status !== 200) {
            document.getElementById("adminError").innerHTML = JSON.stringify(
                json);
            toast('User error', JSON.stringify(json), 'danger');
        } else {
            // usersTable.populateTable();
            $('#createUser').modal('hide');
        }
    };

    const reqData = new ReqData(requestTypes['CREATE'], URL, handler, data);
    //Handler can be used for inverse aswell
    undoRedo.sendAndAppend(reqData);
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
                if(!!ttId) {
                    $('<img style="max-width: 200px" src=' + "user_content/" + destPhoto + '>').appendTo("#modificationDiv");
                    document.getElementById("modificationType").innerText = "Photo Change";
                    $("#modificationDiv").attr('class', 'text-center');


                }
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
                if(!!ttId) {
                    rejectButton.addEventListener('click', function() {
                        rejectDestinationPrimaryPhoto(destId, photoId)
                    });
                } else {
                    rejectButton.addEventListener('click', function () {
                        rejectTravellerTypeRequest(destId, ttId);
                    });
                }

                let acceptButton = document.createElement("button");
                acceptButton.setAttribute("id", "acceptButton");
                acceptButton.innerHTML = "Accept";
                acceptButton.setAttribute("class", "btn btn-success");
                acceptButton.setAttribute("data-toggle", "modal");
                acceptButton.setAttribute("data-target", "#modal-destModify");
                if (!!ttId) {
                    acceptButton.addEventListener('click', function () {
                        acceptDestinationPrimaryPhoto(destId, photoId)
                    });
                } else {
                    acceptButton.addEventListener('click', function () {
                        toggleTravellerType(destId, ttId, true);
                    });
                }

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
 * Toggles the deletion of a request made to add or remove a traveller type to a destination.
 *
 * @param {Number} destId The destination id
 * @param {Number} ttId The traveller type id to add/remove
 */
function rejectTravellerTypeRequest(destId, ttId) {
    const URL = destinationRouter.controllers.backend.DestinationController.toggleRejectTravellerType(
        destId, ttId).url;
    const initialToggle = true;
    const handler = function (status, json) {
        if (this.initialToggle) {
            if (status !== 200) {
                toast("Could not reject request", json, "danger", 5000);
            } else {
                toast("Success", json);
            }
            this.initialToggle = false;
        }
        if (status === 200) {
            travellerTypeRequestTable.populateTable();
        }
    }.bind({initialToggle});
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Toggles the deletion of a request made to change a destination photo.
 *
 * @param {Number} destId The destination id
 * @param {Number} photoId The photoId
 */
function rejectDestinationPrimaryPhoto(destId, photoId) {
    const URL = destinationRouter.controllers.backend.DestinationController.rejectDestinationPrimaryPhoto(
        destId, photoId).url;
    const initialToggle = true;
    const handler = function (status, json) {
        if (this.initialToggle) {
            if (status !== 200) {
                toast("Could not reject request", json, "danger", 5000);
            } else {
                toast("Success", "Rejected destination photo change request");
            }
            this.initialToggle = false;
        }
        if (status === 200) {
            travellerTypeRequestTable.populateTable();
        }
    }.bind({initialToggle});
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}


/**
 * Toggles the acceptance of a request made to change a destination photo.
 *
 * @param {Number} destId The destination id
 * @param {Number} photoId The photoId
 */
function acceptDestinationPrimaryPhoto(destId, photoId) {
    const URL = destinationRouter.controllers.backend.DestinationController.acceptDestinationPrimaryPhoto(
        destId, photoId).url;
    const initialToggle = true;
    const handler = function (status, json) {
        if (this.initialToggle) {
            if (status !== 200) {
                toast("Could not accept request", json, "danger", 5000);
            } else {
                toast("Success", "Accepted destination photo change request");
            }
            this.initialToggle = false;
        }
        if (status === 200) {
            travellerTypeRequestTable.populateTable();
            setPrimaryPhoto(photoId)
        }
    }.bind({initialToggle});
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}