package com.cinema.modules.identity.controller;

import com.cinema.common.constant.RoleConstant;
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

/**
 * Controller xử lý toàn bộ luồng Identity:
 * - /login      → Đăng nhập (BCrypt, phân quyền redirect)
 * - /logout     → Đăng xuất an toàn (hủy session)
 * - /register   → Đăng ký tài khoản (gửi OTP email)
 * - /verify-otp → Xác thực mã OTP 6 số để kích hoạt tài khoản
 *
 * Module Identity — TV 2 (Cường) — M-01.1, M-01.2
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout", "/register", "/verify-otp", "/forgot-password", "/reset-password"})
public class AuthServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    // ────────────────────────────────────────────────────────────────────────────
    // GET HANDLERS
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        // --- /logout: Hủy session và redirect về login ---
        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login?msg=logged_out");
            return;
        }

        // --- /register: Hiển thị form đăng ký ---
        if ("/register".equals(path)) {
            // Nếu đã đăng nhập → về trang chủ
            if (isLoggedIn(req)) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp")
               .forward(req, resp);
            return;
        }

        // --- /verify-otp: Hiển thị form nhập OTP ---
        if ("/verify-otp".equals(path)) {
            String email = req.getParameter("email");
            if (email == null || email.isBlank()) {
                // Không có email → về trang đăng ký
                resp.sendRedirect(req.getContextPath() + "/register");
                return;
            }
            req.setAttribute("verifyEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/verify-otp.jsp")
               .forward(req, resp);
            return;
        }

        // --- /forgot-password: Hiển thị form quên mật khẩu ---
        if ("/forgot-password".equals(path)) {
            if (isLoggedIn(req)) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            req.getRequestDispatcher("/WEB-INF/views/customer/account/forgot-password.jsp")
               .forward(req, resp);
            return;
        }

        // --- /reset-password: Hiển thị form đặt lại mật khẩu từ link email token ---
        if ("/reset-password".equals(path)) {
            String token = req.getParameter("token");
            if (isBlank(token)) {
                req.setAttribute("tokenInvalid", true);
                req.setAttribute("errorMessage", "Đường dẫn không hợp lệ hoặc thiếu mã token xác thực.");
                req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp")
                   .forward(req, resp);
                return;
            }
            User user = userService.getUserByResetToken(token.trim());
            if (user == null) {
                req.setAttribute("tokenInvalid", true);
                req.setAttribute("errorMessage", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn (sau 15 phút). Vui lòng yêu cầu liên kết mới.");
                req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp")
                   .forward(req, resp);
                return;
            }
            req.setAttribute("token", token.trim());
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp")
               .forward(req, resp);
            return;
        }

        // --- /login: Hiển thị form đăng nhập ---
        if (isLoggedIn(req)) {
            // Đã đăng nhập → về trang chủ (hoặc redirect param nếu có)
            String redirect = req.getParameter("redirect");
            resp.sendRedirect(redirect != null && !redirect.isBlank()
                    ? redirect
                    : req.getContextPath() + "/");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp")
           .forward(req, resp);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // POST HANDLERS
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        // ── POST /login ──────────────────────────────────────────────────────────
        if ("/login".equals(path)) {
            handleLogin(req, resp);
            return;
        }

        // ── POST /register ───────────────────────────────────────────────────────
        if ("/register".equals(path)) {
            handleRegister(req, resp);
            return;
        }

        // ── POST /verify-otp ─────────────────────────────────────────────────────
        if ("/verify-otp".equals(path)) {
            handleVerifyOtp(req, resp);
            return;
        }

        // ── POST /forgot-password ───────────────────────────────────────────────
        if ("/forgot-password".equals(path)) {
            handleForgotPassword(req, resp);
            return;
        }

        // ── POST /reset-password ────────────────────────────────────────────────
        if ("/reset-password".equals(path)) {
            handleResetPassword(req, resp);
            return;
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // PRIVATE BUSINESS HANDLERS
    // ────────────────────────────────────────────────────────────────────────────

    /** Xử lý đăng nhập: xác thực BCrypt, phân quyền redirect. */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        // Validate input cơ bản
        if (isBlank(email) || isBlank(password)) {
            req.setAttribute("errorMessage", "Vui lòng nhập đầy đủ email và mật khẩu.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp").forward(req, resp);
            return;
        }

        User user = userService.authenticate(email.trim(), password);

        if (user == null) {
            req.setAttribute("errorMessage", "Email hoặc mật khẩu không chính xác!");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp").forward(req, resp);
            return;
        }

        // Tài khoản chưa xác thực OTP → chuyển về trang verify
        if ("UNVERIFIED".equalsIgnoreCase(user.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/verify-otp?email="
                    + java.net.URLEncoder.encode(user.getEmail(), "UTF-8")
                    + "&msg=need_verify");
            return;
        }

        // Tài khoản bị khóa (LOCKED)
        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            req.setAttribute("errorMessage", "Tài khoản đã bị khóa");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp").forward(req, resp);
            return;
        }

        // Đăng nhập thành công → tạo session
        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);
        session.setMaxInactiveInterval(60 * 60); // Session timeout 1 giờ

        // Redirect theo redirect param hoặc theo role
        String redirectParam = req.getParameter("redirect");
        String role = (user.getRole() != null) ? user.getRole().getRoleName() : RoleConstant.CUSTOMER;

        if (redirectParam != null && !redirectParam.isBlank()
                && redirectParam.startsWith(req.getContextPath())) {
            // Đối với STAFF, chỉ cho phép redirect đến URL mà STAFF có quyền (pos hoặc scanner)
            if (!RoleConstant.STAFF.equalsIgnoreCase(role)
                    || redirectParam.contains("/admin/operation/pos")
                    || redirectParam.contains("/admin/operation/scanner")) {
                resp.sendRedirect(redirectParam);
                return;
            }
        }

        if (RoleConstant.ADMIN.equalsIgnoreCase(role)
                || RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else if (RoleConstant.STAFF.equalsIgnoreCase(role)) {
            resp.sendRedirect(req.getContextPath() + "/admin/operation/pos");
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    /** Xử lý đăng ký: validate, tạo user UNVERIFIED, gửi OTP email. */
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullName         = req.getParameter("fullName");
        String phone            = req.getParameter("phone");
        String email            = req.getParameter("email");
        String password         = req.getParameter("password");
        String confirmPassword  = req.getParameter("confirmPassword");

        // Validate phía server
        if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
            req.setAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin bắt buộc.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("errorMessage", "Định dạng email không hợp lệ (ví dụ: name@gmail.com).");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }
        if (!isBlank(phone) && !ValidationUtil.isValidPhone(phone)) {
            req.setAttribute("errorMessage", "Số điện thoại không hợp lệ. Phải là số di động VN gồm 10 số (bắt đầu bằng 03, 05, 07, 08, 09).");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }
        if (!password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }
        if (!ValidationUtil.isValidPassword(password)) {
            req.setAttribute("errorMessage", "Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt (ví dụ: CineMax@2026).");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }

        // Kiểm tra email đã đăng ký
        if (userService.isEmailTaken(email)) {
            req.setAttribute("errorMessage", "Email này đã được đăng ký. Vui lòng dùng email khác hoặc đăng nhập.");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }

        // Kiểm tra số điện thoại đã đăng ký (nếu có nhập)
        if (phone != null && !phone.isBlank() && userService.isPhoneTaken(phone)) {
            req.setAttribute("errorMessage", "Số điện thoại này đã được sử dụng cho tài khoản khác. Vui lòng nhập số khác.");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }

        User created = userService.registerCustomer(email.trim(), password, fullName.trim(), phone);

        if (created != null) {
            // Thành công → chuyển đến trang nhập OTP
            resp.sendRedirect(req.getContextPath() + "/verify-otp?email="
                    + java.net.URLEncoder.encode(created.getEmail(), "UTF-8")
                    + "&msg=otp_sent");
        } else {
            // Thất bại do lỗi hệ thống
            req.setAttribute("errorMessage", "Đăng ký không thành công. Vui lòng kiểm tra lại thông tin.");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
        }
    }

    /** Xử lý xác thực OTP: kiểm tra mã → kích hoạt tài khoản → redirect login. */
    private void handleVerifyOtp(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email   = req.getParameter("email");
        String action  = req.getParameter("action"); // "verify" hoặc "resend"
        String verifyEmail = (email != null) ? email.trim() : "";

        // Gửi lại OTP
        if ("resend".equals(action)) {
            boolean sent = userService.resendOtp(verifyEmail);
            req.setAttribute("verifyEmail", verifyEmail);
            req.setAttribute("successMessage", sent
                    ? "Đã gửi lại mã OTP mới về email của bạn!"
                    : "Không thể gửi lại. Email không tồn tại hoặc đã được xác thực.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/verify-otp.jsp").forward(req, resp);
            return;
        }

        // Xác thực OTP
        String otpCode = req.getParameter("otpCode");
        if (isBlank(otpCode) || isBlank(verifyEmail)) {
            req.setAttribute("errorMessage", "Thông tin không hợp lệ. Vui lòng thử lại.");
            req.setAttribute("verifyEmail", verifyEmail);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/verify-otp.jsp").forward(req, resp);
            return;
        }

        // Ghép 6 ô input nếu client gửi từng ký tự riêng (digit1..digit6)
        if (otpCode.isBlank()) {
            StringBuilder otp = new StringBuilder();
            for (int i = 1; i <= 6; i++) {
                String digit = req.getParameter("digit" + i);
                otp.append(digit != null ? digit.trim() : "");
            }
            otpCode = otp.toString();
        }

        boolean verified = userService.verifyEmailOtp(verifyEmail, otpCode.trim());

        if (verified) {
            resp.sendRedirect(req.getContextPath() + "/login?msg=verified");
        } else {
            req.setAttribute("errorMessage", "Mã OTP không đúng hoặc đã hết hạn. Vui lòng thử lại hoặc gửi lại mã mới.");
            req.setAttribute("verifyEmail", verifyEmail);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/verify-otp.jsp").forward(req, resp);
        }
    }

    /** Xử lý yêu cầu gửi link đặt lại mật khẩu qua email — M-01.2. */
    private void handleForgotPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        if (isBlank(email) || !ValidationUtil.isValidEmail(email)) {
            req.setAttribute("errorMessage", "Vui lòng nhập địa chỉ email hợp lệ.");
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/forgot-password.jsp").forward(req, resp);
            return;
        }

        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int serverPort = req.getServerPort();
        String contextPath = req.getContextPath();
        String baseUrl = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort) + contextPath;

        boolean sent = userService.sendPasswordResetEmail(email.trim(), baseUrl);
        if (sent) {
            req.setAttribute("successMessage", "Liên kết đặt lại mật khẩu đã được gửi đến " + email.trim() + ". Vui lòng kiểm tra hộp thư (thời hạn 15 phút).");
        } else {
            req.setAttribute("errorMessage", "Không tìm thấy tài khoản nào khớp với email này hoặc tài khoản chưa được kích hoạt.");
            req.setAttribute("prevEmail", email);
        }
        req.getRequestDispatcher("/WEB-INF/views/customer/account/forgot-password.jsp").forward(req, resp);
    }

    /** Xử lý đặt lại mật khẩu mới bằng token — M-01.2. */
    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token           = req.getParameter("token");
        String password        = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        if (isBlank(token)) {
            req.setAttribute("tokenInvalid", true);
            req.setAttribute("errorMessage", "Mã token không hợp lệ hoặc đã hết hạn.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        User user = userService.getUserByResetToken(token.trim());
        if (user == null) {
            req.setAttribute("tokenInvalid", true);
            req.setAttribute("errorMessage", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn (sau 15 phút). Vui lòng yêu cầu lại.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        req.setAttribute("token", token.trim());

        if (isBlank(password) || isBlank(confirmPassword)) {
            req.setAttribute("errorMessage", "Vui lòng nhập đầy đủ mật khẩu mới và xác nhận mật khẩu.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            req.setAttribute("errorMessage", "Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt (ví dụ: CineMax@2026).");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        boolean reset = userService.resetPasswordByToken(token.trim(), password);
        if (reset) {
            resp.sendRedirect(req.getContextPath() + "/login?msg=reset_success");
        } else {
            req.setAttribute("errorMessage", "Không thể đặt lại mật khẩu. Liên kết có thể đã hết hạn hoặc bị lỗi.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // PRIVATE UTILITY
    // ────────────────────────────────────────────────────────────────────────────

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("currentUser") != null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
