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
import java.util.Random;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/movie-list"
@WebServlet(name = "GenreBrowseServlet", urlPatterns = "/api/index")
public class GenreBrowseServlet extends HttpServlet {
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
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Get stars and movie information (hyperlinks)
            // Construct a queries with parameter represented by "?"
            String genre_query = "SELECT * from genres";

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(genre_query);
            // Perform the query
            ResultSet rs = statement.executeQuery();
            // Declare object for movie information
            JsonArray genreArray = new JsonArray();

            //set up movies objects
            while (rs.next()) {
                JsonObject genreObj = new JsonObject();

                // Convert row data into strings
                String genreId = rs.getString("id");
                String genreName = rs.getString("name");

                // Add data as properties of the object
                genreObj.addProperty("genre_id", genreId);
                genreObj.addProperty("genre_name", genreName);

                genreArray.add(genreObj);
            }

            // Close queries
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(genreArray.toString());
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
    }
}