package com.cinema.common.filter;

import com.cinema.common.constant.RoleConstant;
import com.cinema.model.User;
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
 * AuthorizationFilter thá»±c thi kiá»ƒm soÃ¡t phÃ¢n quyá»n dá»±a trÃªn Vai trÃ² (Role-Based Access Control - RBAC).
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = (currentUser.getRole() != null) ? currentUser.getRole().getRoleName() : RoleConstant.CUSTOMER;
        String uri = req.getRequestURI();

        // 1. ADMIN cÃ³ toÃ n quyá»n
        if (RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            if (uri.contains("/operation/pos")) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin không có quyền sử dụng quầy POS!");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // 2. MANAGER Ä‘Æ°á»£c vÃ o háº¡ táº§ng, catalog, hÃ³a Ä‘Æ¡n, bÃ¡o cÃ¡o cá»§a ráº¡p mÃ¬nh
        if (RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            if (uri.contains("/admin/reports/revenue")) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Báo cáo Doanh thu chỉ dành riêng cho Quản trị viên (Admin)!");
                return;
            }
            if (uri.contains("/admin/catalog/movies") || uri.contains("/admin/movies")) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin hệ thống mới được quyền quản lý danh mục Phim!");
                return;
            }
            if (uri.contains("/admin/reviews")) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin hệ thống mới được quyền kiểm duyệt đánh giá phim!");
                return;
            }
                return;
            }
            if (uri.contains("/admin/operation/fnb-catalog")) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Chá»‰ Admin Ä‘Æ°á»£c quáº£n lÃ½ danh má»¥c F&B!");
                return;
            }
            if (uri.contains("/admin/identity/users") && !uri.contains("/admin/identity/staff")) {
                // KhÃ´ng Ä‘Æ°á»£c sá»­a tÃ i khoáº£n Admin
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Báº¡n khÃ´ng cÃ³ quyá»n quáº£n lÃ½ tÃ i khoáº£n cáº¥p cao!");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // 3. STAFF chá»‰ Ä‘Æ°á»£c vÃ o POS vÃ  Scanner QR
        if (RoleConstant.STAFF.equalsIgnoreCase(role)) {
            if (uri.contains("/operation/pos") || uri.contains("/admin/operation/scanner")) {
                chain.doFilter(request, response);
                return;
            } else {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "NhÃ¢n viÃªn chá»‰ cÃ³ quyá»n truy cáº­p Quáº§y POS vÃ  SoÃ¡t vÃ©!");
                return;
            }
        }

        // 4. CUSTOMER cá»‘ tÃ¬nh vÃ o khu vá»±c Admin -> Cháº·n 403
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "KhÃ¡ch hÃ ng khÃ´ng cÃ³ quyá»n truy cáº­p khu vá»±c Quáº£n trá»‹!");
    }
}

