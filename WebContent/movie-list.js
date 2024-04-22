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
    let moviesTableBodyElement = jQuery("#movie_table_body");

    console.log("handleResult: populating movie table from resultData");

    // append two html <p> created to the h3 body, which will refresh the page
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += '<tr><th><a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_title"] + '</a></th>' +
            "<th>" + resultData[i]["movie_year"] + "</th>" +
            "<th>" + resultData[i]["movie_director"] + "</th>" +
            "<th>" + resultData[i]["rating"] + "</th>";

        rowHTML += "<th>";
        // Concatenate the html tags with resultData jsonObject to create table rows
        for (let j = 0; j < 3; j++) {
            if(resultData[i]["genres"][j] == null){
                break;
            }
            rowHTML += resultData[i]["genres"][j];

            if (resultData[i]["genres"][j+1] != null) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>";
        // Concatenate the html tags with resultData jsonObject to create table rows
        for (let j = 0; j < 3; j++) {
            rowHTML += '<a href="single-star.html?id=' + resultData[i]["stars"][j]['star_id'] + '">'
                + resultData[i]["stars"][j]["star_name"] +'</a>';
            if (j < 2) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th></tr>";
        moviesTableBodyElement.append(rowHTML);
    }
}

const handleSearch = async ()=> {
    //console.log('handling search');
    data = await fetch(
        `api/search?${params.toString()}`,
        {
            method:"GET",
            headers:{
                'Content-Type': 'application/json'
            }
        }
    )
    console.log(data);
    return data;
}

window.onload = async event => {
    params = (new URL(document.location)).searchParams;
    handleSearch();
    console.log('search handled');
}
/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movie-list", // Setting request url,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});