class EATable {

    /**
     * Constructor
     *
     * @param  {Number} id Html id of table
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
     *
     * @param {String} url the url to make the call to
     */
    populateTable(url=null) {
        // Query API endpoint to get all destinations
        this.table.clear().draw();
        if (!url) url = this.getURL;

        get(url)
        .then(response => {
            response.json()
            .then(json => {
                if (response.status !== 200) {
                    error(json);
                } else {
                    Promise.resolve(this.populate(json)).then((rows) => {
                        for (const row of rows) {
                            this.table.row.add(row).draw(false);
                        }
                    });
                    
                }
            });
        })
    }

    /**
     * Initialises onclick functions for specified columns
     *
     * @param {Object} colFunctions Object where column is the key and callback is the value
     */
    initButtonClicks(colFunctions) {
        const tableAPI = $(`#${this.id}`).dataTable().api();
        this.table.on('click', 'button', function () {
            const id = tableAPI.cell($(this).parents('tr'), 0).data();
            const col = $(this).parents('td').index();
            if (col.toString() in colFunctions) {
                colFunctions[col.toString()](this, tableAPI, id);
            }
        });
    }

    /**
    * Redirect to uri when row is clicked.
    *
    * @param {Function} clickFunction on row click callback function
    */
    initRowClicks(clickFunction) {
        this.table.on('click', 'tbody tr', clickFunction);
    }

    /**
     * Removes row from table
     *
     * @param {Object} element element to remove from table
     */
    remove(element) {
        this.table.row(element).remove().draw(false);
    }

    /**
     * Adds a row to table
     *
     * @param {Object} rowData Array of row data
     */
    add(rowData) {
        this.table.row.add(rowData).draw(false);
    }
}