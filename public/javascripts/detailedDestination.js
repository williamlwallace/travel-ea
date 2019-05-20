/**
 * Function to get the relevant destination and fill the HTML
 * @param {Long} destinationId  of the destination to display
 */
function populateDestinationDetails(destinationId) {
    get(destinationRouter.controllers.backend.DestinationController.getDestination(destinationId).url)
        .then(response => {
            // Read response from server, which will be a json object
            response.json()
                .then(destination => {
                    if(response.status != 200) {
                        showErrors(destination);
                    } else {
                        document.getElementById("summary_name").innerText = destination.name;
                        document.getElementById("destination_name").innerText = destination.name;
                        document.getElementById("summary_type").innerText = destination._type;
                        document.getElementById("summary_district").innerText = destination.district;
                        document.getElementById("summary_country").innerText = destination.country.name;
                        document.getElementById("summary_latitude").innerText = destination.latitude;
                        document.getElementById("summary_longitude").innerText = destination.longitude;
                    }
                })
        })
}
