import com.google.gson.JsonArray;
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
import java.util.Random;

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            System.out.println("Populating json array with table data");

            String tables_query = "SELECT DISTINCT TABLE_NAME " +
                                 "FROM INFORMATION_SCHEMA.COLUMNS " +
                                 "WHERE TABLE_SCHEMA = 'moviedb'";

            String table_query = "SELECT COLUMN_NAME, DATA_TYPE " +
                                 "FROM INFORMATION_SCHEMA.COLUMNS " +
                                 "WHERE TABLE_SCHEMA = 'moviedb' AND TABLE_NAME = ?";

            PreparedStatement statement = conn.prepareStatement(tables_query);
            ResultSet tables_rs = statement.executeQuery();
            JsonArray tableArray = new JsonArray();

            while(tables_rs.next()){
                JsonObject tableJsonObject = new JsonObject();

                String table_name = tables_rs.getString("TABLE_NAME");
                tableJsonObject.addProperty("table_name", table_name);

                PreparedStatement table_statement = conn.prepareStatement(table_query);
                table_statement.setString(1,table_name);

                ResultSet table_rs    = table_statement.executeQuery();
                JsonArray columnArray = new JsonArray();

                while(table_rs.next()){
                    JsonObject columnObject = new JsonObject();

                    String column    = table_rs.getString("COLUMN_NAME");
                    String data_type = table_rs.getString("DATA_TYPE");

                    columnObject.addProperty("attribute", column);
                    columnObject.addProperty("column_type", data_type);

                    columnArray.add(columnObject);

                }
                tableJsonObject.add("columns", columnArray);
                table_rs.close();

                tableArray.add(tableJsonObject);
            }

            statement.close();
            tables_rs.close();
           // out.write(tables.toString());

            out.write(tableArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

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
