/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Title: " + resultData[0]["movie_title"] + "</p>" +
        "<p>Release Year: " + resultData[0]["movie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movie_title"] + "</p>" +
        "<p>Rating: " + resultData[0]["movie_id"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the genre table
    // Find the empty table body by id "genre_table_body"
    let genreTableBodyElement = jQuery("#genre_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData[2]["genres"].length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[2]["genres"][i]["genre_name"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        genreTableBodyElement.append(rowHTML);
    }
    let starsTableBodyElement = jQuery("#stars_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData[1]["stars"].length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-star.html?id=' + resultData[1]["stars"][i]['star_id'] + '">'
            + resultData[1]["stars"][i]["star_name"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starsTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});