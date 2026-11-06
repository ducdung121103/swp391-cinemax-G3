package com.cinema.common.filter;

import com.cinema.model.User;
import com.cinema.modules.identity.service.UserService;
import com.cinema.modules.identity.service.impl.UserServiceImpl;
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
 * AuthenticationFilter kiá»ƒm tra phiÃªn Ä‘Äƒng nháº­p cá»§a ngÆ°á»i dÃ¹ng.
 * Náº¿u chÆ°a Ä‘Äƒng nháº­p mÃ  truy cáº­p tÃ i nguyÃªn báº£o máº­t -> Chuyá»ƒn hÆ°á»›ng vá» trang /login.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public class AuthenticationFilter implements Filter {
    private final UserService userService = new UserServiceImpl();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        Object current = session != null ? session.getAttribute("currentUser") : null;
        if (!(current instanceof User)) {
            String targetUri = req.getRequestURI();
            String query = req.getQueryString();
            if (query != null) {
                targetUri += "?" + query;
            }
            res.sendRedirect(req.getContextPath() + "/login?redirect=" + java.net.URLEncoder.encode(targetUri, "UTF-8"));
            return;
        }

        User fresh = userService.getUserById(((User) current).getId());
        if (fresh != null) {
            if ("LOCKED".equalsIgnoreCase(fresh.getStatus())) {
                session.invalidate();
                res.sendRedirect(req.getContextPath() + "/login?msg=locked");
                return;
            }
            session.setAttribute("currentUser", fresh);
        }

        chain.doFilter(request, response);
    }
}

