// function getAllDestinations (url) {
//     get(url).then(response => {
//         response.json().then(data => {
//             for(let i = 0; i < data.length; i++) {
//             // For each destination, make a list element that is the json string of object
//             let item = document.createElement("TD");
//             item.innerHTML = data[i]['name'];
//             item.value = data[i]['id'];
//             // Add list element to list
//             document.getElementById("countryDropDown").appendChild(item);
//         }
//     })
//     })
// }

var destinationsInTrip = [];

function addDestinationToTrip (destId) {
    console.log("destId");
    destinationsInTrip.push(destId);


}

function getDestinations() {
    return destinationsInTrip;
}

function displayDestinations () {
    for (var i=0; i < destinationsInTrip.length; i++) {
        var html = "<div class=\"sortable-card col-md-4\">";
        html += "<div class=\"card mb-4\">";
        html += "<div class=\"view overlay\">";
        html += "<a href=\"#!\">";
        html += "<div class=\"mask rgba-white-slight\"></div>";
        html += "</a>";
        html += "</div>";
        html += "<div class=\"card-body\">";
        html += "<h4 class=\"card-title\">" + destinationsInTrip[i]["name"] + "</h4>";
        html += "<p class=\"card-text\"><b>Type: </b>" + destinationsInTrip[i]["_type"] + "<br/><b>District: </b>" + destinationsInTrip[i]["district"] + "<br/><b>Latitude: </b>" + destinationsInTrip[i]["latitude"] + "<br/><b>Longitude: </b>" + destinationsInTrip[i]["longitude"] + "<br/><b>Country: </b>" + destinationsInTrip[i]["countryId"] + "</p>";
        html += "<button type=\"button\" class=\"btn btn-primary btn-md\" data-toggle=\"modal\" data-target=\"#modalEditForm\">Edit</button>";
        html += "</div>";
        html += "</div>";
        html += "</div>";

        document.getElementById("fillTripDestinations").innerHTML = html;
    }
}


function get(url) {
    return fetch(url, {
        method: "GET"
    })
}