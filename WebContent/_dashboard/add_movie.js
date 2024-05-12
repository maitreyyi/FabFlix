/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData){
    var msg = document.getElementById('add-movie-msg');
    msg.textContent = resultData["status"];
    msg.style.display = 'block';

    setTimeout(function() {
        msg.style.display='none';}, 2100);
}
$(document).ready(function() {
    const form = document.getElementById("add_movie");
    const submitButton= form.querySelector("button[type='submit']");

    submitButton.addEventListener("click", function(event) {
        // Prevent the default button click behavior
        event.preventDefault();

        //Extract the form data
        const formData = new FormData(form);
        const title = formData.get("movie_title");
        const year = formData.get("movie_year");
        const director = formData.get("movie_director");
        const star = formData.get("movie_star");
        const genre = formData.get("movie_genre");


        jQuery.ajax({
            dataType: "json",  // Setting return data type
            method: "POST",// Setting request method
            data: {
                title: title,
                year: year,
                director: director,
                star: star,
                genre: genre
            },
            url: '../api/add-movie', // Setting request url,
            success: function(resultData) {
                handleResult(resultData);
                console.log(resultData);
            },
            error: function(xhr, status, error) {
                // Handle error response
                console.log(error);
            }
        });

    });
});

