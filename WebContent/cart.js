function handleResult(resultData) {
    let moviesTableBodyElement = jQuery("#cart_table_body");

    console.log("handleResult: populating cart");

    // append two html <p> created to the h3 body, which will refresh the page
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        //title
        rowHTML += '<tr><th><a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_title"] + '</a></th>';
        //price
        rowHTML += "<th>" + "$" + resultData[i]["price"] + "</th>";
        //quantity
        rowHTML += "<th>" + resultData[i]["quantity"] + "</th>";

        rowHTML += "</tr>";
        moviesTableBodyElement.append(rowHTML);
    }

}
//getting cart results
const getCart = async () => {
    const data = await fetch(
        `api/cart`,
        {
            method: "GET",
            headers: {
                'Content-Type': 'application/json'
            }
        }
    )
    return data.json();
}



// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/cart", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});