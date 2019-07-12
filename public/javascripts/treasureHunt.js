function onTreasureHuntPageLoad(userId) {
    const myTreasureHuntTable = $("#myTreasure").DataTable();
    populateMyTreasureHunts(myTreasureHuntTable, userId);
    fillDestinationDropDown();
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
                for (const hunt in json) {
                    const riddle = json[hunt].riddle;
                    const destination = json[hunt].destination.name;
                    const startDate = json[hunt].startDate;
                    const endDate = json[hunt].endDate;

                    table.row.add(
                        [riddle, destination, startDate, endDate]).draw(false);

                }
            }
        })
    })
}

/**
 * Gets all public destinations and fills into a dropdown
 */
function fillDestinationDropDown() {
    //TODO change this api to one that only gets public destinations
    get(destinationRouter.controllers.backend.DestinationController.getAllDestinations(
        1).url)
    .then(response => {
        response.json()
        .then(json => {
            let destinationDict = {};
            for (let i = 0; i < json.length; i++) {
                destinationDict[json[i]['id']] = json[i]['name'];
            }
            fillDropDown("destinationDropDown", destinationDict);
        });
    });
}


/**
 * Function creates the treasure hunt from information entered in the
 * @param {string} url - API URL to add a Treasure Hunt
 * @param {string} redirect - URL of redirect page
 * @param {Long} userId - the id of the user adding the hunt
 */
function addTreasureHunt(url, redirect, userId) {
    const formData =  new FormData(
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

                let table = $('#myTreasure').DataTable();
                const riddle = data.riddle;
                const startDate = data.startDate;
                const endDate = data.endDate;
                let destination = data.destination;

                let destinations = document.getElementById(
                    "destinationDropDown").getElementsByTagName("option");
                for (let i = 0; i < destinations.length; i++) {
                    if (parseInt(destinations[i].value) === data.destination.id) {
                        destination = destinations[i].innerText;
                        break;
                    }
                }

                table.row.add([riddle, destination, startDate, endDate]).draw(false);

            }
        });
    });
}

$('#add-treasure-hunt-button').click(function () {
    $("#createTreasureHuntModal").modal("show");
});