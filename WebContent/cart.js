function handleResult(resultData) {
    let moviesTableBodyElement = jQuery("#cart_table_body");
    let total_price = 0;
    console.log("handleResult: populating cart");

    // append two html <p> created to the h3 body, which will refresh the page
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        //title
        rowHTML += '<tr><th><a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_title"] + '</a></th>';
        //price
        rowHTML += "<th>" + "$" + resultData[i]["price"] + "</th>";
        //quantity
        rowHTML += "<th><button class = 'decreaseQuantity' data-movie-id='" + resultData[i]["movie_id"] + "'> - </button> " + resultData[i]["quantity"] + " <button class = 'increaseQuantity' data-movie-id='" + resultData[i]["movie_id"] + "'> + </button></th>";

        rowHTML += "</tr>";
        moviesTableBodyElement.append(rowHTML);
        total_price += parseFloat(resultData[i]["price"])*parseFloat(resultData[i]["quantity"]);
    }
    let totalPriceElem = jQuery("#total-price");
    totalPriceElem.append("<h3>Total price: $" + total_price.toFixed(2) + "</h3>")
    totalPriceElem.append("<a href = './payment.html' class='btn btn-primary btn-lg' role='button' >Continue to checkout</a>" );

}
//getting cart results
const getCart = async () => {
    const data = await fetch(
        `api/cart`,
        {
            method: "GET",
            headers: {
                'Content-Type': 'application/json'
            }
        }
    )
    return data.json();
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

function removeCart(movieId) {
    //send data along to shopping servlet (add cart info)
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "POST",// Setting request method
        data: {
            movieId: movieId,
            add: 'False'
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

$(document).ready(function() {
    $(document).on("click", ".increaseQuantity", function() {
        var movieId = $(this).data("movie-id");
        addCart(movieId);
        location.reload();
    });
    $(document).on("click", ".decreaseQuantity", function() {
        var movieId = $(this).data("movie-id");
        removeCart(movieId);
        location.reload();
    });


});

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/cart", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});