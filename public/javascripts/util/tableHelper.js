function populateTable(table, url, populate) {
    // Query API endpoint to get all destinations
    get(url)
    .then(response => {
        response.json()
        .then(json => {
            if (response.status !== 200) {
                // document.getElementById("otherError").innerHTML = json;
            } else {
                // Loop through json and insert into table
                for (const dest in json) {
                    populate(dest);
                }
            }
        });
    })
}