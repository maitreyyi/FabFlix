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

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {
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

        String star_name = request.getParameter("star_name");
        String year = request.getParameter("birthyear");


        request.getServletContext().log("getting parameters: " + star_name);

        try(Connection conn = dataSource.getConnection()){
            String star_query = "SELECT id FROM stars WHERE name = ? AND birthYear = ?;";

            String insert_statement = "DECLARE @star_id INT; " +
                                    "SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1 INTO star_id FROM stars; " +
                                    "IF star_id IS NULL THEN " +
                                    "   SET star_id = 'nm0000001'; " +
                                    "ELSE " +
                                    "   SET star_id = CONCAT('nm', LPAD(star_id, 7, '0')); " +
                                    "END IF; " +
                                    "INSERT INTO stars (id, name, birthYear) VALUES (star_id, ?, ?); ";
            //set birth year in star_id
            PreparedStatement statement = conn.prepareStatement(star_query);
            ResultSet star_rs           = statement.executeQuery();

            JsonObject responseJson = new JsonObject(); //store success/failure

            if(!star_rs.next()){
                //set birthyear and star_name in insert_statement if star_id == null
                PreparedStatement insert_star = conn.prepareStatement(insert_statement);
                insert_star.setString(1, star_name);
                insert_star.setString(2,year);
                insert_star.executeUpdate();

                insert_star.close();
            } else {
                //update response to state star already exists (display id)
            }
            star_rs.close();
            statement.close();

            out.write(responseJson.toString());
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
