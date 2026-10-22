package com.cinema.modules.operation.controller;

import com.cinema.model.User;
import com.cinema.modules.operation.dto.PosCheckoutCommand;
import com.cinema.modules.operation.dto.PosCheckoutResult;
import com.cinema.modules.operation.dto.PosCounterPageData;
import com.cinema.modules.operation.service.CheckoutConfirmationRequiredException;
import com.cinema.modules.operation.service.PosCounterService;
import com.cinema.modules.operation.service.impl.PosCounterServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(name = "PosCounterServlet", urlPatterns = {"/operation/pos"})
public class PosCounterServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/admin/operation/pos-counter.jsp";
    private final PosCounterService posCounterService = new PosCounterServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User staff = currentUser(request);
        if (staff == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=login_required");
            return;
        }

        try {
            PosCounterPageData page = posCounterService.loadPage(staff, request.getParameter("cinemaId"),
                    request.getParameter("date"), request.getParameter("showtimeId"), request.getSession().getId());
            request.setAttribute("staff", staff);
            request.setAttribute("today", page.getToday());
            request.setAttribute("selectedDate", page.getSelectedDate());
            request.setAttribute("cinemaId", page.getCinemaId());
            request.setAttribute("cinemas", page.getCinemas());
            request.setAttribute("showtimes", page.getShowtimes());
            request.setAttribute("fnbItems", page.getFnbItems());
            request.setAttribute("selectedShowtime", page.getSelectedShowtimeId());
            request.setAttribute("selectedShowtimeDetails", page.getSelectedShowtime());
            request.setAttribute("seatMap", page.getSeatMap());
            request.setAttribute("seatPrices", page.getSeatPrices());
            request.setAttribute("availableSeatCount", page.getAvailableSeatCount());
            request.setAttribute("seatColumnCount", page.getSeatColumnCount());
            request.setAttribute("studentDiscount", page.getStudentDiscount());
            request.setAttribute("seniorDiscount", page.getSeniorDiscount());
            request.setAttribute("posSetupMessage", page.getSetupMessage());
            request.setAttribute("posError", page.getErrorMessage());

            HttpSession session = request.getSession();
            Object flashError = session.getAttribute("posError");
            if (flashError != null) {
                request.setAttribute("posError", flashError);
            }
            session.removeAttribute("posError");
            request.setAttribute("checkoutResult", session.getAttribute("posCheckoutResult"));
            session.removeAttribute("posCheckoutResult");
            request.setAttribute("checkoutShowtime", session.getAttribute("posCheckoutShowtime"));
            session.removeAttribute("posCheckoutShowtime");
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException("Không thể tải dữ liệu quầy POS.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User staff = currentUser(request);
        if (staff == null) {
            response.sendRedirect(request.getContextPath() + "/login?msg=login_required");
            return;
        }

        try {
            PosCheckoutResult checkout = posCounterService.checkout(toCommand(request, staff));
            HttpSession session = request.getSession();
            session.setAttribute("posCheckoutResult", checkout.getBookingResult());
            session.setAttribute("posCheckoutShowtime", checkout.getShowtime());
            response.sendRedirect(posUrl(request, checkout.getCinemaId().toString(),
                    checkout.getDate().toString(), null));
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (CheckoutConfirmationRequiredException e) {
            redirectWithError(request, response, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectWithError(request, response, e.getMessage());
        } catch (Exception e) {
            throw new ServletException("Không thể tạo giao dịch POS.", e);
        }
    }

    private PosCheckoutCommand toCommand(HttpServletRequest request, User staff) {
        return PosCheckoutCommand.builder()
                .staff(staff)
                .requestedCinemaId(request.getParameter("cinemaId"))
                .date(request.getParameter("date"))
                .showtimeId(request.getParameter("showtimeId"))
                .checkoutConfirmed(request.getParameter("checkoutConfirmed"))
                .seatIds(request.getParameterValues("seatIds"))
                .ticketClass(request.getParameter("ticketClass"))
                .paymentMethod(request.getParameter("paymentMethod"))
                .sessionId(request.getSession().getId())
                .fnbQuantities(fnbQuantities(request.getParameterMap()))
                .build();
    }

    private Map<String, String> fnbQuantities(Map<String, String[]> parameters) {
        Map<String, String> quantities = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            if (!entry.getKey().startsWith("fnb_")) {
                continue;
            }
            String[] values = entry.getValue();
            quantities.put(entry.getKey().substring(4), values == null || values.length == 0 ? null : values[0]);
        }
        return quantities;
    }

    private User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("currentUser");
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        request.getSession().setAttribute("posError", message);
        response.sendRedirect(posUrl(request, request.getParameter("cinemaId"),
                request.getParameter("date"), request.getParameter("showtimeId")));
    }

    private String posUrl(HttpServletRequest request, String cinemaId, String date, String showtimeId) {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/operation/pos?");
        appendParameter(url, "cinemaId", cinemaId);
        appendParameter(url, "date", date);
        appendParameter(url, "showtimeId", showtimeId);
        if (url.charAt(url.length() - 1) == '&') {
            url.setLength(url.length() - 1);
        }
        if (url.charAt(url.length() - 1) == '?') {
            url.append("date=").append(java.time.LocalDate.now());
        }
        return url.toString();
    }

    private void appendParameter(StringBuilder url, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        url.append(name).append('=')
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                .append('&');
    }
}
