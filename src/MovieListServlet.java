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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/movie-list"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
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
            String movie_query = "SELECT m.title, m.year, m.director, r.rating " +
                    "from movies as m, ratings as r " +
                    "where r.movieId = ?";

            String stars_query = "SELECT sim.starId, s.name " +
                    "from stars as s, stars_in_movies as sim " +
                    "where sim.starId = s.id and sim.movieId = ? " +
                    "limit 3";

            String genre_query = "SELECT g.name from genres as g, genres_in_movies as gim " +
                    "where gim.genreId = g.id and gim.movieId = ? " +
                    "limit 3";

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(movie_query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            // Declare object for movie information
            JsonArray movieArray = new JsonArray();

            while(rs.next())
            {
                JsonObject movieObj = new JsonObject();

                // Convert row data into strings
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String rating = rs.getString("rating");

                // Add data as properties of the object
                movieObj.addProperty("movie_title", movieTitle);
                movieObj.addProperty("movie_year", movieYear);
                movieObj.addProperty("movie_director", movieDirector);
                movieObj.addProperty("rating", rating);

                JsonArray genreArray = new JsonArray();
                statement = conn.prepareStatement(genre_query);
                statement.setString(1, movieId);
                ResultSet genre_rs = statement.executeQuery();

                while(genre_rs.next()){
                    String genre = genre_rs.getString("id");
                    genreArray.add(genre);
                }

                movieObj.add("genres", genreArray);
                genre_rs.close();

                JsonArray starsArray = new JsonArray();
                statement = conn.prepareStatement(stars_query);
                statement.setString(1, movieId);
                ResultSet stars_rs = statement.executeQuery();

                while(stars_rs.next()){
                    JsonObject starObj = new JsonObject();
                    String star_id = stars_rs.getString("id");
                    String star_name = stars_rs.getString("id");

                    starObj.addProperty("star_id", star_id);
                    starObj.addProperty("star_name", star_name);
                    starsArray.add(starObj);
                }

                movieObj.add("stars", starsArray);
                stars_rs.close();

                movieArray.add(movieObj);
            }

            // Close queries
            rs.close();
            statement.close();

            // Write JSON string to output
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

}
