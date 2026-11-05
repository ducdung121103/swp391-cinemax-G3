package com.cinema.modules.operation.controller;

import com.cinema.modules.operation.service.FnBMenuService;
import com.cinema.modules.operation.service.impl.FnBMenuServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/concession-menu")
public class ConcessionMenuServlet extends HttpServlet {
    private final FnBMenuService menuService = new FnBMenuServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("fnbItems", menuService.getAvailableItems());
        request.getRequestDispatcher("/WEB-INF/views/customer/catalog/concession-menu.jsp")
                .forward(request, response);
    }
}
