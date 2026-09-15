package com.cinema.modules.operation.controller;

import com.cinema.model.FnBItem;
import com.cinema.modules.operation.dao.FnBItemDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller quầy bán vé & bắp nước POS cảm ứng tại rạp (TV 5).
 */
@WebServlet(name = "PosCounterServlet", urlPatterns = {"/admin/operation/pos"})
public class PosCounterServlet extends HttpServlet {
    private final FnBItemDAO fnbItemDAO = new FnBItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<FnBItem> fnbItems = fnbItemDAO.findAllActive();
        req.setAttribute("fnbItems", fnbItems);
        req.getRequestDispatcher("/WEB-INF/views/admin/operation/pos-counter.jsp").forward(req, resp);
    }
}
