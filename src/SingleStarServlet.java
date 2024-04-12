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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
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
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Get stars and movie information
            // Construct a queries with parameter represented by "?"
            String movies_query = "SELECT s.name, s.id, s.birthYear, m.title, sim.movieId " +
                    "FROM stars as s, stars_in_movies as sim, movies as m " +
                    "WHERE s.id = sim.starId and sim.movieId = m.id and s.id = ?";

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(movies_query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            // Get first row of data
            rs.next();
            // Declare object for movie information
            JsonObject starInfoObj = new JsonObject();

            // Convert row data into strings
            String starName = rs.getString("name");
            String starBirth = rs.getString("birthYear");

            if (starBirth == null){
                starBirth = "N/A";
            }
            // Add data as properties of the object
            starInfoObj.addProperty("star_name", starName);
            starInfoObj.addProperty("star_birth", starBirth);

            // Create array to store star objects
            JsonArray moviesArray = new JsonArray();

            // Iterate through each row of rs
            do {
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject movieObject = new JsonObject();
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                movieObject.addProperty("movie_id", movieId);
                movieObject.addProperty("movie_title", movieTitle);

                // Add star object to the array
                moviesArray.add(movieObject);
            } while (rs.next());

            // Add stars to movie object
            starInfoObj.add("movies", moviesArray);

            // Close queries
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(starInfoObj.toString());
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
