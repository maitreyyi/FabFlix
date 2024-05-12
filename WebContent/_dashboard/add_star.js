/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData){
    var msg = document.getElementById('add-star-msg');
    msg.textContent = resultData["status"];
    msg.style.display = 'block';

    setTimeout(function() {
        msg.style.display='none';}, 1700);
}
$(document).ready(function() {
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
            url: '../api/add-star', // Setting request url,
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

