package com.cinema.modules.catalog.controller;

import com.cinema.common.constant.RoleConstant;
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

/**
 * Xu ly gui danh gia phim cua khach hang.
 */
@WebServlet(name = "CustomerReviewServlet", urlPatterns = {"/customer/reviews", "/customer/reviews/create"})
public class CustomerReviewServlet extends HttpServlet {
    private final ReviewService reviewService = new ReviewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String movieId = req.getParameter("movieId");
        if (movieId != null && !movieId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/movie/detail?id=" + movieId.trim());
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/movies");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            String redirect = req.getContextPath() + "/movie/detail?id=" + req.getParameter("movieId");
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8));
            return;
        }

        String role = currentUser.getRole() != null ? currentUser.getRole().getRoleName() : RoleConstant.CUSTOMER;
        if (!RoleConstant.CUSTOMER.equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Chi khach hang moi duoc gui danh gia phim.");
            return;
        }

        Long movieId;
        Integer rating;
        String comment = req.getParameter("comment");

        try {
            movieId = Long.parseLong(req.getParameter("movieId"));
            rating = Integer.parseInt(req.getParameter("rating"));
        } catch (Exception e) {
            redirectWithError(resp, req, req.getParameter("movieId"), "Du lieu danh gia khong hop le.");
            return;
        }

        try {
            reviewService.submitReview(currentUser.getId(), movieId, rating, comment);
            String successMessage = "Your review has been submitted and is waiting for admin approval.";
            resp.sendRedirect(req.getContextPath() + "/movie/detail?id=" + movieId + "&reviewMsg="
                    + URLEncoder.encode(successMessage, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            redirectWithError(resp, req, String.valueOf(movieId), e.getMessage());
        } catch (Exception e) {
            redirectWithError(resp, req, String.valueOf(movieId), "Khong the gui danh gia. Vui long thu lai.");
        }
    }

    private void redirectWithError(HttpServletResponse resp, HttpServletRequest req, String movieId, String message)
            throws IOException {
        String fallbackMovieId = movieId != null ? movieId : "";
        resp.sendRedirect(req.getContextPath() + "/movie/detail?id=" + fallbackMovieId + "&reviewErr="
                + URLEncoder.encode(message != null ? message : "Da co loi xay ra.", StandardCharsets.UTF_8));
    }
}

