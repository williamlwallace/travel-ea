class EATable {

    /**
     * Constructor
     *
     * @param  {Int} id Html id of table
     * @param  {Object} tableModal modal representing table
     * @param  {String} getURL url to get the data to populate
     * @param  {Function} populate function for data population
     * @param  {Function} error function if data collection fails
     */
    constructor(id, tableModal, getURL, populate, error) {
        this.id = id;
        this.getURL = getURL;
        this.table = ($(`#${id}`).DataTable(tableModal));
        this.populate = populate;
        this.error = error;
        this.populateTable()
    }

    /**
     * Populates the data of the table using the population callback
     */
    populateTable() {
        // Query API endpoint to get all destinations
        get(this.getURL)
        .then(response => {
            response.json()
            .then(json => {
                if (response.status !== 200) {
                    error(json);
                } else {
                    Promise.resolve(populate(json)).then((rows) => {
                        for (const row of rows) {
                            console.log(row);
                            
                            this.table.row.add(row).draw(false);
                            console.log(this.table);
                            
                        }
                    });
                    
                }
            });
        })
    }

    /**
     * Initizilisies onclick functions for specified coloumns
     *
     * @param {Object} colFunctions Object where column is the key and callback is the value
     */
    initButtonClicks(colFunctions) {
        table.on('click', 'button', function () {
            const tableAPI = table.api();
            const id = tableAPI.cell($(this).parents('tr'), 0).data();
            const col = $(this).parents('td').index();
            if (colFunctions.includes(col)) {
                colFunctions.col(this, tableAPI, id);
            }
        });
    }

    /**
     * Clears and repopulates table
     */
    refresh() {
        this.table.clear().draw();
        this.populateTable()
    }

    /**
    * Redirect to users profile when row is clicked.
    */
    initRowClicks(clickFunction) {
        this.table.on('click', 'tbody tr', clickFunction);
    }
}