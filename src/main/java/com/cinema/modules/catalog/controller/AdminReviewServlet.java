package com.cinema.modules.catalog.controller;

import com.cinema.common.constant.RoleConstant;
import com.cinema.model.Review;
import com.cinema.model.User;
import com.cinema.modules.catalog.service.ReviewService;
import com.cinema.modules.catalog.service.impl.ReviewServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Quan ly duyet danh gia phim cho Admin.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@WebServlet(name = "AdminReviewServlet", urlPatterns = {
        "/admin/reviews",
        "/admin/reviews/approve",
        "/admin/reviews/hide",
        "/admin/reviews/delete"
})
public class AdminReviewServlet extends HttpServlet {
    private final ReviewService reviewService = new ReviewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }

        String status = req.getParameter("status");
        try {
            List<Review> reviews = reviewService.getAllReviewsForAdmin(status);
            req.setAttribute("reviews", reviews);
            req.setAttribute("selectedStatus", status);
            req.setAttribute("msg", req.getParameter("msg"));
            req.setAttribute("error", req.getParameter("error"));
            req.getRequestDispatcher("/WEB-INF/views/admin/reviews.jsp").forward(req, resp);
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!ensureAdmin(req, resp)) {
            return;
        }

        String path = req.getServletPath();
        String reviewIdParam = req.getParameter("reviewId");

        Long reviewId;
        try {
            reviewId = Long.parseLong(reviewIdParam);
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews?error="
                    + URLEncoder.encode("ID danh gia khong hop le.", StandardCharsets.UTF_8));
            return;
        }

        try {
            boolean ok = false;
            String msg = "updated";

            if ("/admin/reviews/approve".equals(path)) {
                ok = reviewService.approveReview(reviewId);
                msg = "approved";
            } else if ("/admin/reviews/hide".equals(path)) {
                ok = reviewService.hideReview(reviewId);
                msg = "hidden";
            } else if ("/admin/reviews/delete".equals(path)) {
                ok = reviewService.deleteReview(reviewId);
                msg = "deleted";
            }

            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/admin/reviews?msg=" + msg);
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/reviews?error="
                        + URLEncoder.encode("Khong the thuc hien thao tac voi danh gia nay.", StandardCharsets.UTF_8));
            }
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/admin/reviews?error="
                    + URLEncoder.encode("Da co loi he thong khi xu ly danh gia.", StandardCharsets.UTF_8));
        }
    }

    private boolean ensureAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }

        String role = currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "";
        if (!RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chi Admin moi duoc quan ly danh gia phim.");
            return false;
        }
        return true;
    }
}


