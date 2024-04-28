import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/order-placed")
public class ConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        User user = (User) request.getSession().getAttribute("user");
        Map<String, Integer> cart = user.getCart();

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Get stars and movie information
            // Construct a queries with parameter represented by "?"
            String sales_query = "SELECT id, movieId, quantity " +
                    "FROM sales " +
                    "ORDER BY id DESC " +
                    "LIMIT ?";

            String movie_query = "SELECT m.title, mp.price " +
                    "FROM movies as m, movie_prices as mp " +
                    "WHERE m.id = mp.movieId AND m.id = ?";

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(sales_query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setInt(1, cart.size());

            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonArray sales = new JsonArray();

            while(rs.next()) {
                // Declare object for movie information
                JsonObject saleInfo = new JsonObject();

                // Convert row data into strings
                int salesId = rs.getInt("id");
                String movieId = rs.getString("movieId");
                int quantity = rs.getInt("quantity");

                PreparedStatement movie_statement = conn.prepareStatement(movie_query);
                movie_statement.setString(1, movieId);

                // Perform the query
                ResultSet movie_rs = movie_statement.executeQuery();
                movie_rs.next();
                String title = movie_rs.getString("title");
                float price = movie_rs.getFloat("price");

                saleInfo.addProperty("salesId", salesId);
                saleInfo.addProperty("title", title);
                saleInfo.addProperty("quantity", quantity);
                saleInfo.addProperty("price", price);

                // Add stars to movie object
                sales.add(saleInfo);

                // Close queries
                movie_rs.close();
                movie_statement.close();
            }

            // Close queries
            rs.close();
            statement.close();

            // Clear cart
            // user.clearCart();

            // Write JSON string to output
            out.write(sales.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
