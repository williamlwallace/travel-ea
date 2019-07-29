let isAdmin;

/**
 * Sets up the two treasure hunt tables, calls methods to fill the data
 * @param {Number} userId - Id of logged in user
 * @param {Boolean} admin - Whether or not the logged in user is an admin
 */
function onTreasureHuntPageLoad(userId, admin) {
    isAdmin = admin;
    setTimeZone();
    populateMyTreasureHunts(userId);
    populateAllTreasureHunts(userId);
    fillDestinationDropDown();

    $('#add-treasure-hunt-button').click(function () {
        $("#createTreasureHuntModal").modal("show");
    });
}

/**
 * Retrieves the time zone of the user and sets title attribute of start date and end date columns on both tables
 */
function setTimeZone() {
    let offset = new Date().getTimezoneOffset() / 60;
    let offsetString;

    if (offset < 0) {
        offsetString = "Timezone (UTC" + offset + ")";
    } else {
        offsetString = "Timezone (UTC+" + offset + ")";
    }

    $("#myTHStartDate").attr("title", offsetString);
    $("#myTHEndDate").attr("title", offsetString);
    $("#allTHStartDate").attr("title", offsetString);
    $("#allTHEndDate").attr("title", offsetString);
}

/**
 * Updates the treasure hunt with given ID, and force table to reload
 * @param {Number} id - ID of the treasure hunt
 * @param {Number} userId - ID of the logged in user
 * @param {Boolean} deletingOther - checking whether updating own hunt or another
 */
function updateTreasureHunt(id, userId) {
    const formData = new FormData(
        document.getElementById("updateTreasureHuntForm"));

    document.getElementById("updateTreasureHuntForm").reset();

    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo, [pair[0]]: pair[1],
    }), {});

    data.destination = {
        id: data.destinationId
    };

    delete data.destinationId;

    put(treasureHuntRouter.controllers.backend.TreasureHuntController.updateTreasureHunt(
        id).url, data)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById(
                    "otherError").innerHTML = json;
                toast("Treasure hunt could not be updated",
                    "There was an error in updating the treasure hunt",
                    "danger", 5000);

            } else {
                populateAllTreasureHunts(userId);
                populateMyTreasureHunts(userId);

                toast("Treasure hunt successfully updated",
                    "Your treasure hunt has been updated",
                    "success");

                $("#updateTreasureHuntModal").modal("hide");
            }
        })
    });
}

/**
 * Fills the update treasure hunt modal with the information of that treasure hunt
 *
 * @param {Number} id - the ID of the treasure hunt
 */
function populateUpdateTreasureHunt(id) {
    fillDestinationDropDown();
    get(treasureHuntRouter.controllers.backend.TreasureHuntController.getTreasureHuntById(
        id).url)
    .then(response => {
        response.json()
        .then(treasureHunt => {
            if (response.status !== 200) {
                showErrors(treasureHunt);
            } else {
                hideErrors("updateTreasureHuntForm");
                let startDate = formatDateForInput(treasureHunt.startDate);
                let endDate = formatDateForInput(treasureHunt.endDate);

                document.getElementById(
                    "updateRiddle").value = treasureHunt.riddle;
                $('#updateDestinationDropDown').picker('set',
                    treasureHunt.destination.id);
                document.getElementById("updateStartDate").value = startDate;
                document.getElementById("updateEndDate").value = endDate;

                getUserId().then(userId => {
                    document.getElementById(
                        "updateTreasureHunt").onclick = function () {
                        updateTreasureHunt(id, userId)
                    }
                });
            }
        })
    })
}

/**
 * Formats a date in yyyy,m,d format to populate a date input field
 * @param date - Date to be formatted
 * @returns {string} String date in format of yyyy-mm-dd
 */
function formatDateForInput(date) {
    let dateList = date.toString().split(",");
    let day = dateList[2].padStart(2, "0");
    let month = dateList[1].padStart(2, "0");
    let year = dateList[0];

    return year + "-" + month + "-" + day;
}

/**
 * Deletes the treasure hunt with given ID, and forced table to reload
 * @param {Number} id - ID of the treasure hunt
 * @param {Number} userId - ID of the logged in user
 * @param {Boolean} deletingOther - checking whether updating own hunt or another
 */
function deleteTreasureHunt(id, userId, deletingOther) {
    const URL = treasureHuntRouter.controllers.backend.TreasureHuntController.deleteTreasureHunt(id).url;
    const handler = function(status, json) {
        if (status !== 200) {
            toast("Treasure hunt could not be deleted", json, "danger", 5000);
        } else {
            console.log(deletingOther);
            if (deletingOther) {
                populateAllTreasureHunts(userId);
            } else {
                console.log(userId);
                populateMyTreasureHunts(userId);
            }
            toast("Treasure hunt deleted",
                "The treasure hunt was successfully deleted.",
                "success");
        }
    };

    const reqData = new ReqData(requestTypes["TOGGLE"], URL, handler);
    undoRedo.sendAndAppend(reqData);

    // _delete(
    //     treasureHuntRouter.controllers.backend.TreasureHuntController.deleteTreasureHunt(
    //         id).url)
    // .then(response => {
    //     response.json()
    //     .then(json => {
    //         if (response.status !== 200) {
    //             toast("Treasure hunt could not be deleted",
    //                 "There was an error in deleting the treasure hunt.",
    //                 "danger", 5000);
    //         } else {
    //             if (deletingOther) {
    //                 populateAllTreasureHunts(userId);
    //             } else {
    //                 populateMyTreasureHunts(userId);
    //             }
    //             toast("Treasure hunt deleted",
    //                 "The treasure hunt was successfully deleted.",
    //                 "success");
    //         }
    //     })
    // })
}

/**
 * Insert treasure hunts of a particular user into table
 * @param {Object} table - data table object
 * @param {Number} userId - ID of user to retrieve destinations for
 */
function populateMyTreasureHunts(userId) {
    console.log("CALLED");
    $("#myTreasure").DataTable().clear();
    get(treasureHuntRouter.controllers.backend.TreasureHuntController.getAllUserTreasureHunts(
        userId).url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById("otherError").innerHTML = json;
            } else {
                for (let hunt in json) {
                    let riddle = json[hunt].riddle;
                    let destination = json[hunt].destination.name;
                    let startDate = new Date(
                        json[hunt].startDate).toLocaleDateString();
                    let endDate = new Date(
                        json[hunt].endDate).toLocaleDateString();
                    let huntId = json[hunt].id;

                    let updateButton = `<button type="button" class="btn btn-popup" onclick='$("#updateTreasureHuntModal").modal("show"); populateUpdateTreasureHunt(${huntId})'>Update</button>`;
                    let buttonHtml = `<button type="button" class="btn btn-danger" onclick="deleteTreasureHunt(${huntId}, ${userId}, false)">Delete</button>`;

                    $("#myTreasure").DataTable().row.add(
                        [riddle, destination, startDate, endDate, function () {
                            return updateButton
                        }, function () {
                            return buttonHtml
                        }]).draw(false);

                }
            }
        })
    })
}

/**
 * Insert all treasure hunts NOT including those of the current user
 * @param {Object} table - data table object
 * @param {Number} userId - ID of current user, who hunts are excluded
 */
function populateAllTreasureHunts(userId) {

    const today = new Date();
    const date = today.getFullYear() + '-' + (today.getMonth() + 1) + '-'
        + today.getDate();
    const todayDate = new Date(date);

    $("#allTreasure").DataTable().clear();
    get(treasureHuntRouter.controllers.backend.TreasureHuntController.getAllTreasureHunts(
        userId).url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById("otherError").innerHTML = json;
            } else {
                for (let hunt in json) {
                    if (json[hunt].user.id !== parseInt(userId)) {
                        let riddle = json[hunt].riddle;
                        let startDate = new Date(json[hunt].startDate);
                        let endDate = new Date(json[hunt].endDate);
                        let huntId = json[hunt].id;

                        if ((todayDate >= startDate) && (todayDate
                            <= endDate)) {
                            let formattedStartDate = startDate.toLocaleDateString();
                            let formattedEndDate = endDate.toLocaleDateString();
                            if (isAdmin) {
                                let updateButton = `<button type="button" class="btn btn-popup" onclick='$("#updateTreasureHuntModal").modal("show"); populateUpdateTreasureHunt(${huntId})'>Update</button>`;
                                let buttonHtml = `<button type="button"  class="btn btn-danger" onclick="deleteTreasureHunt(${huntId}, ${userId}, true)">Delete</button>`;
                                $("#allTreasure").DataTable().row.add(
                                    [riddle, formattedStartDate,
                                        formattedEndDate, function () {
                                        return updateButton
                                    }, function () {
                                        return buttonHtml
                                    }]).draw(false);
                            } else {
                                $("#allTreasure").DataTable().row.add(
                                    [riddle, formattedStartDate,
                                        formattedEndDate]).draw(
                                    false);
                            }
                        }
                    }
                }
            }
        })
    })
}

/**
 * Gets all public destinations and fills into a dropdown
 */
function fillDestinationDropDown() {
    get(destinationRouter.controllers.backend.DestinationController.getAllPublicDestinations(
        1).url)
    .then(response => {
        response.json()
        .then(json => {
            const destinationDict = {};
            json.forEach(function (destination) {
                destinationDict[destination['id']] = destination['name'];
            });
            fillDropDown("destinationDropDown", destinationDict);
            fillDropDown("updateDestinationDropDown", destinationDict);
        });
    });
}

/**
 * Function creates the treasure hunt from information entered in the form
 * @param {string} url - API URL to add a Treasure Hunt
 * @param {string} redirect - URL of redirect page
 * @param {Long} userId - the id of the user adding the hunt
 */
function addTreasureHunt(url, redirect, userId) {
    const formData = new FormData(
        document.getElementById("addTreasureHuntForm"));

    document.getElementById("addTreasureHuntForm").reset();

    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});

    data.user = {
        id: userId
    };

    data.destination = {
        id: parseInt(data.destinationId)
    };

    delete data.destinationId;

    post(url, data)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                showErrors(json);
            } else {
                toast("Riddle Created!",
                    "The new riddle will be added to the table.",
                    "success");
                $("#createTreasureHuntModal").modal("hide");

                populateMyTreasureHunts(userId);
            }
        });
    });
}