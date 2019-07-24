let isAdmin;

/**
 * Sets up the two treasure hunt tables, calls methods to fill the data
 * @param {Number} userId - Id of logged in user
 * @param {Boolean} admin - Whether or not the logged in user is an admin
 */
function onTreasureHuntPageLoad(userId, admin) {
    isAdmin = admin;
    let myTreasureHuntTable = $("#myTreasure").DataTable();
    let allTreasureHuntTable = $("#allTreasure").DataTable();
    populateMyTreasureHunts(myTreasureHuntTable, userId);
    populateAllTreasureHunts(allTreasureHuntTable, userId);

    fillDestinationDropDown();
}

/**
 * Updates the treasure hunt with given ID, and force table to reload
 * @param {Number} id - ID of the treasure hunt
 * @param {Number} userId - ID of the logged in user
 * @param {Boolean} deletingOther - checking whether updating own hunt or another
 */
function updateTreasureHunt(id, userId) {

    let myTreasureHuntTable = $("#myTreasure").DataTable();
    let allTreasureHuntTable = $("#allTreasure").DataTable();

    const formData = new FormData(
        document.getElementById("updateTreasureHuntForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
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
                populateAllTreasureHunts(allTreasureHuntTable, userId);
                allTreasureHuntTable.draw();
                populateMyTreasureHunts(myTreasureHuntTable, userId);
                myTreasureHuntTable.draw();

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
                document.getElementById(
                    "updateRiddle").value = treasureHunt.riddle;
                document.getElementById(
                    "updateStartDate").value = treasureHunt.startDate;
                $('#updateDestinationDropDown').picker('set',
                    treasureHunt.destination.id);
                document.getElementById(
                    "updateEndDate").value = treasureHunt.endDate;

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
 * Deletes the treasure hunt with given ID, and forced table to reload
 * @param {Number} id - ID of the treasure hunt
 * @param {Number} userId - ID of the logged in user
 * @param {Boolean} deletingOther - checking whether updating own hunt or another
 */
function deleteTreasureHunt(id, userId, deletingOther) {

    let myTreasureHuntTable = $("#myTreasure").DataTable();
    let allTreasureHuntTable = $("#allTreasure").DataTable();

    _delete(
        treasureHuntRouter.controllers.backend.TreasureHuntController.deleteTreasureHunt(
            id).url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById("otherError").innerHTML = json;
            } else {
                if (deletingOther) {
                    populateAllTreasureHunts(allTreasureHuntTable, userId);
                    allTreasureHuntTable.draw();
                } else {
                    populateMyTreasureHunts(myTreasureHuntTable, userId);
                    myTreasureHuntTable.draw();
                }
            }
        })
    })
}

/**
 * Insert treasure hunts of a particular user into table
 * @param {Object} table - data table object
 * @param {Number} userId - ID of user to retrieve destinations for
 */
function populateMyTreasureHunts(table, userId) {
    table.clear();
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
                    let startDate = json[hunt].startDate;
                    let endDate = json[hunt].endDate;
                    let huntId = json[hunt].id;

                    let updateButton = `<button type="button" class="btn btn-popup" onclick='$("#updateTreasureHuntModal").modal("show"); populateUpdateTreasureHunt(${huntId})'>Update</button>`;
                    let buttonHtml = `<button type="button" class="btn btn-danger" onclick="deleteTreasureHunt(${huntId}, ${userId}, false)">Delete</button>`;

                    table.row.add(
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
function populateAllTreasureHunts(table, userId) {

    const today = new Date();
    const date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();
    const todayDate = new Date(date);

    table.clear();
    get(treasureHuntRouter.controllers.backend.TreasureHuntController.getAllTreasureHunts(
        userId).url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                document.getElementById("otherError").innerHTML = json;
            } else {
                for (let hunt in json) {
                    if (json[hunt].user.id !== userId) {
                        let riddle = json[hunt].riddle;
                        let startDate = json[hunt].startDate;
                        let endDate = json[hunt].endDate;
                        let huntId = json[hunt].id;
                        let startDateCompare = new Date(startDate);
                        let endDateCompare = new Date(endDate);

                        if((todayDate >= startDateCompare) && (todayDate < endDateCompare)) {
                            if (isAdmin) {
                                let updateButton = `<button type="button" class="btn btn-popup" onclick='$("#updateTreasureHuntModal").modal("show"); populateUpdateTreasureHunt(${huntId})'>Admin Update</button>`;
                                let buttonHtml = `<button type="button"  class="btn btn-danger" onclick="deleteTreasureHunt(${huntId}, ${userId}, true)">Admin Delete</button>`
                                table.row.add(
                                    [riddle, startDate, endDate, function () {
                                        return updateButton
                                    }, function () {
                                        return buttonHtml
                                    }]).draw(false);
                            } else {
                                table.row.add(
                                    [riddle, startDate, endDate]).draw(
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

    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});

    data.destinationId = parseInt(data.destinationId);
    data.user = {
        id: userId
    };

    data.destination = {
        id: data.destinationId
    };

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

                let myTreasureHuntTable = $("#myTreasure").DataTable();
                populateMyTreasureHunts(myTreasureHuntTable, userId);
                myTreasureHuntTable.draw();
            }
        });
    });
}

$('#add-treasure-hunt-button').click(function () {
    $("#createTreasureHuntModal").modal("show");
});
