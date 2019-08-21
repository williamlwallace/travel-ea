/**
 * Calls an API endpoint to toggle an associated primary photo with a destination
 * @param {Number} destId - ID of destination
 * @param {Number} photoId - ID of photo to accept/reject
 * @param {Boolean} adminPage - Boolean value which determines what to update on a successful response
 */
function toggleDestinationPhotoRequest(destId, photoId, adminPage) {
    const URL = destinationRouter.controllers.backend.DestinationController.changeDestinationPrimaryPhoto(destId, photoId).url;
    const initialToggle = true;
    const handler = function (status, json) {
        if (this.initialToggle) {
            if (status !== 200) {
                toast("Could not modify destination primary photo", json,
                    "danger", 5000);
            } else {
                toast("Success", json);
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