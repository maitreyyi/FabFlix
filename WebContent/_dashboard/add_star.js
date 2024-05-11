/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */


function handleResult(resultData) {
}
const form = document.getElementById("add_star");
const submitButton = form.querySelector("button[type='submit']");
submitButton.addEventListener("click", function(event) {
    // Prevent the default button click behavior
    event.preventDefault();

    //Extract the form data
    const formData = new FormData(form);
    const star_name = formData.get("star_name");
    const year = formData.get("birth_year");

    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "POST",// Setting request method
        data: {
            star_name: star_name,
            birthyear: year
        },
        url: 'api/add-star', // Setting request url,
        success: function(response){
            console.log(response);
        },
        error: function(xhr, status, error) {
            // Handle error response
            console.log(error);
        }
    });

});

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "../api/add_star", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});