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

        User user = (User) request.getSession().getAttribute("user");
        String session = request.getParameter("session");
        String genre, start, title, year, director, star_name, firstSort, secondSort, page, limit;

        if (session != null){
            Map<String, String> parameters = user.getParam();

            genre = parameters.get("genre");
            start = parameters.get("start");

            title = parameters.get("title");
            year = parameters.get("year");
            director = parameters.get("director");
            star_name = parameters.get("star_name");

            firstSort = parameters.get("firstSort");
            secondSort = parameters.get("secondSort");

            page = parameters.get("page");
            limit = parameters.get("limit");
        }
        else {
            // Retrieve parameter genre from url request.
            genre = (request.getParameter("genre") != null) ? request.getParameter("genre") : "%";
            start = (request.getParameter("start") != null) ? request.getParameter("start") : "%";

            title = (request.getParameter("title") != null) ? request.getParameter("title") : "%";
            year = (request.getParameter("year") != null) ? request.getParameter("year") : "%";
            director = (request.getParameter("director") != null) ? request.getParameter("director") : "%";
            star_name = (request.getParameter("star_name") != null) ? request.getParameter("star_name") : "%";

            firstSort = (request.getParameter("firstSort") != null) ? request.getParameter("firstSort") : "titleASC";
            secondSort = (request.getParameter("secondSort") != null) ? request.getParameter("secondSort") : "ratingDESC";

            page = (request.getParameter("page") != null) ? request.getParameter("page") : "1";
            limit = (request.getParameter("limit") != null) ? request.getParameter("limit") : "10";
        }
        int offset = (Integer.parseInt(page) - 1) * Integer.parseInt(limit);

        // The log message can be found in localhost log
        request.getServletContext().log("getting parameters: " + title);

        // Save parameters in session
        user.addParam("genre", genre);
        user.addParam("start",start);
        user.addParam("title",title);
        user.addParam("year",year);
        user.addParam("director",director);
        user.addParam("star_name",star_name);
        user.addParam("firstSort",firstSort);
        user.addParam("secondSort",secondSort);
        user.addParam("page",page);
        user.addParam("limit",limit);

        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Get stars and movie information (hyperlinks)
            // Construct a queries with parameter represented by "?"
            String movie_query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
                    "FROM movies as m " +
                    "JOIN ratings AS r ON m.id = r.movieId ";

            String count_query = "SELECT count(DISTINCT m.id) as count " +
                    "FROM movies as m " +
                    "JOIN ratings AS r ON m.id = r.movieId ";

            String price_query = "SELECT price " +
                                 "FROM movie_prices mp " +
                                 "WHERE mp.movieId = ?";

            String insert_price = "INSERT INTO movie_prices VALUES(?, ?)";

            if (!genre.equals("%")) {
                movie_query += "JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                        "JOIN genres AS g ON g.id = gim.genreId " +
                        "WHERE g.id LIKE ? ";
                count_query += "JOIN genres_in_movies AS gim ON m.id = gim.movieId " +
                        "JOIN genres AS g ON g.id = gim.genreId " +
                        "WHERE g.id LIKE ? ";
            }
            else if (!start.equals("%")) {
                movie_query += (start.equals("*")) ? "WHERE m.title REGEXP ? " : "WHERE m.title LIKE ? ";
                count_query += (start.equals("*")) ? "WHERE m.title REGEXP ? " : "WHERE m.title LIKE ? ";
            }
            else {
                movie_query += "JOIN stars_in_movies AS sim on m.id = sim.movieId " +
                        "JOIN stars AS s ON sim.starId = s.id " +
                        "WHERE m.director LIKE ? AND s.name LIKE ? AND m.year LIKE ? ";
                count_query += "JOIN stars_in_movies AS sim on m.id = sim.movieId " +
                        "JOIN stars AS s ON sim.starId = s.id " +
                        "WHERE m.director LIKE ? AND s.name LIKE ? AND m.year LIKE ? ";
                if (!title.equals("%")) {
                    movie_query += "AND MATCH(title) AGAINST (? IN BOOLEAN MODE) ";
                    count_query += "AND MATCH(title) AGAINST (? IN BOOLEAN MODE) ";
                }
            }

            // Assign sorting
            if (firstSort.startsWith("title")) {
                // First sort by title
                movie_query += (firstSort.endsWith("DESC")) ? "ORDER BY m.title DESC, " : "ORDER BY m.title ASC, ";
                // Break ties with rating
                movie_query += (secondSort.endsWith("DESC")) ? "r.rating DESC " : "r.rating ASC ";
            }
            else {
                // First sort with rating
                movie_query += (firstSort.endsWith("DESC")) ? "ORDER BY r.rating DESC, " : "ORDER BY r.rating ASC, ";
                // Break ties with title
                movie_query += (secondSort.endsWith("DESC")) ? "m.title DESC " : "m.title ASC ";
            }

            movie_query += "LIMIT ? OFFSET ?";

            String stars_query = "SELECT s.name, sim.starId " +
                                 "FROM (SELECT sim.starId FROM stars_in_movies sim WHERE sim.movieId = ? ) as star_list, stars_in_movies sim, stars s " +
                                 "WHERE star_list.starId = sim.starId and s.id = star_list.starId " +
                                 "GROUP BY sim.starId " +
                                 "ORDER BY count(sim.starId) DESC, s.name ASC " +
                                 "LIMIT 3";

            String genre_query = "SELECT g.id, g.name from genres as g, genres_in_movies as gim " +
                    "where gim.genreId = g.id and gim.movieId = ? " +
                    "order by g.name asc " +
                    "limit 3";

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(movie_query);
            PreparedStatement count_statement = conn.prepareStatement(count_query);

            //check if title LIKE '%%'still works how we intended it to
            if (!title.equals("%")) {
                title = title.trim();
                title = title.replace(" ", "* +");
                title = "+" + title + "*";
            }

            director = '%' + director + '%';
            star_name = '%' + star_name + '%';

            String start_adjusted = (start.equals("*")) ? "^[^a-zA-Z0-9]" : start + "%";

            //replacing ? in movie query string
            int index = 1;
            if (!genre.equals("%")) {
                count_statement.setString(index, genre);
                statement.setString(index++, genre);
            }
            else if (!start.equals("%")) {
                count_statement.setString(index, start_adjusted);
                statement.setString(index++, start_adjusted);
            }
            else {
                count_statement.setString(index, director);
                statement.setString(index++, director);
                count_statement.setString(index, star_name);
                statement.setString(index++, star_name);
                count_statement.setString(index, year);
                statement.setString(index++, year);
                if (!title.equals("%")) {
                    count_statement.setString(index, title);
                    statement.setString(index++, title);
                }
            }
            statement.setInt(index++, Integer.parseInt(limit));
            statement.setInt(index, offset);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            // Declare object for movie information
            JsonArray movieArray = new JsonArray();

            ResultSet count_rs = count_statement.executeQuery();
            count_rs.next();

            //set up movies objects
            while(rs.next()) {
                JsonObject movieObj = new JsonObject();
                PreparedStatement price_statement = conn.prepareStatement(price_query);

                // Convert row data into strings
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String rating = rs.getString("rating");
                String count = count_rs.getString("count");

                price_statement.setString(1,movieId);
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
                    insert_statement.setString(1,movieId);
                    insert_statement.setFloat(2, price);
                    insert_statement.executeUpdate();
                }
                else {
                    price = price_rs.getFloat("price");
                }

                // Add data as properties of the object
                movieObj.addProperty("movie_id", movieId);
                movieObj.addProperty("movie_title", movieTitle);
                movieObj.addProperty("movie_year", movieYear);
                movieObj.addProperty("movie_director", movieDirector);
                movieObj.addProperty("rating", rating);
                movieObj.addProperty("count", count);
                movieObj.addProperty("price",price);

                price_statement.close();
                movieArray.add(movieObj);

                JsonArray genreArray = new JsonArray();
                statement = conn.prepareStatement(genre_query);
                statement.setString(1, movieId);
                ResultSet genre_rs = statement.executeQuery();

                while(genre_rs.next()){
                    JsonObject genreObj = new JsonObject();

                    String genreId = genre_rs.getString("id");
                    String genreName = genre_rs.getString("name");
                    genreObj.addProperty("genre_id", genreId);
                    genreObj.addProperty("genre_name", genreName);

                    genreArray.add(genreObj);
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
                    String starName = stars_rs.getString("name");

                    starObj.addProperty("star_id", star_id);
                    starObj.addProperty("star_name", starName);
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
