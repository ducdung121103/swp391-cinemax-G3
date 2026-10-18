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
 * AuthorizationFilter thực thi kiểm soát phân quyền dựa trên Vai trò (Role-Based Access Control - RBAC).
 */
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

        // 1. ADMIN có toàn quyền
        if (RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. MANAGER được vào hạ tầng, catalog, hóa đơn, báo cáo của rạp mình
        if (RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            if (uri.contains("/admin/operation/fnb-catalog")) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ Admin được quản lý danh mục F&B!");
                return;
            }
            if (uri.contains("/admin/identity/users") && !uri.contains("/admin/identity/staff")) {
                // Không được sửa tài khoản Admin
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền quản lý tài khoản cấp cao!");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // 3. STAFF chỉ được vào POS và Scanner QR
        if (RoleConstant.STAFF.equalsIgnoreCase(role)) {
            if (uri.contains("/admin/operation/pos") || uri.contains("/admin/operation/scanner")) {
                chain.doFilter(request, response);
                return;
            } else {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Nhân viên chỉ có quyền truy cập Quầy POS và Soát vé!");
                return;
            }
        }

        // 4. CUSTOMER cố tình vào khu vực Admin -> Chặn 403
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Khách hàng không có quyền truy cập khu vực Quản trị!");
    }
}
