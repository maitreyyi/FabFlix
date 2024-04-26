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
import java.util.Date;

@WebServlet(name = "PurchaseServlet", urlPatterns = "/api/payment")
public class PurchaseServlet extends HttpServlet {
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
            String first = request.getParameter("first_name");
            String last = request.getParameter("last_name");
            String card = request.getParameter("card");
            String expiration = request.getParameter("expir_date");

            /* This example only allows username/password to be test/test
            /  in the real project, you should talk to the database to verify username/password
            */
            String query = "SELECT * FROM creditcards " +
                    "where id = ?";

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, card);
            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonObject responseJsonObject = new JsonObject();

            if(rs.next())
            {
                String return_first = rs.getString("firstName");
                String return_last = rs.getString("lastName");
                Date return_expiration = rs.getDate("expiration"); // Need to import date

                if (!first.equals(return_first)) // Double-check string comparisons
                {
                    // First name is wrong, purchase fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Purchase failed");
                    responseJsonObject.addProperty("message", "incorrect first name");
                }
                else if (!last.equals(return_last)) // Double-check string comparisons
                {
                    // Last name is wrong, login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Purchase failed");
                    responseJsonObject.addProperty("message", "incorrect last name");
                }
                else if (!expiration.equals(return_expiration)) // String to date comparison, might need conversion
                {
                    // Expiration date is wrong, login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Purchase failed");
                    responseJsonObject.addProperty("message", "incorrect expiration date");
                }
                else
                {
                    // Login success:
                    // set this user into the session

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                }
            }
            else {
                // Credit card is wrong, purchase fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Purchase failed");
                responseJsonObject.addProperty("message", "card " + card + " doesn't exist");
            }
            statement.close();
            rs.close();
            out.write(responseJsonObject.toString());

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
