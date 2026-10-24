package com.cinema.modules.catalog.controller;

import com.cinema.common.constant.RoleConstant;
import com.cinema.model.TicketPricing;
import com.cinema.model.User;
import com.cinema.modules.catalog.service.PricingService;
import com.cinema.modules.catalog.service.impl.PricingServiceImpl;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Cấu hình bảng giá vé và phần trăm giảm HSSV/Senior.
 */
@WebServlet(name = "PricingAdminServlet", urlPatterns = {"/admin/catalog/pricing"})
public class PricingAdminServlet extends HttpServlet {
    private final PricingService pricingService = new PricingServiceImpl();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User actor = currentUser(req);
        if (!canManage(actor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        req.setAttribute("prices", pricingService.listPrices());
        req.setAttribute("cinemas", screeningRoomService.getAllCinemas());
        req.setAttribute("seatTypes", screeningRoomService.getAllSeatTypes());
        req.setAttribute("studentDiscount", pricingService.getStudentDiscountPercent());
        req.setAttribute("seniorDiscount", pricingService.getSeniorDiscountPercent());
        req.setAttribute("actor", actor);
        req.setAttribute("msg", req.getParameter("msg"));
        req.setAttribute("error", req.getParameter("error"));
        req.getRequestDispatcher("/WEB-INF/views/admin/catalog/pricing.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User actor = currentUser(req);
        if (!canManage(actor)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String action = req.getParameter("action");
        String code;
        if ("discount".equals(action)) {
            code = pricingService.saveDiscountPercents(parseInt(req.getParameter("studentPercent")), parseInt(req.getParameter("seniorPercent")), actor);
        } else if ("delete".equals(action)) {
            code = pricingService.deletePrice(parseLong(req.getParameter("id")), actor);
        } else if ("save".equals(action)) {
            code = pricingService.savePrice(readPricing(req), actor);
        } else {
            code = "FAILED";
        }
        String key = "OK".equals(code) ? "msg" : "error";
        String value = "OK".equals(code) ? action : code;
        resp.sendRedirect(req.getContextPath() + "/admin/catalog/pricing?" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private TicketPricing readPricing(HttpServletRequest req) {
        TicketPricing pricing = new TicketPricing();
        pricing.setId(parseLong(req.getParameter("id")));
        pricing.setCinemaId(parseLong(req.getParameter("cinemaId")));
        pricing.setDayType(trim(req.getParameter("dayType")));
        pricing.setTimeSlot(trim(req.getParameter("timeSlot")));
        pricing.setExperienceFormat(trim(req.getParameter("experienceFormat")));
        pricing.setSeatTypeId(parseLong(req.getParameter("seatTypeId")));
        pricing.setEffectiveFrom(parseDate(req.getParameter("effectiveFrom")));
        pricing.setEffectiveTo(parseDate(req.getParameter("effectiveTo")));
        try {
            String raw = req.getParameter("basePrice");
            pricing.setBasePrice(raw == null || raw.isBlank() ? null : new BigDecimal(raw.trim()));
        } catch (NumberFormatException e) {
            pricing.setBasePrice(null);
        }
        return pricing;
    }

    private boolean canManage(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole().getRoleName();
        return RoleConstant.ADMIN.equalsIgnoreCase(role) || RoleConstant.MANAGER.equalsIgnoreCase(role);
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("currentUser");
        return value instanceof User ? (User) value : null;
    }

    private String trim(String raw) {
        return raw == null ? null : raw.trim().toUpperCase();
    }

    private Long parseLong(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private LocalDate parseDate(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
