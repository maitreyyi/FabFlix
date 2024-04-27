/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    let salesBodyElement = jQuery("#sales_details_body");

    console.log("handleResult: populating sales table from resultData");
    let total = 0;

    // append two html <p> created to the h3 body, which will refresh the page
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr><th>" + resultData[i]["salesId"] + "</th>" +
            "<th>" + resultData[i]["title"] + "</th>" +
            "<th>" + resultData[i]["quantity"] + "</th>" +
            "<th>" + resultData[i]["price"] + "</th></tr>";

        total += resultData[i]["price"];
        rowHTML += "</th>";
        salesBodyElement.append(rowHTML);
    }

    let totalBodyElement = jQuery("#total_body");
    console.log("handleResult: populating total table");
    totalBodyElement.append("<tr><th></th><th>" + total + "</th>");
}

/*
 * Once this .js is loaded, following scripts will be executed by the browser\
 */


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?", // Setting request url,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});