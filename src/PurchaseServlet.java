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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;

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
            card = card.substring(0, 4) + " " + card.substring(4, 8) + " " + card.substring(8, 12) + " " + card.substring(12);
            String expr_param = (!request.getParameter("expir_date").isEmpty()) ? request.getParameter("expir_date") : "1000-01-01";
            Date expiration = Date.valueOf(expr_param);

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
                Date return_expiration = rs.getDate("expiration");

                if (!first.equals(return_first)) // Double-check string comparisons
                {
                    // First name is wrong, purchase fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Purchase failed");
                    responseJsonObject.addProperty("message", "Invalid first name");
                }
                else if (!last.equals(return_last)) // Double-check string comparisons
                {
                    // Last name is wrong, login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Purchase failed");
                    responseJsonObject.addProperty("message", "Invalid last name");
                }
                else if (expiration.compareTo(return_expiration) != 0)
                {
                    // Expiration date is wrong, login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Purchase failed");
                    responseJsonObject.addProperty("message", "Invalid expiration date");
                }
                else
                {
                    // Login success:
                    // set this user into the session

                    String customer_query = "SELECT id FROM customers " +
                            "WHERE firstName = ? AND lastName = ?";
                    PreparedStatement customer_statement = conn.prepareStatement(customer_query);
                    customer_statement.setString(1, first);
                    customer_statement.setString(2, last);
                    ResultSet customer_rs = customer_statement.executeQuery();
                    customer_rs.next();
                    String customer = customer_rs.getString("id");

                    // Insert values into sales
                    User user = (User) request.getSession().getAttribute("user");
                    Map<String, Integer> cart = user.getCart();

                    String sales_query = "INSERT INTO sales (customerId, movieId, salesDate, quantity)" +
                            "VALUES (?, ?, ?, ?)";

                    LocalDate cur_date = LocalDate.now();

                    for (var entry : cart.entrySet())
                    {
                        PreparedStatement insert_statement = conn.prepareStatement(sales_query);
                        insert_statement.setString(1, customer);
                        insert_statement.setString(2, entry.getKey());
                        insert_statement.setDate(3, Date.valueOf(cur_date));
                        insert_statement.setInt(4, entry.getValue());
                        // Perform the query
                        insert_statement.executeUpdate();
                    }

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                }
            }
            else {
                // Credit card is wrong, purchase fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Purchase failed");
                responseJsonObject.addProperty("message", "Invalid credit card number");
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
