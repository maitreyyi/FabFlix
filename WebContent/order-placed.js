/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log(resultData);
    let salesBodyElement = jQuery("#sales_details_body");

    console.log("handleResult: populating sales table from resultData");
    let total = 0;

    // append two html <p> created to the h3 body, which will refresh the page
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr><th>" + resultData[i]["salesId"] + "</th>" +
            "<th>" + resultData[i]["title"] + "</th>" +
            "<th>" + resultData[i]["quantity"] + "</th>" +
            "<th>$" + resultData[i]["price"].toFixed(2) + "</th></tr>";

        console.log(resultData[i]["price"] + typeof(resultData[i]["price"]));
        console.log(resultData[i]["quantity"] + typeof(resultData[i]["quantity"]));
        console.log((resultData[i]["price"] * resultData[i]["quantity"]).toFixed(2) + typeof(resultData[i]["price"] * resultData[i]["quantity"]).toFixed(2))

        let price = resultData[i]["price"] * resultData[i]["quantity"];
        console.log(typeof(price));
        total += price;
        rowHTML += "</th>";
        salesBodyElement.append(rowHTML);
    }

    let totalBodyElement = jQuery("#total_body");
    console.log("handleResult: populating total table");
    totalBodyElement.append("<tr><th colspan='3'></th><th>$" + total.toFixed(2) + "</th>");
}

/*
 * Once this .js is loaded, following scripts will be executed by the browser\
 */


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/order-placed?", // Setting request url,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});