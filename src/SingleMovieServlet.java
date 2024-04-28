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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
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

            // Get stars and movie information
            // Construct a queries with parameter represented by "?"
            String stars_query = "SELECT m.title, m.year, m.director, sim.starId, s.name " +
                    "from stars as s, stars_in_movies as sim, movies as m " +
                    "where m.id = sim.movieId and sim.starId = s.id and m.id = ? " +
                    "group by sim.starId order by count(sim.movieId)";

            String genre_query = "SELECT g.id, g.name from genres as g, genres_in_movies as gim " +
                    "where gim.genreId = g.id and gim.movieId = ?" +
                    "order by g.name asc";

            String rating_query = "SELECT rating from ratings " +
                    "where movieId = ?";

            String price_query = "SELECT price " +
                                 "FROM movie_prices mp " +
                                 "WHERE mp.movieId = ?";

            String insert_price = "INSERT INTO movie_prices VALUES(?, ?)";

            //getting price and setting if it doesn't exist
            PreparedStatement price_statement = conn.prepareStatement(price_query);
            price_statement.setString(1,id);
            ResultSet price_rs = price_statement.executeQuery();
            float price;
            if(!price_rs.next()){
                //calculate new price
                Random rand = new Random();
                int scale = 2;
                float result = (float)(1.5 + rand.nextFloat() * 8); //minimum price + random*price_range
                price = (float)(Math.round(result * Math.pow(10, scale)) / Math.pow(10, scale));

                //insert into movie_prices table
                PreparedStatement insert_statement = conn.prepareStatement(insert_price);
                insert_statement.setString(1,id);
                insert_statement.setFloat(2, price);
                insert_statement.executeUpdate();
            }
            else {
                price = price_rs.getFloat("price");
            }

            PreparedStatement statement = conn.prepareStatement(stars_query);
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            rs.next();
            JsonObject movieInfoObj = new JsonObject();

            // Convert row data into strings
            String movieTitle = rs.getString("title");
            String movieYear = rs.getString("year");
            String movieDirector = rs.getString("director");

            // Add data as properties of the object
            movieInfoObj.addProperty("movie_id", id);
            movieInfoObj.addProperty("movie_title", movieTitle);
            movieInfoObj.addProperty("movie_year", movieYear);
            movieInfoObj.addProperty("movie_director", movieDirector);
            movieInfoObj.addProperty("price", price);

            // Create array to store star objects
            JsonArray starsArray = new JsonArray();

            // Iterate through each row of rs
            do {
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject starObject = new JsonObject();
                String starId = rs.getString("starId");
                String starName = rs.getString("name");
                starObject.addProperty("star_id", starId);
                starObject.addProperty("star_name", starName);

                // Add star object to the array
                starsArray.add(starObject);
            } while (rs.next());

            // Add stars to movie object
            movieInfoObj.add("stars", starsArray);

            // Declare statement for genre_query
            statement = conn.prepareStatement(genre_query);
            statement.setString(1, id);
            rs = statement.executeQuery();

            // Create array to store genre objects
            JsonArray genreArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                String genreId = rs.getString("id");
                String genreName = rs.getString("name");
                jsonObject.addProperty("genre_id", genreId);
                jsonObject.addProperty("genre_name", genreName);

                // Add genre object to array
                genreArray.add(jsonObject);
            }

            // Add genres to movie object
            movieInfoObj.add("genres", genreArray);

            // Declare statement for rating_query
            statement = conn.prepareStatement(rating_query);
            statement.setString(1, id);
            rs = statement.executeQuery();

            // Create a JsonObject based on the data we retrieve from rs
            String rating = "N/A";
            if(rs.next())
                rating = rs.getString("rating");
            movieInfoObj.addProperty("rating", rating);

            // Close queries
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(movieInfoObj.toString());
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
