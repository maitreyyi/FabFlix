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
import java.util.Random;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/movie-list"
@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        Random random = new Random();
        try {
            if(random.nextBoolean()){
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slavedb");
            } else {
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
            }
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

        // The log message can be found in localhost log
        request.getServletContext().log("getting parameters: ");
        String query = request.getParameter("query");

        // setup the response json arrray
        JsonArray jsonArray = new JsonArray();
        PrintWriter out = response.getWriter();

        if (query == null || query.trim().isEmpty()) {
            response.getWriter().write(jsonArray.toString());
            return;
        }

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Get stars and movie information (hyperlinks)
            // Construct a queries with parameter represented by "?"
            String movie_query = "SELECT DISTINCT id, title, year " +
                    "FROM movies " +
                    "WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE) " +
                    "LIMIT 10";
            query = query.trim();
            query = query.replace(" ", "* +");
            query = "+" + query + "*";

            // Declare statement for movie_query
            PreparedStatement statement = conn.prepareStatement(movie_query);
            statement.setString(1, query);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            //set up movies objects
            while(rs.next()) {
                JsonObject movieObj = new JsonObject();

                // Convert row data into strings
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");

                JsonObject dataObj = new JsonObject();
                dataObj.addProperty("movieId", movieId);

                // Add data as properties of the object
                movieObj.addProperty("value", movieTitle + " (" + movieYear + ")");
                movieObj.add("data", dataObj);

                jsonArray.add(movieObj);
            }

            // Close queries
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
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
