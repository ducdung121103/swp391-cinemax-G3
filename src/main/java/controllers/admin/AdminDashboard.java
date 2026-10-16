package controllers.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import services.AdminDashboardService;
import services.AuthService;
import models.User;

@WebServlet(name = "AdminDashboard", urlPatterns = { "/admin/dashboard" })
public class AdminDashboard extends HttpServlet {

    private AuthService authService = new AuthService();
    private AdminDashboardService dashboardService = new AdminDashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String role = "";
        try {
            role = authService.getRoleName(user.getRoleId());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            java.util.Map<String, Object> dashboardData = dashboardService.getDashboardData();
            request.setAttribute("dashboard", dashboardData);
            // Pass JSON strings for charts
            request.setAttribute("revenueByBranchJson", dashboardService.toJson(dashboardData.get("revenueByBranch")));
            request.setAttribute("topMoviesJson", dashboardService.toJson(dashboardData.get("topMovies")));
            request.setAttribute("revenueTrendJson", dashboardService.toJson(dashboardData.get("revenueTrend")));

            request.getRequestDispatcher("/pages/admin/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading dashboard data");
        }
    }
}
