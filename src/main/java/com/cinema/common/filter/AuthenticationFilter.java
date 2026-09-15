package com.cinema.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * AuthenticationFilter kiểm tra phiên đăng nhập của người dùng.
 * Nếu chưa đăng nhập mà truy cập tài nguyên bảo mật -> Chuyển hướng về trang /login.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        boolean isLoggedIn = (session != null && session.getAttribute("currentUser") != null);

        if (!isLoggedIn) {
            String targetUri = req.getRequestURI();
            String query = req.getQueryString();
            if (query != null) {
                targetUri += "?" + query;
            }
            res.sendRedirect(req.getContextPath() + "/login?redirect=" + java.net.URLEncoder.encode(targetUri, "UTF-8"));
            return;
        }

        chain.doFilter(request, response);
    }
}
