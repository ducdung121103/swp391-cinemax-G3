package controllers.guest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.HomeService;

import java.io.IOException;

public class Home extends HttpServlet {

    private final HomeService homeService = new HomeService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Load public branch information
        request.setAttribute("branches",
                homeService.getActiveBranches());

        request.getRequestDispatcher("/pages/index.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.sendRedirect(request.getContextPath() + "/home");
    }
}