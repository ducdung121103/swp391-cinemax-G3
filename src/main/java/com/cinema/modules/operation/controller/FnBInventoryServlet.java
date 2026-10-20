package com.cinema.modules.operation.controller;

import com.cinema.model.Cinema;
import com.cinema.model.User;
import com.cinema.modules.operation.service.FnBInventoryService;
import com.cinema.modules.operation.service.impl.FnBInventoryServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/operation/fnb")
public class FnBInventoryServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/admin/operation/fnb-inventory.jsp";
    private final FnBInventoryService inventoryService = new FnBInventoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=login_required");
            return;
        }

        try {
            if (user.getCinemaId() == null) {
                List<Cinema> cinemas = inventoryService.getActiveCinemas();
                request.setAttribute("cinemas", cinemas);
                if (cinemas.isEmpty()) {
                    request.setAttribute("inventory", List.of());
                    request.setAttribute("inventoryMessage",
                            "Chưa có chi nhánh. Hãy tạo chi nhánh trước khi quản lý tồn kho.");
                    request.getRequestDispatcher(VIEW).forward(request, response);
                    return;
                }
            }

            long cinemaId = inventoryService.resolveCinemaId(user, request.getParameter("cinemaId"));
            request.setAttribute("cinemaId", cinemaId);
            request.setAttribute("inventory", inventoryService.getInventory(cinemaId));
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (IllegalArgumentException e) {
            request.setAttribute("inventory", List.of());
            request.setAttribute("inventoryMessage", e.getMessage());
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải tồn kho F&B.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=login_required");
            return;
        }

        try {
            long cinemaId = inventoryService.resolveCinemaId(user, request.getParameter("cinemaId"));
            long itemId = parseLong(request.getParameter("itemId"), "Sản phẩm không hợp lệ.");
            int quantity = parseInt(request.getParameter("quantity"), "Số lượng tồn kho không hợp lệ.");
            int threshold = parseInt(request.getParameter("threshold"), "Ngưỡng cảnh báo không hợp lệ.");
            inventoryService.updateStock(cinemaId, itemId, quantity, threshold);
            response.sendRedirect(request.getContextPath() + "/admin/operation/fnb?success=1&cinemaId=" + cinemaId);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Không thể cập nhật tồn kho F&B.", e);
        }
    }

    private User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("currentUser");
    }

    private long parseLong(String value, String message) {
        try {
            return Long.parseLong(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private int parseInt(String value, String message) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message, e);
        }
    }
}
