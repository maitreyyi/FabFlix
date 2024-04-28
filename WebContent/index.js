/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {

    console.log("handleResult: populating genre hyperlinks");

    // populate the alphabet browsing
    let genreLink = jQuery("#genres");
    let content = "";
    for (let i = 0; i < resultData.length; i++)
    {
        content += '<a class="link-underline link-underline-opacity-0 link-underline-opacity-100-hover" href="movie-list.html?genre='
                    + resultData[i]["genre_id"] + '">' + resultData[i]["genre_name"] + '</a>';
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
        content += '<a class="link-underline link-underline-opacity-0 link-underline-opacity-100-hover" href="movie-list.html?start='
                    + String.fromCharCode(i) + '">' + String.fromCharCode(i) + '</a>';
        if (i + 1 < 91)
        {
            content += " "
        }
    }
    alphanumericLink.append("<h5>"+ content + "</h5>");

    content = "";
    for (let i = 0; i < 10; i++)
    {
        content += '<a class="link-underline link-underline-opacity-0 link-underline-opacity-100-hover" href="movie-list.html?start=' + i + '">' + i + '</a>' + " ";
    }
    content += '<a class="link-underline link-underline-opacity-0 link-underline-opacity-100-hover" href="movie-list.html?start=*">*</a>';
    alphanumericLink.append("<h5>"+ content + "</h5>");

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
    sendSearch(searchData);

});

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/index", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});
