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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

//NEED TO TEST DISPLAY CART FUNCTION AND FINISH WRITING THAT
// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/movie-list"
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
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

        User user = (User) request.getSession().getAttribute("user");
        String session = request.getParameter("session");
        Map<String, Integer> cart = user.cart;

        if (cart == null) {
            cart = new TreeMap<>();
        }
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String title_query = "SELECT DISTINCT m.id, m.title " +
                    "FROM movies m " +
                    "JOIN movie_prices AS mp ON m.id = mp.movieId " +
                    "WHERE mp.movieId = ?";

            String price_query = "SELECT mp.price " +
                    "FROM movie_prices mp " +
                    "WHERE mp.movieId = ? ";

            //set up movies objects
            JsonArray movieArray = new JsonArray();

            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                String movie_id = entry.getKey();
                Integer quantity = entry.getValue();

                JsonObject movieObj = new JsonObject();

                PreparedStatement statement = conn.prepareStatement(title_query);
                PreparedStatement price_statement = conn.prepareStatement(price_query);

                statement.setString(1, movie_id);
                price_statement.setString(1, movie_id);

                ResultSet rs = statement.executeQuery();
                ResultSet price_rs = price_statement.executeQuery();

                if (rs.next() && price_rs.next()) {
                    // Convert row data into strings
                    String movieTitle = rs.getString("title");
                    String price = price_rs.getString("price");

                    // Add data as properties of the object
                    movieObj.addProperty("movie_id", movie_id);
                    movieObj.addProperty("movie_title", movieTitle);
                    movieObj.addProperty("quantity", quantity);
                    movieObj.addProperty("price", price);

                    rs.close();
                    price_statement.close();
                    movieArray.add(movieObj);
                }

            }
            out.write(movieArray.toString());
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        try{
            User user = (User) request.getSession().getAttribute("user");
            Map<String, Integer> cart = user.cart;

            if (cart == null) {
                cart = new TreeMap<>();
            }

            String movie_id = request.getParameter("movieId");
            String add = request.getParameter("add");
            //try
            if(add != null && add.equals("True")){
                user.addToCart(movie_id);
            } else{
                user.removeFromCart(movie_id);
            }

        } catch (Exception e) {
            // write error message JSON object to output

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("error message", e.toString());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
            out.close();
        } finally {
            out.close();
        }
    }
}
