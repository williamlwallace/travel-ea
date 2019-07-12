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

                //TODO add the new riddle to the table
            }
        });
    });
}

$('#add-treasure-hunt-button').click(function () {
    $("#createTreasureHuntModal").modal("show");
});