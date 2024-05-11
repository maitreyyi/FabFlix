/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

console.log('inside employee employee_index.js');
function handleResult(resultData) {
    let metaData = jQuery("#meta-data");
    let rowHTML = "";

    for(let i = 0; i < resultData.length; i++) {
        rowHTML += "<table class = 'table table-striped table-bordered rounded overflow-hidden shadow'>"
                +  '<h3>' + resultData[i]['table_name'] + '</h3>';
        rowHTML += '<thead class = "table-primary"><tr>' +
                    '<th>Attribute</th>' +
                    '<th>Type</th></tr></thead>';

        rowHTML += '<tbody>';
        for (let j = 0; j < resultData[i]["columns"].length; j++) {
            rowHTML += '<tr><th>' + resultData[i]["columns"][j]["attribute"] + '</th>' +
                '<th>' + resultData[i]["columns"][j]["column_type"] + '</th></tr>';
        }
        rowHTML += '</tbody><br>';
    }
    metaData.append(rowHTML);
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "../api/dashboard", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});