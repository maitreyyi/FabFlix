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
import org.jasypt.util.password.StrongPasswordEncryptor;
import java.util.Random;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/customer_login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        Random random = new Random();
        try {
            if(random.nextBoolean()){
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slavedb");
            } else {
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
            }
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
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

            RecaptchaVerifyUtils.verify(gRecaptchaResponse);


            String username = request.getParameter("username");
            String password = request.getParameter("password");

            String query = "SELECT id, password FROM customers " +
                    "where email = ?";

            // Declare statement for stars_query
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonObject responseJsonObject = new JsonObject();

            if(rs.next())
            {
                String encrypted_password = rs.getString("password");
                Boolean success = new StrongPasswordEncryptor().checkPassword(password, encrypted_password);

                if (!success) // Double-check string comparisons
                {
                    // Password is wrong, login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Login failed");
                    // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                    responseJsonObject.addProperty("message", "incorrect password");
                }
                else
                {
                    // Login success:
                    // set this user into the session
                    if (request.getSession().getAttribute("user") == null)
                        request.getSession().setAttribute("user", new User(username));
                    User user = (User) request.getSession().getAttribute("user");
                    user.log_customer();

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");

                }
            }
            else {
                // Email is wrong, login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            }
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
