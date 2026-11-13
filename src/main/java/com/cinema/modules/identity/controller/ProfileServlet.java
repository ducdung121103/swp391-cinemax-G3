package com.cinema.modules.identity.controller;

import com.cinema.common.util.ValidationUtil;
import com.cinema.model.User;
import com.cinema.modules.identity.service.UserService;
import com.cinema.modules.identity.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Instant;

/**
 * Hồ sơ khách và đổi mật khẩu có OTP Gmail 30 phút (M-01.3).
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {
        "/customer/account/profile",
        "/account/profile",
        "/profile",
        "/customer/account/change-password-otp"
})
public class ProfileServlet extends HttpServlet {

    private static final String ATTR_PWD_USER_ID = "pwdChangeUserId";
    private static final String ATTR_PWD_HASH = "pwdChangeNewHash";
    private static final String ATTR_PWD_EXPIRES = "pwdChangeExpiresAt";
    private static final long PWD_CHANGE_SESSION_MINUTES = 30L;

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = currentUser(req);
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (isChangePasswordOtpPath(req)) {
            if (!hasValidPendingPasswordChange(req.getSession(false), current.getId())) {
                flash(req, "error", "OTP_SESSION_EXPIRED");
                resp.sendRedirect(req.getContextPath() + "/customer/account/profile");
                return;
            }
            req.setAttribute("otpEmail", current.getEmail());
            req.getRequestDispatcher("/WEB-INF/views/customer/account/change-password-otp.jsp").forward(req, resp);
            return;
        }

        User fresh = userService.getUserById(current.getId());
        if (fresh != null) {
            req.getSession().setAttribute("currentUser", fresh);
            req.setAttribute("profileUser", fresh);
        } else {
            req.setAttribute("profileUser", current);
        }
        req.getRequestDispatcher("/WEB-INF/views/customer/account/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = currentUser(req);
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (isChangePasswordOtpPath(req)) {
            handlePasswordOtpPost(req, resp, current);
            return;
        }

        String action = req.getParameter("action");
        if ("password".equals(action)) {
            requestPasswordChange(req, resp, current);
            return;
        }
        updateProfile(req, current);
        resp.sendRedirect(req.getContextPath() + "/customer/account/profile");
    }

    private void requestPasswordChange(HttpServletRequest req, HttpServletResponse resp, User current)
            throws IOException {
        String newPassword = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");
        if (!ValidationUtil.isValidPassword(newPassword)) {
            flash(req, "error", "WEAK_PASSWORD");
            resp.sendRedirect(req.getContextPath() + "/customer/account/profile");
            return;
        }
        if (confirm == null || !confirm.equals(newPassword)) {
            flash(req, "error", "PASSWORD_MISMATCH");
            resp.sendRedirect(req.getContextPath() + "/customer/account/profile");
            return;
        }

        String[] hashOut = new String[1];
        String code = userService.requestPasswordChangeOtp(
                current.getId(), req.getParameter("oldPassword"), newPassword, hashOut);
        if (!"OK".equals(code)) {
            flash(req, "error", code);
            resp.sendRedirect(req.getContextPath() + "/customer/account/profile");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute(ATTR_PWD_USER_ID, current.getId());
        session.setAttribute(ATTR_PWD_HASH, hashOut[0]);
        session.setAttribute(ATTR_PWD_EXPIRES,
                Instant.now().plusSeconds(PWD_CHANGE_SESSION_MINUTES * 60).toEpochMilli());
        flash(req, "msg", "otp_sent");
        resp.sendRedirect(req.getContextPath() + "/customer/account/change-password-otp");
    }

    private void handlePasswordOtpPost(HttpServletRequest req, HttpServletResponse resp, User current)
            throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (!hasValidPendingPasswordChange(session, current.getId())) {
            clearPendingPasswordChange(session);
            flash(req, "error", "OTP_SESSION_EXPIRED");
            resp.sendRedirect(req.getContextPath() + "/customer/account/profile");
            return;
        }

        String action = req.getParameter("action");
        if ("resend".equals(action)) {
            boolean sent = userService.resendPasswordChangeOtp(current.getId());
            if (sent) {
                session.setAttribute(ATTR_PWD_EXPIRES,
                        Instant.now().plusSeconds(PWD_CHANGE_SESSION_MINUTES * 60).toEpochMilli());
                req.setAttribute("successMessage", "Đã gửi lại mã OTP. Kiểm tra email (kể cả Spam).");
            } else {
                req.setAttribute("errorMessage", "Không gửi lại được OTP. Thử lại sau.");
            }
            req.setAttribute("otpEmail", current.getEmail());
            req.getRequestDispatcher("/WEB-INF/views/customer/account/change-password-otp.jsp").forward(req, resp);
            return;
        }

        String otpCode = req.getParameter("otpCode");
        if (otpCode == null || otpCode.isBlank()) {
            // Ghép từ 6 ô digit nếu form gửi từng digit
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 6; i++) {
                String d = req.getParameter("digit" + i);
                if (d != null) sb.append(d.trim());
            }
            otpCode = sb.toString();
        }

        String newHash = (String) session.getAttribute(ATTR_PWD_HASH);
        boolean ok = userService.confirmPasswordChangeWithOtp(current.getId(), otpCode, newHash);
        if (ok) {
            clearPendingPasswordChange(session);
            flash(req, "msg", "password_changed");
            resp.sendRedirect(req.getContextPath() + "/customer/account/profile");
            return;
        }

        req.setAttribute("errorMessage", "Mã OTP không đúng hoặc đã hết hạn. Thử lại hoặc gửi lại mã.");
        req.setAttribute("otpEmail", current.getEmail());
        req.getRequestDispatcher("/WEB-INF/views/customer/account/change-password-otp.jsp").forward(req, resp);
    }

    private void updateProfile(HttpServletRequest req, User current) {
        String code = userService.updateProfile(
                current.getId(),
                req.getParameter("fullName"),
                req.getParameter("phone"),
                req.getParameter("avatarUrl"));
        if ("OK".equals(code)) {
            User fresh = userService.getUserById(current.getId());
            if (fresh != null) {
                req.getSession().setAttribute("currentUser", fresh);
            }
            flash(req, "msg", "profile_saved");
        } else {
            flash(req, "error", code);
        }
    }

    private boolean isChangePasswordOtpPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return uri != null && uri.endsWith("/customer/account/change-password-otp");
    }

    private boolean hasValidPendingPasswordChange(HttpSession session, Long userId) {
        if (session == null || userId == null) return false;
        Object sid = session.getAttribute(ATTR_PWD_USER_ID);
        Object hash = session.getAttribute(ATTR_PWD_HASH);
        Object exp = session.getAttribute(ATTR_PWD_EXPIRES);
        if (!(sid instanceof Long) || !(hash instanceof String) || !(exp instanceof Long)) return false;
        if (!userId.equals(sid)) return false;
        if (((String) hash).isBlank()) return false;
        return Instant.now().toEpochMilli() <= (Long) exp;
    }

    private void clearPendingPasswordChange(HttpSession session) {
        if (session == null) return;
        session.removeAttribute(ATTR_PWD_USER_ID);
        session.removeAttribute(ATTR_PWD_HASH);
        session.removeAttribute(ATTR_PWD_EXPIRES);
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("currentUser");
        return value instanceof User ? (User) value : null;
    }

    private void flash(HttpServletRequest req, String key, String value) {
        req.getSession().setAttribute("profileFlashKey", key);
        req.getSession().setAttribute("profileFlashValue", value);
    }
}
