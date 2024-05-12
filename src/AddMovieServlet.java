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

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject responseJson = new JsonObject();

        String movie_title = request.getParameter("title");
        String movie_year = request.getParameter("year");
        String movie_director = request.getParameter("director");
        String movie_star = request.getParameter("star");
        String movie_genre = request.getParameter("genre");

        request.getServletContext().log("getting parameters: " + movie_title);

        try(Connection conn = dataSource.getConnection()){
            String insert_statement = "call add_movie(?, ?, ?, ?, ?) ;";

            PreparedStatement statement = conn.prepareStatement(insert_statement);

            statement.setString(1,movie_title);
            statement.setString(2,movie_year);
            statement.setString(3,movie_director);
            statement.setString(4,movie_star);
            statement.setString(5,movie_genre);

            statement.executeUpdate();

            //find movieid, starid, genreid that is generated
            String query = "SELECT m.id, sim.starId, gim.genreId "+
                           "FROM movies m, stars_in_movies sim, genres_in_movies gim " +
                           "WHERE m.id = sim.movieId AND m.id = gim.movieId AND m.title = ? AND m.year = ? AND m.director = ? ";

            PreparedStatement idStatement = conn.prepareStatement(query);

            idStatement.setString(1,movie_title);
            idStatement.setString(2,movie_year);
            idStatement.setString(3,movie_director);

            ResultSet rs = idStatement.executeQuery();
            
            if(rs.next()){
                String movieId = rs.getString("id");
                String starId  = rs.getString("starId");
                String genreId = rs.getString("genreId");

                responseJson.addProperty("status", "Success -> Movie id: " + movieId + " star id: " + starId + " genre id: " + genreId);
            }

            out.write(responseJson.toString());

        } catch (Exception e) {
            // Write error message JSON object to output
            responseJson.addProperty("status", "Movie is a duplicate");
            out.write(responseJson.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            //response.setStatus(500);
        } finally {
            out.close();
        }

    }

}
