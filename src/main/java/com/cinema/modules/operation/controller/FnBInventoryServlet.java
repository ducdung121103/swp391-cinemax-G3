package com.cinema.modules.operation.controller;

import com.cinema.model.Cinema;
import com.cinema.model.User;
import com.cinema.modules.infrastructure.dao.CinemaDAO;
import com.cinema.modules.operation.dao.FnBInventoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/operation/fnb")
public class FnBInventoryServlet extends HttpServlet {
    private final FnBInventoryDAO inventoryDAO = new FnBInventoryDAO();
    private final CinemaDAO cinemaDAO = new CinemaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=login_required");
            return;
        }
        try {
            if (user.getCinemaId() == null) {
                List<Cinema> cinemas = cinemaDAO.findAll();
                request.setAttribute("cinemas", cinemas);
                if (cinemas.isEmpty()) {
                    request.setAttribute("inventory", List.of());
                    request.setAttribute("inventoryMessage", "Chưa có chi nhánh. Hãy tạo chi nhánh trước khi quản lý tồn kho.");
                    request.getRequestDispatcher("/WEB-INF/views/admin/operation/fnb-inventory.jsp")
                            .forward(request, response);
                    return;
                }
            }
            long cinemaId = resolveCinema(request, user);
            request.setAttribute("cinemaId", cinemaId);
            request.setAttribute("inventory", inventoryDAO.findInventory(cinemaId));
            request.getRequestDispatcher("/WEB-INF/views/admin/operation/fnb-inventory.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Không thể tải tồn kho F&B", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=login_required");
            return;
        }
        try {
            long cinemaId = resolveCinema(request, user);
            long itemId = Long.parseLong(request.getParameter("itemId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            int threshold = Integer.parseInt(request.getParameter("threshold"));
            inventoryDAO.save(cinemaId, itemId, quantity, threshold);
            response.sendRedirect(request.getContextPath()
                    + "/admin/operation/fnb?success=1&cinemaId=" + cinemaId);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ServletException("Không thể cập nhật tồn kho F&B", e);
        }
    }

    private long resolveCinema(HttpServletRequest request, User user) {
        // Manager and staff accounts always operate on their assigned branch.
        if (user.getCinemaId() != null) {
            return user.getCinemaId();
        }
        String requestedId = request.getParameter("cinemaId");
        if (requestedId != null && !requestedId.isBlank()) {
            return Long.parseLong(requestedId);
        }
        List<Cinema> cinemas = cinemaDAO.findAll();
        if (!cinemas.isEmpty()) {
            return cinemas.get(0).getId();
        }
        throw new IllegalArgumentException("Chưa có rạp để quản lý tồn kho.");
    }
}
