package com.cinema.modules.booking.controller;

import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import com.cinema.modules.operation.dao.PosSeatDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sơ đồ ghế cho khách sau khi đã xem chi tiết phim của suất đã chọn.
 */
@WebServlet(name = "SeatSelectionServlet", urlPatterns = {"/booking/seats"})
public class SeatSelectionServlet extends HttpServlet {
    private static final DateTimeFormatter SHOWTIME_TEXT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final ShowtimeService showtimeService = new ShowtimeServiceImpl();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();
    private final PosSeatDAO posSeatDAO = new PosSeatDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Showtime showtime = loadScheduled(req.getParameter("showtimeId"));
        if (showtime == null) {
            resp.sendRedirect(req.getContextPath() + "/showtimes");
            return;
        }

        List<Seat> seats = screeningRoomService.getRoomSeatMatrix(showtime.getScreeningRoomId());
        Set<Long> availableIds = new HashSet<>();
        try {
            String sessionId = req.getSession().getId();
            for (Seat seat : posSeatDAO.findForShowtime(showtime.getId(), showtime.getScreeningRoomId(), sessionId)) {
                if ("AVAILABLE".equals(seat.getStatus())) {
                    availableIds.add(seat.getId());
                }
            }
        } catch (SQLException e) {
            req.setAttribute("seatError", "Không tải được ghế trống của suất này.");
        }

        req.setAttribute("showtime", showtime);
        req.setAttribute("showtimeText", showtime.getStartTime().format(SHOWTIME_TEXT));
        req.setAttribute("seatRows", groupByRow(seats));
        req.setAttribute("availableIds", availableIds);
        req.getRequestDispatcher("/WEB-INF/views/customer/booking/seats.jsp").forward(req, resp);
    }

    private Showtime loadScheduled(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            Showtime showtime = showtimeService.getShowtimeById(Long.parseLong(raw.trim()));
            if (showtime == null || showtime.getStartTime() == null || showtime.getScreeningRoomId() == null) {
                return null;
            }
            return "SCHEDULED".equals(showtime.getStatus()) ? showtime : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<List<Seat>> groupByRow(List<Seat> seats) {
        List<List<Seat>> rows = new ArrayList<>();
        List<Seat> current = null;
        String rowName = null;
        for (Seat seat : seats) {
            String next = seat.getSeatRow() == null ? "" : seat.getSeatRow();
            if (current == null || !next.equals(rowName)) {
                current = new ArrayList<>();
                rows.add(current);
                rowName = next;
            }
            current.add(seat);
        }
        return rows;
    }
}
