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
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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

            JsonArray jsonArray = new JsonArray();

            // Get stars and movie information
            // Construct a query with parameter represented by "?"
            String stars_query = "SELECT * from stars as s, stars_in_movies as sim, movies as m " +
                    "where m.id = sim.movieId and sim.starId = s.id and m.id = ?";

            String genre_query = "SELECT g.name from genres as g, genres_in_movies as gim " +
                    "where gim.genreId = g.id and gim.movieId = ?";

            String rating_query = "SELECT rating from ratings " +
                    "where movieId = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(stars_query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            rs.next();

            String movieId = rs.getString("movieId");
            String movieTitle = rs.getString("title");
            String movieYear = rs.getString("year");
            String movieDirector = rs.getString("director");

            JsonObject movieInfoObj = new JsonObject();
            movieInfoObj.addProperty("movie_id", movieId);
            movieInfoObj.addProperty("movie_title", movieTitle);
            movieInfoObj.addProperty("movie_year", movieYear);
            movieInfoObj.addProperty("movie_director", movieDirector);

            jsonArray.add(movieInfoObj);

            JsonArray starsArray = new JsonArray();

            // Iterate through each row of rs
            do {

                String starId = rs.getString("starId");
                String starName = rs.getString("name");

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_id", starId);
                jsonObject.addProperty("star_name", starName);

                starsArray.add(jsonObject);
            } while (rs.next());

            JsonObject starsObject = new JsonObject();
            starsObject.add("stars", starsArray);
            jsonArray.add(starsObject);

            statement = conn.prepareStatement(genre_query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);
            rs = statement.executeQuery();

            JsonArray genreArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String genreName = rs.getString("name");

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genre_name", genreName);

                genreArray.add(jsonObject);
            }

            JsonObject genreObject = new JsonObject();
            genreObject.add("genres", genreArray);
            jsonArray.add(genreObject);

            statement = conn.prepareStatement(rating_query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            rs = statement.executeQuery();

            rs.next();

            String rating = rs.getString("rating");

            JsonObject ratingObj = new JsonObject();
            ratingObj.addProperty("rating", rating);

            jsonArray.add(ratingObj);

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
