package com.cinema.modules.operation.controller;

import com.cinema.model.FnBCategory;
import com.cinema.model.FnBItem;
import com.cinema.modules.operation.service.FnBCatalogService;
import com.cinema.modules.operation.service.impl.FnBCatalogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/admin/operation/fnb-catalog")
public class FnBCatalogServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/admin/operation/fnb-catalog.jsp";
    private static final String URL = "/admin/operation/fnb-catalog";

    private final FnBCatalogService catalogService = new FnBCatalogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("items", catalogService.getItems());
            request.setAttribute("categories", catalogService.getCategories());
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Không thể tải danh mục F&B.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            handleAction(request);
            response.sendRedirect(request.getContextPath() + URL + "?success=1");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Không thể cập nhật danh mục F&B.", e);
        }
    }

    private void handleAction(HttpServletRequest request) throws SQLException {
        String action = request.getParameter("action");
        if ("saveItem".equals(action)) {
            FnBItem item = new FnBItem();
            item.setId(parseOptionalId(request.getParameter("id"), "Mã sản phẩm không hợp lệ."));
            item.setCategoryId(parseRequiredLong(request.getParameter("categoryId"), "Danh mục sản phẩm không hợp lệ."));
            item.setItemCode(request.getParameter("itemCode"));
            item.setName(request.getParameter("name"));
            item.setPrice(parsePrice(request.getParameter("price")));
            item.setImageUrl(request.getParameter("imageUrl"));
            item.setIsCombo("true".equals(request.getParameter("isCombo")));
            catalogService.saveItem(item);
            return;
        }
        if ("toggleItem".equals(action)) {
            catalogService.setItemActive(
                    parseRequiredLong(request.getParameter("id"), "Mã sản phẩm không hợp lệ."),
                    "true".equals(request.getParameter("active")));
            return;
        }
        if ("saveCategory".equals(action)) {
            FnBCategory category = new FnBCategory();
            category.setId(parseOptionalId(request.getParameter("id"), "Mã danh mục không hợp lệ."));
            category.setName(request.getParameter("name"));
            category.setDescription(request.getParameter("description"));
            catalogService.saveCategory(category);
            return;
        }
        if ("toggleCategory".equals(action)) {
            catalogService.setCategoryActive(
                    parseRequiredLong(request.getParameter("id"), "Mã danh mục không hợp lệ."),
                    "true".equals(request.getParameter("active")));
            return;
        }
        throw new IllegalArgumentException("Thao tác không hợp lệ.");
    }

    private BigDecimal parsePrice(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá sản phẩm không hợp lệ.", e);
        }
    }

    private Long parseOptionalId(String value, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseRequiredLong(value, message);
    }

    private long parseRequiredLong(String value, String message) {
        try {
            long parsed = Long.parseLong(value == null ? "" : value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(message);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String error = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + URL + "?error=" + error);
    }
}
