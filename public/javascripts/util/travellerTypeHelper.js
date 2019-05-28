/**
 * Calls an API endpoint to associate a traveller type to a destination
 * @param {Number} destId - ID of destination to link
 * @param {Number} ttId - ID of traveller type to link
 */
function addTravellerType(destId, ttId) {
    return put(destinationRouter.controllers.backend.DestinationController.addTravellerType(destId, ttId).url, {})
    .then(response => {
        return response.json()
        .then(data => {
            if (response.status !== 200) {
                toast("Could not modify destination traveller types", data, "danger", 5000);
            } else {
                toast("Success", data, "success");
            }
            return response.status;
        })
    })
}

/**
 * Calls an API endpoint to remove an associated traveller type from a destination
 * @param {Number} destId - ID of destination to remove
 * @param {Number} ttId - ID of traveller type to remove
 */
function deleteTravellerType(destId, ttId) {
    return put(destinationRouter.controllers.backend.DestinationController.removeTravellerType(destId, ttId).url, {})
    .then(response => {
        return response.json()
        .then(data => {
            if (response.status !== 200) {
                toast("Could not modify destination traveller types", data, "danger", 5000);
            } else {
                toast("Success", data, "success");
            }
            return response.status;
        })
    })
}