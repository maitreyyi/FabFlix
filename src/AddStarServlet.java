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
import java.sql.SQLException;
import java.util.Random;

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
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
        JsonObject responseJson = new JsonObject(); //store success/failure

        String star_name = request.getParameter("star_name");
        String year = request.getParameter("birthyear");

        System.out.println("Star name: " +star_name);
        System.out.println("Year: " + year);
        request.getServletContext().log("getting parameters: " + star_name);

        try(Connection conn = dataSource.getConnection()){
            String star_query = "SELECT id FROM stars WHERE name = ? AND birthYear = ?;";

            String insert_statement = "call add_star(?, ?);";

            //set birth year & starname in star_query
            PreparedStatement statement = conn.prepareStatement(star_query);

            statement.setString(1,star_name);
            statement.setString(2,year);

            ResultSet star_rs = statement.executeQuery();

            if(!star_rs.next()){
                //set birthyear and star_name in insert_statement if star_id == null
                PreparedStatement insert_star = conn.prepareStatement(insert_statement);

                insert_star.setString(1, star_name);
                insert_star.setString(2,year);

                insert_star.executeUpdate();

                //find starId
                PreparedStatement idStatement = conn.prepareStatement(star_query);

                idStatement.setString(1,star_name);
                idStatement.setString(2,year);

                ResultSet rs = idStatement.executeQuery();

                if(rs.next()){
                    responseJson.addProperty("status", "Star: " + star_name + " added successfully. Star id: " + rs.getString("id"));
                }

                idStatement.close();
                insert_star.close();
            } else {
                //update response to state star already exists (display id)
                responseJson.addProperty("status", "Star: " + star_name + " already exists in database. Star id: " + star_rs.getString("id") );
            }
            star_rs.close();
            statement.close();

            out.write(responseJson.toString());
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            //responseJson.addProperty("status", "Star: " + star_name + " could not be added");
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
