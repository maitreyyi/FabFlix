/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

/**
function search(query){

}
function displayResults(results){

}**/
function handleResult(resultData) {

    console.log("handleResult: populating genre hyperlinks");

    // populate the alphabet browsing
    let genreLink = jQuery("#genres");
    let content = "";
    for (let i = 0; i < resultData.length; i++)
    {
        content += '<a href="movie-list.html?genre=' + resultData[i]["genre_id"]
                    + '">' + resultData[i]["genre_name"] + '</a>';
        if (i + 1 < resultData.length)
        {
            content += " "
        }
    }
    genreLink.append("<h5>"+ content + "</h5>");

    console.log("handleResult: populating alphanumeric hyperlinks");

    // populate the alphabet browsing
    let alphanumericLink = jQuery("#alphanumeric");
    content = "";
    for (let i = 65; i < 91; i++)
    {
        content += '<a href="movie-list.html?start=' + String.fromCharCode(i)
                    + '">' + String.fromCharCode(i) + '</a>';
        if (i + 1 < 91)
        {
            content += " "
        }
    }
    alphanumericLink.append("<h5>"+ content + "</h5>");

    content = "";
    for (let i = 0; i < 10; i++)
    {
        content += '<a href="movie-list.html?start=' + i + '">' + i + '</a>' + " ";
    }
    content += '<a href="movie-list.html?start=*">*</a>';
    alphanumericLink.append("<h5>"+ content + "</h5>");

}
// Event listener for input changes
/**
document.getElementById("search_query").addEventListener("input", function(event) {
    const query = event.target.value.trim();
    const results = search(query);
    displayResults(results);
}); **/

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/index", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});