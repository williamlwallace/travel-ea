/**
 * Sets up the two treasure hunt tables, calls methods to fill the data
 */
function onTreasureHuntPageLoad() {
    setTimeZone();
    populateTreasureHunts();
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
 */
function updateTreasureHunt(id, userId) {
    const formData = new FormData(
        document.getElementById("updateTreasureHuntForm"));

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
                document.getElementById("updateTreasureHuntForm").reset();
                populateTreasureHunts();
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
 */
function deleteTreasureHunt(id) {
    const URL = treasureHuntRouter.controllers.backend.TreasureHuntController.deleteTreasureHunt(id).url;
    let initialDelete = true;
    const handler = function(status, json) {
        if (initialDelete) {
            if (status !== 200) {
                toast("Treasure hunt could not be deleted", json, "danger",
                    5000);
            } else {
                toast("Treasure hunt deleted",
                    "The treasure hunt was successfully deleted.",
                    "success");
            }
            initialDelete = false;
        }
        populateTreasureHunts();
    };

    const reqData = new ReqData(requestTypes["TOGGLE"], URL, handler);
    undoRedo.sendAndAppend(reqData);
}

/**
 * Retrieves and sorts all treasure hunts depending if the logged in user owns them.
 * Calls populating methods passing in sorted treasure hunts.
 */
function populateTreasureHunts() {
    getUserId().then(userId => {
        get(treasureHuntRouter.controllers.backend.TreasureHuntController.getAllTreasureHunts(userId).url)
        .then(response => {
            response.json().then(json => {
                const myTreasureHunts = [];
                const otherTreasureHunts = [];
                for (const hunt in json) {
                    if (json[hunt].user.id === parseInt(userId)) {
                        myTreasureHunts.push(json[hunt]);
                    } else {
                        otherTreasureHunts.push(json[hunt]);
                    }
                }
                populateMyTreasureHunts(myTreasureHunts);
                populateAllTreasureHunts(otherTreasureHunts);
            })
        })
    });
}

/**
 * Inserts a users treasure hunts into my treasure hunts table
 * @param {array} treasureHunts - Treasure hunts to insert
 */
function populateMyTreasureHunts(treasureHunts) {
    const myHuntTable = $("#myTreasure").DataTable();
    myHuntTable.clear();
    myHuntTable.draw();
    for (let hunt in treasureHunts) {
        let riddle = treasureHunts[hunt].riddle;
        let destination = treasureHunts[hunt].destination.name;
        let startDate = new Date(
            treasureHunts[hunt].startDate).toLocaleDateString();
        let endDate = new Date(
            treasureHunts[hunt].endDate).toLocaleDateString();
        let huntId = treasureHunts[hunt].id;

                    let updateButton = `<button type="button" class="btn btn-secondary" onclick='$("#updateTreasureHuntModal").modal("show"); populateUpdateTreasureHunt(${huntId})'>Update</button>`;
                    let buttonHtml = `<button type="button" class="btn btn-danger" onclick="deleteTreasureHunt(${huntId}, ${userId}, false)">Delete</button>`;

        myHuntTable.row.add(
            [riddle, destination, startDate, endDate, function () {
                return updateButton
            }, function () {
                return buttonHtml
            }]).draw(false);

    }
}

/**
 * Inserts all treasure hunts NOT including those of the current user into the allHuntTable
 * @param {Array} treasureHunts - Treasure hunts to insert into table
 */
function populateAllTreasureHunts(treasureHunts) {
    const allHuntTable = $("#allTreasure").DataTable();

    const today = new Date();
    const date = today.getFullYear() + '-' + (today.getMonth() + 1) + '-'
        + today.getDate();
    const todayDate = new Date(date);

    allHuntTable.clear();
    allHuntTable.draw();

    for (let hunt in treasureHunts) {
        let riddle = treasureHunts[hunt].riddle;
        let startDate = new Date(treasureHunts[hunt].startDate);
        let endDate = new Date(treasureHunts[hunt].endDate);
        let huntId = treasureHunts[hunt].id;

        if ((todayDate >= startDate) && (todayDate
            <= endDate)) {
            let formattedStartDate = startDate.toLocaleDateString();
            let formattedEndDate = endDate.toLocaleDateString();
            if (isUserAdmin()) {
                let updateButton = `<button type="button" class="btn btn-secondary" onclick='$("#updateTreasureHuntModal").modal("show"); populateUpdateTreasureHunt(${huntId})'>Update</button>`;
                let buttonHtml = `<button type="button"  class="btn btn-danger" onclick="deleteTreasureHunt(${huntId})">Delete</button>`;
                allHuntTable.row.add(
                    [riddle, formattedStartDate,
                        formattedEndDate, function () {
                        return updateButton
                    }, function () {
                        return buttonHtml
                    }]).draw(false);
            } else {
                allHuntTable.row.add(
                    [riddle, formattedStartDate,
                        formattedEndDate]).draw(
                    false);
            }
        }
    }
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
 * @param {Number} userId - the id of the user adding the hunt
 */
function addTreasureHunt(userId) {
    const formData = new FormData(
        document.getElementById("addTreasureHuntForm"));

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

    const URL = treasureHuntRouter.controllers.backend.TreasureHuntController.insertTreasureHunt().url;
    const handler = function(status, json) {
        if (status !== 200) {
            showErrors(json);
        } else {
            document.getElementById("addTreasureHuntForm").reset();
            toast("Riddle Created!",
                "The new riddle will be added to the table.",
                "success");
            $("#createTreasureHuntModal").modal("hide");
            populateTreasureHunts();
        }
    };
    const inverseHandler = (status, json) => {
        if (status === 200) {
            populateTreasureHunts();
        }
    };
    const reqData = new ReqData(requestTypes["CREATE"], URL, handler, data);
    undoRedo.sendAndAppend(reqData, inverseHandler);
}