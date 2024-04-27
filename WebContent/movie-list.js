
const sort = document.getElementById("sort");
const submitSort = sort.querySelector("button[type='submit']");
const prev_page = document.getElementById("prev");
const next_page = document.getElementById("next");


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
            + resultData[i]["movie_title"] + '   </a><button class = "add-to-cart" data-movie-id=' + resultData[i]["movie_id"] + '>Add</button></th>' +
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
        rowHTML += "<th>" + "$" + resultData[i]["price"] + "</th>";
        rowHTML += "</th></tr>";
        moviesTableBodyElement.append(rowHTML);
    }

    const params = (new URL(document.location)).searchParams;
    let cur_page = (params.get("page")) ? params.get("page") : "1";
    let limit = (params.get("limit")) ? params.get("limit") : "10";

    if (resultData.length > 0 && cur_page * limit >= resultData[0]["count"]) {
        next_page.setAttribute('disabled', '');
    }

    if (cur_page === "1") {
        prev_page.setAttribute('disabled', '');
    }

}

function addCart(movieId) {
    //send data along to shopping servlet (add cart info)
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "POST",// Setting request method
        data: {
            movieId: movieId,
            add: 'True'
        },
        url: 'api/cart', // Setting request url,
        success: function(response){
            console.log(response);
        },
        error: function(xhr, status, error) {
            // Handle error response
            console.log(error);
        }
    });
}
const sendSort = searchData => {
    let queryString = '';
    const params = (new URL(document.location)).searchParams;
    for (const key of params.keys()) {
        if (key !== "page" && key !== "session" && !searchData[key]) {
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


prev_page.addEventListener("click", function(event) {
    // Prevent the default button click behavior
    event.preventDefault();
    let queryString = '';
    const params = (new URL(document.location)).searchParams;
    let cur_page = (params.get("page")) ? params.get("page") : "1";

    for (const key of params.keys()) {
        if (key !== "page" && key !== "session") {
            if (queryString.length > 0) {
                queryString += '&';
            }
            queryString += key + '=' + encodeURIComponent(params.get(key));
        }
    }
    queryString += "&page" + '=' + encodeURIComponent(parseInt(cur_page) - 1);

    window.location = `movie-list.html?${queryString}`;
});

next_page.addEventListener("click", function(event) {
    // Prevent the default button click behavior
    event.preventDefault();
    let queryString = '';
    const params = (new URL(document.location)).searchParams;
    let cur_page = (params.get("page")) ? params.get("page") : "1";

    for (const key of params.keys()) {
        if (key !== "page" && key !== "session") {
            if (queryString.length > 0) {
                queryString += '&';
            }
            queryString += key + '=' + encodeURIComponent(params.get(key));
        }
    }
    queryString += "&page" + '=' + encodeURIComponent(parseInt(cur_page) + 1);

    window.location = `movie-list.html?${queryString}`;
});

const params = (new URL(document.location)).searchParams;
const movie_url= `api/movie-list?${params.toString()}`;
/*
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Assuming jQuery is already loaded
$(document).ready(function() {
    console.log('inside document');
    $(document).on("click", ".add-to-cart", function() {
        console.log('click logged');
        var movieId = $(this).data("movie-id");
        addCart(movieId);
    });
});


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: movie_url, // Setting request url,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});