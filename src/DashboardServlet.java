import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            String query = "SELECT password FROM employees " +
                    "where email = ?";

            System.out.println(query);

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(query);
            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();


            statement.close();
            rs.close();
            out.write(responseJsonObject.toString());

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            request.getServletContext().log("Error:", e);
            if (e.getMessage().equals("invalid-input-response")) {
                jsonObject.addProperty("status", "failed");
                jsonObject.addProperty("message", "Error verifying reCAPTCHA, please try again");
                response.setStatus(202);
            }
            else {
                jsonObject.addProperty("errorMessage", e.getMessage());
                // Log error to localhost log
                // Set response status to 500 (Internal Server Error)
                response.setStatus(500);
            }

            out.write(jsonObject.toString());
        } finally {
            out.close();
        }
    }
}
