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
        for (let j = 0; j < Math.min(3,resultData[i]["genres"].length); j++) {
            rowHTML += '<a href="movie-list.html?genre=' + resultData[i]["genres"][j]["genre_id"] +'">' +
                        resultData[i]["genres"][j]["genre_name"] + '</a>';
            if (resultData[i]["genres"][j+1] != null) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>";
        // Concatenate the html tags with resultData jsonObject to create table rows
        for (let j = 0; j < Math.min(3, resultData[i]["stars"].length); j++) {
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


const sendSort = searchData => {
    let queryString = '';
    const params = (new URL(document.location)).searchParams;
    for (const key of params.keys()) {
        if (key !== "page" && !searchData[key]) {
            if (queryString.length > 0) {
                queryString += '&';
            }
            queryString += key + '=' + encodeURIComponent(params.get(key));
        }
    }

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

const sort = document.getElementById("sort");
const submitSort = sort.querySelector("button[type='submit']");

submitSort.addEventListener("click", function(event) {
    // Prevent the default button click behavior
    event.preventDefault();
    const searchData = {}

    //Extract the form data
    const formData = new FormData(sort);
    const sorting = formData.get("ordering");
    const limit = formData.get("per-page");

    const firstSort = sorting.split(",")[0];
    const secondSort = sorting.split(",")[1];

    if(firstSort){
        searchData['firstSort'] = firstSort;
    }
    if(secondSort){
        searchData['secondSort'] = secondSort;
    }
    if(limit){
        searchData['limit'] = limit;
    }
    sendSort(searchData);

});

const params = (new URL(document.location)).searchParams;
const movie_url= `api/movie-list?${params.toString()}`;
/*
 * Once this .js is loaded, following scripts will be executed by the browser\
 */


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: movie_url, // Setting request url,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});