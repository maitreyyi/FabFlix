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
    let movieTitleElement = jQuery("#movie_title");
    movieTitleElement.append("<h1>" + resultData["movie_title"] +
        " <span style='font-size: 50%'><i>(" + resultData["movie_year"] + ")</i></span></h1>");

    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Director: " + resultData["movie_director"] + "</p>" +
        "<p>Rating: " + resultData["rating"] + "</p>" + "<p>Price: $" + resultData["price"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    /** Populate the genre table */
    // Find the empty table body by id "genre_table_body"
    let genreTableBodyElement = jQuery("#genre_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";
    rowHTML += "<tr><th>";
    for (let i = 0; i < resultData["genres"].length; i++) {
        rowHTML += resultData["genres"][i]["genre_name"]
        if (i + 1 < resultData["genres"].length)
            rowHTML += ", ";
    }
    rowHTML += "</th><th>";

    for (let i = 0; i < resultData["stars"].length; i++) {
        // Add a link to single-star.html with id passed with GET url parameter
        rowHTML +=
        '<a href="single-star.html?id=' + resultData["stars"][i]['star_id'] + '">'
        + resultData["stars"][i]["star_name"] +     // display star_name for the link text
        '</a>'
        if (i + 1 < resultData["stars"].length)
            rowHTML += ", ";
    }

    rowHTML += "</th></tr>";

    // Append the row created to the table body, which will refresh the page
    genreTableBodyElement.append(rowHTML);

}

const backButton = document.getElementById("jumpback");
backButton.addEventListener("click", function(event) {
    // Prevent the default button click behavior
    event.preventDefault();
    window.location = `movie-list.html?session=true`
});

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