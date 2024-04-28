
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
        rowHTML += '<tr><th><a class="link-underline link-underline-opacity-0 link-underline-opacity-100-hover" href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_title"] + '</a></th>' +
            "<th>" + resultData[i]["movie_year"] + "</th>" +
            "<th>" + resultData[i]["movie_director"] + "</th>" +
            "<th>" + resultData[i]["rating"] + "</th>";

        rowHTML += "<th>";
        // Concatenate the html tags with resultData jsonObject to create table rows
        for (let j = 0; j < Math.min(3,resultData[i]["genres"].length); j++) {
            rowHTML += '<a class="link-underline link-underline-opacity-0 link-underline-opacity-100-hover" href="movie-list.html?genre=' + resultData[i]["genres"][j]["genre_id"] +'">' +
                        resultData[i]["genres"][j]["genre_name"] + '</a>';
            if (resultData[i]["genres"][j+1] != null) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>";
        // Concatenate the html tags with resultData jsonObject to create table rows
        for (let j = 0; j < Math.min(3, resultData[i]["stars"].length); j++) {
            rowHTML += '<a class="link-underline link-underline-opacity-0 link-underline-opacity-100-hover" href="single-star.html?id=' + resultData[i]["stars"][j]['star_id'] + '">'
                + resultData[i]["stars"][j]["star_name"] +'</a>';
            if (j < 2) {
                rowHTML += ", ";
            }
        }
        rowHTML += "<th>" + "$" + resultData[i]["price"].toFixed(2) + "</th>";
        rowHTML +=  '<th><button type="button" class="add-to-cart btn btn-secondary" fdprocessedid="9yh6qp" data-movie-id=' + resultData[i]["movie_id"] + '> ' +
            '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bag-plus" viewBox="0 0 16 16">' +
            '<path fill-rule="evenodd" d="M8 7.5a.5.5 0 0 1 .5.5v1.5H10a.5.5 0 0 1 0 1H8.5V12a.5.5 0 0 1-1 0v-1.5H6a.5.5 0 0 1 0-1h1.5V8a.5.5 0 0 1 .5-.5"></path>' +
            '<path d="M8 1a2.5 2.5 0 0 1 2.5 2.5V4h-5v-.5A2.5 2.5 0 0 1 8 1m3.5 3v-.5a3.5 3.5 0 1 0-7 0V4H1v10a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V4zM2 5h12v9a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1z"></path>'+
            '</svg>'+
            '</button></th>';

        rowHTML += "</th></tr>";
        moviesTableBodyElement.append(rowHTML);

    }

    const params = (new URL(document.location)).searchParams;
    let cur_page = (params.get("page")) ? params.get("page") : "1";
    let limit = (params.get("limit")) ? params.get("limit") : "10";

    if (resultData.length === 0 || cur_page * limit >= resultData[0]["count"]) {
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
            action: 'add'
        },
        url: 'api/cart', // Setting request url,
        success: function(response){
            console.log(response);
        }
    });
}
function showAddCartMsg() {
    var msg = document.getElementById('add-cart-msg');
    msg.textContent = 'Your movie has been added to Cart';
    msg.style.display = 'block';

    setTimeout(function() {
        msg.style.display='none';}, 1700);
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
    localStorage.setItem('sort', document.getElementById("ordering").value)
    localStorage.setItem('per', document.getElementById("per-page").value)
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

if (localStorage.getItem('sort')) {
    document.getElementById('ordering').value = localStorage.getItem('sort');
}

if (localStorage.getItem('per')) {
    document.getElementById('per-page').value = localStorage.getItem('per');
}

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

    //create url for redirection using form data
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

});

const params = (new URL(document.location)).searchParams;
const movie_url= `api/movie-list?${params.toString()}`;
sessionStorage.setItem('main_url',`movie-list.html?${params.toString()}`);
/*
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Assuming jQuery is already loaded
$(document).ready(function() {
    $(document).on("click", ".add-to-cart", function() {
        var movieId = $(this).data("movie-id");
        addCart(movieId);
        showAddCartMsg();

    });
});


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: movie_url, // Setting request url,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});