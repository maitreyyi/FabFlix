/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("Autocomplete initiated");

    // Check if cached data exists
    if (localStorage.getItem("autocomplete")) {
        // Get mapping
        let map = new Map(JSON.parse(localStorage.getItem("autocomplete")));
        // If entry exists in mapping
        if (map.has(query)) {
            console.log("Using cached data from frontend localStorage");
            handleLookupAjaxSuccess(map.get(query), query, doneCallback);
            return;
        }
    }

    console.log("Sending AJAX request to backend Java Servlet");
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/autocomplete?query=" + encodeURI(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log(data);

    // parse the string into JSON
    // var jsonData = JSON.parse(data);
    // console.log(data)

    // Cache results if needed
    let input = document.getElementById("autocomplete").value;

    // If autocomplete variable already exists
    if (localStorage.getItem("autocomplete")) {
        // Get mapping
        let map = new Map(JSON.parse(localStorage.getItem("autocomplete")));
        // If entry for input does not exist, add it to the mapping
        if (!map.has(input)) {
            map.set(input, data);
            localStorage.setItem("autocomplete", JSON.stringify(Array.from(map.entries())));
        }
    }
    // If autocomplete variable does not exist
    else {
        // Create new map, add data and save to storage
        let map = new Map();
        map.set(input, data);
        localStorage.setItem("autocomplete", JSON.stringify(Array.from(map.entries())));
    }


    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // Jump to the specific result page based on the selected suggestion
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"])
    window.location.replace("single-movie.html?id=" + suggestion["data"]["movieId"]);

}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // Set minimum characters to 3
    minChars: 3
});