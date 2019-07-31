/**
 * Calls an API endpoint to toggle an associated traveller type with a destination
 * @param {Number} destId - ID of destination to link/unlink
 * @param {Number} ttId - ID of traveller type to link/unlink
 * @param {Boolean} adminPage - Boolean value which determines what to update on a successful response
 */
function toggleTravellerType(destId, ttId, adminPage) {
    const URL = destinationRouter.controllers.backend.DestinationController.toggleDestinationTravellerType(
        destId, ttId).url;
    const initialToggle = true;
    const handler = function (status, json) {
        if (this.initialToggle) {
            if (status !== 200) {
                toast("Could not modify destination traveller types", json,
                    "danger", 5000);
            } else {
                toast("Success", json, "success");
            }
            this.initialToggle = false;
        }
        if (status === 200) {
            if (adminPage) {
                travellerTypeRequestTable.populateTable();
            } else {
                populateDestinationDetails(destId);
            }
        }
    }.bind({initialToggle});
    const reqData = new ReqData(requestTypes['TOGGLE'], URL, handler);
    undoRedo.sendAndAppend(reqData);
}