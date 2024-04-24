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

        // Retrieve parameter genre from url request.
        String genreParam = request.getParameter("genre");
        request.getServletContext().log("getting genre: " + genreParam);
        String startParam = request.getParameter("start");
        request.getServletContext().log("getting start: " + startParam);

        String titleParam = request.getParameter("title");
        request.getServletContext().log("getting start: " + titleParam);
        String yearParam = request.getParameter("year");
        request.getServletContext().log("getting start: " + yearParam);
        String directorParam = request.getParameter("director");
        request.getServletContext().log("getting start: " + directorParam);
        String starParam = request.getParameter("starName"); // check name
        request.getServletContext().log("getting start: " + starParam);

        String firstSort = request.getParameter("firstSort");
        request.getServletContext().log("getting start: " + firstSort);
        String secondSort = request.getParameter("secondSort");
        request.getServletContext().log("getting start: " + secondSort);

        String pageParam = request.getParameter("page");
        request.getServletContext().log("getting genre: " + pageParam);
        String limitParam = request.getParameter("limit");
        request.getServletContext().log("getting start: " + limitParam);

        int offset = Integer.parseInt(pageParam) * Integer.parseInt(limitParam);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Get stars and movie information (hyperlinks)
            // Construct a queries with parameter represented by "?"
            String movie_query = "SELECT m.id, m.title, m.year, m.director, r.rating " +
                    "FROM movies as m, ratings as r ";
            if (genreParam != null) {
                // If there is a genre parameter
                movie_query += ", genres_in_movies as gim " +
                        "WHERE r.movieId = m.id and gim.movieId = m.id and gim.genreId = ? ";
            }
            else if (startParam != null) {
                if (startParam.equals("*")) {
                    startParam = "[^a-zA-Z0-9]";
                    movie_query += "WHERE r.movieId = m.id and m.title not like ? ";
                }
                else {
                    movie_query += "WHERE r.movieId = m.id and m.title like ? ";
                }
            }
            else {
                // If there is no parameter
                movie_query += "WHERE r.movieId = m.id ";

            }

            // Assign sorting
            if (firstSort == null || secondSort == null) {
                // Default sorting
                movie_query += "ORDER BY m.title DESC, r.rating DESC ";
            }
            else if (firstSort.startsWith("title")) {
                // First sort by title
                if (firstSort.endsWith("DESC"))
                    movie_query += "ORDER BY m.title DESC, ";
                else
                    movie_query += "ORDER BY m.title ASC, ";
                // Break ties with rating
                if (secondSort.endsWith("DESC"))
                    movie_query += "r.rating DESC ";
                else
                    movie_query += "r.rating ASC ";
            }
            else {
                // First sort with rating
                if (firstSort.endsWith("DESC"))
                    movie_query += "ORDER BY r.rating DESC, ";
                else
                    movie_query += "ORDER BY r.rating ASC, ";
                // Break ties with title
                if (secondSort.endsWith("DESC"))
                    movie_query += "m.title DESC ";
                else
                    movie_query += "m.title ASC ";
            }

            movie_query += "LIMIT ? OFFSET ?";

            String stars_query = "SELECT sim.starId, s.name " +
                    "from stars as s, stars_in_movies as sim " +
                    "where sim.starId = s.id and sim.movieId = ? " +
                    "group by sim.starId order by count(sim.movieId)" +
                    "limit 3";

            String genre_query = "SELECT g.id, g.name from genres as g, genres_in_movies as gim " +
                    "where gim.genreId = g.id and gim.movieId = ? " +
                    "order by g.name asc " +
                    "limit 3";

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(movie_query);
            // Perform the query
            int index = 1;
            if (genreParam != null) {
                statement.setInt(index, Integer.parseInt(genreParam));
                index++;
            }
            if (startParam != null) {
                statement.setString(index, startParam + "%");
                index++;
            }
            if (limitParam != null) {
                statement.setInt(index, Integer.parseInt(limitParam));
                index++;
            }
            if (pageParam != null) {
                statement.setInt(index, offset);
                index++;
            }

            ResultSet rs = statement.executeQuery();
            // Declare object for movie information
            JsonArray movieArray = new JsonArray();

            //set up movies objects
            while(rs.next()) {
                JsonObject movieObj = new JsonObject();

                // Convert row data into strings
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String rating = rs.getString("rating");

                // Add data as properties of the object
                movieObj.addProperty("movie_id", movieId);
                movieObj.addProperty("movie_title", movieTitle);
                movieObj.addProperty("movie_year", movieYear);
                movieObj.addProperty("movie_director", movieDirector);
                movieObj.addProperty("rating", rating);

                movieArray.add(movieObj);

                JsonArray genreArray = new JsonArray();
                statement = conn.prepareStatement(genre_query);
                statement.setString(1, movieId);
                ResultSet genre_rs = statement.executeQuery();

                while(genre_rs.next()){
                    JsonObject genre = new JsonObject();

                    String genreId = genre_rs.getString("id");
                    String genreName = genre_rs.getString("name");
                    genre.addProperty("genre_id", genreId);
                    genre.addProperty("genre_name", genreName);

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
                    String star_id = stars_rs.getString("starId");
                    String star_name = stars_rs.getString("name");

                    starObj.addProperty("star_id", star_id);
                    starObj.addProperty("star_name", star_name);
                    starsArray.add(starObj);
                }

                movieObj.add("stars", starsArray);
                stars_rs.close();
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
