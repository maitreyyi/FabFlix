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

    console.log("handleResult: populating movie info from resultData");

    // populate the movie info h3
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<h1>"+resultData["star_name"] + "</h1>" +
        "<p style='font-size: 50%'><i> Born: " + resultData["star_birth"] + "</i></p></h1>");

    console.log("handleResult: populating star table from resultData");

    /** Populate the movies table */
    let moviesTableBodyElement = jQuery("#movie_table_body");
    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData["movies"].length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData["movies"][i]['movie_id'] + '">'
            + resultData["movies"][i]["movie_title"] +     // display movie_title for the link text
            '</a>' +
            "</th>";
        rowHTML += "</tr>";

        moviesTableBodyElement.append(rowHTML);
    }
}
const sendSearch = searchData => {
    let queryString = '';
    for (const key in searchData) {
        if (searchData.hasOwnProperty(key)) {
            if (queryString.length > 0) {
                queryString += '&';
            }
            queryString += key + '=' + encodeURIComponent(searchData[key]);
        }
    }
    window.location = `movie-list.html?${queryString}`
}

const backButton = document.getElementById("jumpback");
backButton.addEventListener("click", function(event) {
    // Prevent the default button click behavior
    event.preventDefault();
    window.location = sessionStorage.getItem('main_url');
});


// Get id from URL
let starId = getParameterByName('id');
let movieId = getParameterByName('id');

const form = document.getElementById("search");
const submitButton = form.querySelector("button[type='submit']");
submitButton.addEventListener("click", function(event) {
    // Prevent the default button click behavior
    event.preventDefault();
    const searchData = {}

    //Extract the form data
    const formData = new FormData(form);
    const title = formData.get("title");
    const year = formData.get("year");
    const director = formData.get("director");
    const starName = formData.get("star_name");

    if(title && title.length > 0){
        searchData['title'] = title;
    }
    if(year){
        searchData['year'] = year;
    }
    if(director && director.length > 0){
        searchData['director'] = director;
    }
    if(starName && starName.length > 0){
        searchData['star_name'] = starName;
    }

    //create url using form data and redirect user
    let queryString = '';
    for (const key in searchData) {
        if (searchData.hasOwnProperty(key)) {
            if (queryString.length > 0) {
                queryString += '&';
            }
            queryString += key + '=' + encodeURIComponent(searchData[key]);
        }
    }
    window.location = `movie-list.html?${queryString}`;

});

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});