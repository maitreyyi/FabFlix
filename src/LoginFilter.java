import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> employeeURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            // If employee page, redirect to employee login
            if (employee_page(httpRequest.getRequestURI())) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/employee_login.html");
            }
            // If customer page, redirect to customer lgoin
            else {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
            }
        } else {
            User cur_user = (User) httpRequest.getSession().getAttribute("user");
            if (employee_page(httpRequest.getRequestURI()) && !cur_user.is_employee()) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/employee_login.html");
            }
            else if (!employee_page(httpRequest.getRequestURI()) && !cur_user.is_customer()){
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
            }
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean employee_page(String requestURI) {
        return employeeURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/customer_login");
        allowedURIs.add("api/employee_login");

        employeeURIs.add("index.html");
        employeeURIs.add("dashboard.js");
        employeeURIs.add("api/dashboard");
    }

    public void destroy() {
        // ignored.
    }

}
