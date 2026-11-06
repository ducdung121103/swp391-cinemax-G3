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
 * Controller xá»­ lÃ½ toÃ n bá»™ luá»“ng Identity:
 * - /login      â†’ ÄÄƒng nháº­p (BCrypt, phÃ¢n quyá»n redirect)
 * - /logout     â†’ ÄÄƒng xuáº¥t an toÃ n (há»§y session)
 * - /register   â†’ ÄÄƒng kÃ½ tÃ i khoáº£n (gá»­i OTP email)
 * - /verify-otp â†’ XÃ¡c thá»±c mÃ£ OTP 6 sá»‘ Ä‘á»ƒ kÃ­ch hoáº¡t tÃ i khoáº£n
 *
 * Module Identity â€” TV 2 (CÆ°á»ng) â€” M-01.1, M-01.2
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout", "/register", "/verify-otp", "/forgot-password", "/reset-password"})
public class AuthServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // GET HANDLERS
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        // --- /logout: Há»§y session vÃ  redirect vá» login ---
        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login?msg=logged_out");
            return;
        }

        // --- /register: Hiá»ƒn thá»‹ form Ä‘Äƒng kÃ½ ---
        if ("/register".equals(path)) {
            // Náº¿u Ä‘Ã£ Ä‘Äƒng nháº­p â†’ vá» trang chá»§
            if (isLoggedIn(req)) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp")
               .forward(req, resp);
            return;
        }

        // --- /verify-otp: Hiá»ƒn thá»‹ form nháº­p OTP ---
        if ("/verify-otp".equals(path)) {
            String email = req.getParameter("email");
            if (email == null || email.isBlank()) {
                // KhÃ´ng cÃ³ email â†’ vá» trang Ä‘Äƒng kÃ½
                resp.sendRedirect(req.getContextPath() + "/register");
                return;
            }
            req.setAttribute("verifyEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/verify-otp.jsp")
               .forward(req, resp);
            return;
        }

        // --- /forgot-password: Hiá»ƒn thá»‹ form quÃªn máº­t kháº©u ---
        if ("/forgot-password".equals(path)) {
            if (isLoggedIn(req)) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
            req.getRequestDispatcher("/WEB-INF/views/customer/account/forgot-password.jsp")
               .forward(req, resp);
            return;
        }

        // --- /reset-password: Hiá»ƒn thá»‹ form Ä‘áº·t láº¡i máº­t kháº©u tá»« link email token ---
        if ("/reset-password".equals(path)) {
            String token = req.getParameter("token");
            if (isBlank(token)) {
                req.setAttribute("tokenInvalid", true);
                req.setAttribute("errorMessage", "ÄÆ°á»ng dáº«n khÃ´ng há»£p lá»‡ hoáº·c thiáº¿u mÃ£ token xÃ¡c thá»±c.");
                req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp")
                   .forward(req, resp);
                return;
            }
            User user = userService.getUserByResetToken(token.trim());
            if (user == null) {
                req.setAttribute("tokenInvalid", true);
                req.setAttribute("errorMessage", "LiÃªn káº¿t Ä‘áº·t láº¡i máº­t kháº©u khÃ´ng há»£p lá»‡ hoáº·c Ä‘Ã£ háº¿t háº¡n (sau 15 phÃºt). Vui lÃ²ng yÃªu cáº§u liÃªn káº¿t má»›i.");
                req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp")
                   .forward(req, resp);
                return;
            }
            req.setAttribute("token", token.trim());
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp")
               .forward(req, resp);
            return;
        }

        // --- /login: Hiá»ƒn thá»‹ form Ä‘Äƒng nháº­p ---
        if (isLoggedIn(req)) {
            // ÄÃ£ Ä‘Äƒng nháº­p â†’ vá» trang chá»§ (hoáº·c redirect param náº¿u cÃ³)
            String redirect = req.getParameter("redirect");
            resp.sendRedirect(redirect != null && !redirect.isBlank()
                    ? redirect
                    : req.getContextPath() + "/");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp")
           .forward(req, resp);
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // POST HANDLERS
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        // â”€â”€ POST /login â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if ("/login".equals(path)) {
            handleLogin(req, resp);
            return;
        }

        // â”€â”€ POST /register â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if ("/register".equals(path)) {
            handleRegister(req, resp);
            return;
        }

        // â”€â”€ POST /verify-otp â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if ("/verify-otp".equals(path)) {
            handleVerifyOtp(req, resp);
            return;
        }

        // â”€â”€ POST /forgot-password â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if ("/forgot-password".equals(path)) {
            handleForgotPassword(req, resp);
            return;
        }

        // â”€â”€ POST /reset-password â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if ("/reset-password".equals(path)) {
            handleResetPassword(req, resp);
            return;
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // PRIVATE BUSINESS HANDLERS
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Xá»­ lÃ½ Ä‘Äƒng nháº­p: xÃ¡c thá»±c BCrypt, phÃ¢n quyá»n redirect. */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        // Validate input cÆ¡ báº£n
        if (isBlank(email) || isBlank(password)) {
            req.setAttribute("errorMessage", "Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ email vÃ  máº­t kháº©u.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp").forward(req, resp);
            return;
        }

        User user = userService.authenticate(email.trim(), password);

        if (user == null) {
            req.setAttribute("errorMessage", "Email hoáº·c máº­t kháº©u khÃ´ng chÃ­nh xÃ¡c!");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp").forward(req, resp);
            return;
        }

        // TÃ i khoáº£n chÆ°a xÃ¡c thá»±c OTP â†’ chuyá»ƒn vá» trang verify
        if ("UNVERIFIED".equalsIgnoreCase(user.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/verify-otp?email="
                    + java.net.URLEncoder.encode(user.getEmail(), "UTF-8")
                    + "&msg=need_verify");
            return;
        }

        // TÃ i khoáº£n bá»‹ khÃ³a (LOCKED)
        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            req.setAttribute("errorMessage", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp").forward(req, resp);
            return;
        }

        // ÄÄƒng nháº­p thÃ nh cÃ´ng â†’ táº¡o session
        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);
        session.setMaxInactiveInterval(60 * 60); // Session timeout 1 giá»

        // Redirect theo redirect param hoáº·c theo role
        String redirectParam = req.getParameter("redirect");
        String role = (user.getRole() != null) ? user.getRole().getRoleName() : RoleConstant.CUSTOMER;

        if (redirectParam != null && !redirectParam.isBlank()
                && redirectParam.startsWith(req.getContextPath())) {
            // Äá»‘i vá»›i STAFF, chá»‰ cho phÃ©p redirect Ä‘áº¿n URL mÃ  STAFF cÃ³ quyá»n (pos hoáº·c scanner)
            if (!RoleConstant.STAFF.equalsIgnoreCase(role)
                    || redirectParam.contains("/operation/pos")
                    || redirectParam.contains("/admin/operation/scanner")) {
                resp.sendRedirect(redirectParam);
                return;
            }
        }

        if (RoleConstant.ADMIN.equalsIgnoreCase(role)
                || RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else if (RoleConstant.STAFF.equalsIgnoreCase(role)) {
            resp.sendRedirect(req.getContextPath() + "/operation/pos");
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    /** Xá»­ lÃ½ Ä‘Äƒng kÃ½: validate, táº¡o user UNVERIFIED, gá»­i OTP email. */
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullName         = req.getParameter("fullName");
        String phone            = req.getParameter("phone");
        String email            = req.getParameter("email");
        String password         = req.getParameter("password");
        String confirmPassword  = req.getParameter("confirmPassword");

        // Validate phÃ­a server
        if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
            req.setAttribute("errorMessage", "Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ thÃ´ng tin báº¯t buá»™c.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            req.setAttribute("errorMessage", "Äá»‹nh dáº¡ng email khÃ´ng há»£p lá»‡ (vÃ­ dá»¥: name@gmail.com).");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }
        if (!isBlank(phone) && !ValidationUtil.isValidPhone(phone)) {
            req.setAttribute("errorMessage", "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡. Pháº£i lÃ  sá»‘ di Ä‘á»™ng VN gá»“m 10 sá»‘ (báº¯t Ä‘áº§u báº±ng 03, 05, 07, 08, 09).");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }
        if (!password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Máº­t kháº©u xÃ¡c nháº­n khÃ´ng khá»›p!");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }
        if (!ValidationUtil.isValidPassword(password)) {
            req.setAttribute("errorMessage", "Máº­t kháº©u pháº£i cÃ³ Ã­t nháº¥t 8 kÃ½ tá»±, bao gá»“m Ã­t nháº¥t 1 chá»¯ hoa, 1 chá»¯ thÆ°á»ng, 1 sá»‘ vÃ  1 kÃ½ tá»± Ä‘áº·c biá»‡t (vÃ­ dá»¥: CineMax@2026).");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }

        // Kiá»ƒm tra email Ä‘Ã£ Ä‘Äƒng kÃ½
        if (userService.isEmailTaken(email)) {
            req.setAttribute("errorMessage", "Email nÃ y Ä‘Ã£ Ä‘Æ°á»£c Ä‘Äƒng kÃ½. Vui lÃ²ng dÃ¹ng email khÃ¡c hoáº·c Ä‘Äƒng nháº­p.");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }

        // Kiá»ƒm tra sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Äƒng kÃ½ (náº¿u cÃ³ nháº­p)
        if (phone != null && !phone.isBlank() && userService.isPhoneTaken(phone)) {
            req.setAttribute("errorMessage", "Sá»‘ Ä‘iá»‡n thoáº¡i nÃ y Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng cho tÃ i khoáº£n khÃ¡c. Vui lÃ²ng nháº­p sá»‘ khÃ¡c.");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }

        User created = userService.registerCustomer(email.trim(), password, fullName.trim(), phone);

        if (created != null) {
            // ThÃ nh cÃ´ng â†’ chuyá»ƒn Ä‘áº¿n trang nháº­p OTP
            resp.sendRedirect(req.getContextPath() + "/verify-otp?email="
                    + java.net.URLEncoder.encode(created.getEmail(), "UTF-8")
                    + "&msg=otp_sent");
        } else {
            // Tháº¥t báº¡i do lá»—i há»‡ thá»‘ng
            req.setAttribute("errorMessage", "ÄÄƒng kÃ½ khÃ´ng thÃ nh cÃ´ng. Vui lÃ²ng kiá»ƒm tra láº¡i thÃ´ng tin.");
            req.setAttribute("prevFullName", fullName);
            req.setAttribute("prevPhone", phone);
            req.setAttribute("prevEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
        }
    }

    /** Xá»­ lÃ½ xÃ¡c thá»±c OTP: kiá»ƒm tra mÃ£ â†’ kÃ­ch hoáº¡t tÃ i khoáº£n â†’ redirect login. */
    private void handleVerifyOtp(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email   = req.getParameter("email");
        String action  = req.getParameter("action"); // "verify" hoáº·c "resend"
        String verifyEmail = (email != null) ? email.trim() : "";

        // Gá»­i láº¡i OTP
        if ("resend".equals(action)) {
            boolean sent = userService.resendOtp(verifyEmail);
            req.setAttribute("verifyEmail", verifyEmail);
            req.setAttribute("successMessage", sent
                    ? "ÄÃ£ gá»­i láº¡i mÃ£ OTP má»›i vá» email cá»§a báº¡n!"
                    : "KhÃ´ng thá»ƒ gá»­i láº¡i. Email khÃ´ng tá»“n táº¡i hoáº·c Ä‘Ã£ Ä‘Æ°á»£c xÃ¡c thá»±c.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/verify-otp.jsp").forward(req, resp);
            return;
        }

        // XÃ¡c thá»±c OTP
        String otpCode = req.getParameter("otpCode");
        if (isBlank(otpCode) || isBlank(verifyEmail)) {
            req.setAttribute("errorMessage", "ThÃ´ng tin khÃ´ng há»£p lá»‡. Vui lÃ²ng thá»­ láº¡i.");
            req.setAttribute("verifyEmail", verifyEmail);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/verify-otp.jsp").forward(req, resp);
            return;
        }

        // GhÃ©p 6 Ã´ input náº¿u client gá»­i tá»«ng kÃ½ tá»± riÃªng (digit1..digit6)
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
            req.setAttribute("errorMessage", "MÃ£ OTP khÃ´ng Ä‘Ãºng hoáº·c Ä‘Ã£ háº¿t háº¡n. Vui lÃ²ng thá»­ láº¡i hoáº·c gá»­i láº¡i mÃ£ má»›i.");
            req.setAttribute("verifyEmail", verifyEmail);
            req.getRequestDispatcher("/WEB-INF/views/customer/account/verify-otp.jsp").forward(req, resp);
        }
    }

    /** Xá»­ lÃ½ yÃªu cáº§u gá»­i link Ä‘áº·t láº¡i máº­t kháº©u qua email â€” M-01.2. */
    private void handleForgotPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        if (isBlank(email) || !ValidationUtil.isValidEmail(email)) {
            req.setAttribute("errorMessage", "Vui lÃ²ng nháº­p Ä‘á»‹a chá»‰ email há»£p lá»‡.");
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
            req.setAttribute("successMessage", "LiÃªn káº¿t Ä‘áº·t láº¡i máº­t kháº©u Ä‘Ã£ Ä‘Æ°á»£c gá»­i Ä‘áº¿n " + email.trim() + ". Vui lÃ²ng kiá»ƒm tra há»™p thÆ° (thá»i háº¡n 15 phÃºt).");
        } else {
            req.setAttribute("errorMessage", "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n nÃ o khá»›p vá»›i email nÃ y hoáº·c tÃ i khoáº£n chÆ°a Ä‘Æ°á»£c kÃ­ch hoáº¡t.");
            req.setAttribute("prevEmail", email);
        }
        req.getRequestDispatcher("/WEB-INF/views/customer/account/forgot-password.jsp").forward(req, resp);
    }

    /** Xá»­ lÃ½ Ä‘áº·t láº¡i máº­t kháº©u má»›i báº±ng token â€” M-01.2. */
    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token           = req.getParameter("token");
        String password        = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        if (isBlank(token)) {
            req.setAttribute("tokenInvalid", true);
            req.setAttribute("errorMessage", "MÃ£ token khÃ´ng há»£p lá»‡ hoáº·c Ä‘Ã£ háº¿t háº¡n.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        User user = userService.getUserByResetToken(token.trim());
        if (user == null) {
            req.setAttribute("tokenInvalid", true);
            req.setAttribute("errorMessage", "LiÃªn káº¿t Ä‘áº·t láº¡i máº­t kháº©u khÃ´ng há»£p lá»‡ hoáº·c Ä‘Ã£ háº¿t háº¡n (sau 15 phÃºt). Vui lÃ²ng yÃªu cáº§u láº¡i.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        req.setAttribute("token", token.trim());

        if (isBlank(password) || isBlank(confirmPassword)) {
            req.setAttribute("errorMessage", "Vui lÃ²ng nháº­p Ä‘áº§y Ä‘á»§ máº­t kháº©u má»›i vÃ  xÃ¡c nháº­n máº­t kháº©u.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Máº­t kháº©u xÃ¡c nháº­n khÃ´ng khá»›p!");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            req.setAttribute("errorMessage", "Máº­t kháº©u pháº£i cÃ³ Ã­t nháº¥t 8 kÃ½ tá»±, bao gá»“m Ã­t nháº¥t 1 chá»¯ hoa, 1 chá»¯ thÆ°á»ng, 1 sá»‘ vÃ  1 kÃ½ tá»± Ä‘áº·c biá»‡t (vÃ­ dá»¥: CineMax@2026).");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
            return;
        }

        boolean reset = userService.resetPasswordByToken(token.trim(), password);
        if (reset) {
            resp.sendRedirect(req.getContextPath() + "/login?msg=reset_success");
        } else {
            req.setAttribute("errorMessage", "KhÃ´ng thá»ƒ Ä‘áº·t láº¡i máº­t kháº©u. LiÃªn káº¿t cÃ³ thá»ƒ Ä‘Ã£ háº¿t háº¡n hoáº·c bá»‹ lá»—i.");
            req.getRequestDispatcher("/WEB-INF/views/customer/account/reset-password.jsp").forward(req, resp);
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // PRIVATE UTILITY
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("currentUser") != null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

