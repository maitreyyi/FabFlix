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
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");    // Response mime type

        // Retrieve parameter id from url request.
        String title = (request.getParameter("title") != null) ? request.getParameter("title") : "%";
        String year = (request.getParameter("year") != null) ? request.getParameter("year") : "%";
        String director = (request.getParameter("director") != null) ? request.getParameter("director") : "%";
        String star_name = (request.getParameter("star_name") != null) ? request.getParameter("star_name") : "%";

        //console.log(title);

        // The log message can be found in localhost log
        request.getServletContext().log("getting parameters: " + title);
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {

            // Create a new connection to database
            Connection dbCon = dataSource.getConnection();
            Statement statement = dbCon.createStatement();

            //substring matching and filtering

            //find matching results or redirect to movielistservlet

            // Close all structures
            //rs.close();
            statement.close();
            dbCon.close();



        } catch (Exception e) {
            /*
             * After you deploy the WAR file through tomcat manager webpage,
             *   there's no console to see the print messages.
             * Tomcat append all the print messages to the file: tomcat_directory/logs/catalina.out
             *
             * To view the last n lines (for example, 100 lines) of messages you can use:
             *   tail -100 catalina.out
             * This can help you debug your program after deploying it on AWS.
             */
            request.getServletContext().log("Error: ", e);

            // Output Error Massage to html
            out.println(String.format("<html><head><title>MovieDBExample: Error</title></head>\n<body><p>SQL error in doGet: %s</p></body></html>", e.getMessage()));
            return;
        }
        out.close();
    }
}
