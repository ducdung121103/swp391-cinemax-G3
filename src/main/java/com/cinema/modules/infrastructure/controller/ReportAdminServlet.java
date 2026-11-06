package com.cinema.modules.infrastructure.controller;

import com.cinema.common.dto.PageResult;
import com.cinema.common.constant.RoleConstant;
import com.cinema.model.Cinema;
import com.cinema.model.User;
import com.cinema.model.Movie;
import com.cinema.model.ScreeningRoom;
import com.cinema.modules.infrastructure.dao.InfrastructureReportDAO;
import com.cinema.modules.infrastructure.dto.BookingDetailDTO;
import com.cinema.modules.infrastructure.dto.RevenueBreakdownDTO;
import com.cinema.modules.infrastructure.dto.RevenueFilterDTO;
import com.cinema.modules.infrastructure.dto.RevenueKpiDTO;
import com.cinema.modules.infrastructure.service.RevenueReportService;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.RevenueReportServiceImpl;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller for Reporting & Business Intelligence:
 * - Branch Occupancy Report (M-10.3)
 * - Revenue & Sales Reports (M-10.1)
 * Managed by DungBD (Leader - TV 1).
 */
@WebServlet(name = "ReportAdminServlet", urlPatterns = {
        "/admin/infrastructure/occupancy",
        "/admin/reports/occupancy",
        "/admin/reports",
        "/admin/reports/revenue"
})
public class ReportAdminServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ReportAdminServlet.class);

    private final InfrastructureReportDAO reportDAO = new InfrastructureReportDAO();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();
    private final RevenueReportService revenueReportService = new RevenueReportServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();

        if ("/admin/reports/revenue".equals(servletPath)) {
            handleRevenueReport(req, resp);
        } else {
            handleOccupancyReport(req, resp);
        }
    }

    /**
     * Handles Revenue & Sales Report screen (M-10.1).
     */
    private void handleRevenueReport(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            RevenueFilterDTO filter = revenueReportService.resolveAndValidateFilter(
                    req.getParameter("from"),
                    req.getParameter("to"),
                    req.getParameter("cinemaId"),
                    req.getParameter("page"),
                    req.getParameter("pageSize")
            );

            RevenueKpiDTO kpi = revenueReportService.getRevenueKpis(filter);
            List<RevenueBreakdownDTO> byMovie = revenueReportService.getRevenueByMovie(filter, 10);
            List<RevenueBreakdownDTO> byCinema = revenueReportService.getRevenueByCinema(filter);
            PageResult<BookingDetailDTO> bookingsPaged = revenueReportService.getBookingDetailsPaged(filter);

            List<Cinema> cinemas = revenueReportService.getAllCinemasForFilter();

            req.setAttribute("filter", filter);
            req.setAttribute("kpi", kpi);
            req.setAttribute("byMovie", byMovie);
            req.setAttribute("byCinema", byCinema);
            req.setAttribute("bookingsPaged", bookingsPaged);
            req.setAttribute("cinemas", cinemas);

            // Flash messages
            String msg = req.getParameter("msg");
            String error = req.getParameter("error");
            if (msg != null) req.setAttribute("msg", msg);
            if (error != null) req.setAttribute("error", error);

            req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/revenue-report.jsp").forward(req, resp);

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            RevenueFilterDTO safeFilter = revenueReportService.resolveAndValidateFilter(null, null, null, null, null);
            req.setAttribute("filter", safeFilter);
            req.setAttribute("kpi", revenueReportService.getRevenueKpis(safeFilter));
            req.setAttribute("cinemas", revenueReportService.getAllCinemasForFilter());
            req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/revenue-report.jsp").forward(req, resp);

        } catch (Exception e) {
            logger.error("Error displaying revenue report", e);
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error=" + URLEncoder.encode("Đã có lỗi xảy ra khi tải báo cáo doanh thu!", StandardCharsets.UTF_8));
        }
    }

    

    /**
     * Handles Branch Occupancy Report (M-10.3) - existing implementation preserved.
     */
    private void handleOccupancyReport(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cinemaIdParam = req.getParameter("cinemaId");
        String roomIdParam = req.getParameter("roomId");
        String roomType = req.getParameter("roomType");
        String timeSlot = req.getParameter("timeSlot");

        Long cinemaId = null;
        if (cinemaIdParam != null && !cinemaIdParam.isBlank() && !"ALL".equalsIgnoreCase(cinemaIdParam)) {
            try { cinemaId = Long.parseLong(cinemaIdParam.trim()); } catch (NumberFormatException ignored) {}
        }

        Long roomId = null;
        if (roomIdParam != null && !roomIdParam.isBlank() && !"ALL".equalsIgnoreCase(roomIdParam)) {
            try { roomId = Long.parseLong(roomIdParam.trim()); } catch (NumberFormatException ignored) {}
        }

        // Load filter items
        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        List<ScreeningRoom> rooms = (cinemaId != null && cinemaId > 0)
                ? screeningRoomService.getRoomsByCinema(cinemaId)
                : screeningRoomService.getAllRoomsWithCinema(null, null, null);

        // Summaries and showtime details
        List<InfrastructureReportDAO.RoomOccupancyDTO> roomSummaries = reportDAO.getRoomOccupancySummaries(cinemaId, roomType);
        List<InfrastructureReportDAO.ShowtimeOccupancyDTO> showtimeDetails = reportDAO.getShowtimeOccupancyDetails(cinemaId, roomId, roomType, timeSlot);

        int totalSold = 0;
        int totalSupply = 0;
        int primeSold = 0;
        int primeSupply = 0;
        int offPeakSold = 0;
        int offPeakSupply = 0;

        for (InfrastructureReportDAO.ShowtimeOccupancyDTO st : showtimeDetails) {
            totalSold += st.getSoldSeats();
            totalSupply += st.getCapacity();
            if ("PRIME_TIME".equalsIgnoreCase(st.getTimeSlot())) {
                primeSold += st.getSoldSeats();
                primeSupply += st.getCapacity();
            } else {
                offPeakSold += st.getSoldSeats();
                offPeakSupply += st.getCapacity();
            }
        }

        double overallOccupancyRate = totalSupply > 0 ? Math.round((totalSold * 100.0 / totalSupply) * 10.0) / 10.0 : 0.0;
        double primeTimeOccupancyRate = primeSupply > 0 ? Math.round((primeSold * 100.0 / primeSupply) * 10.0) / 10.0 : 0.0;
        double offPeakOccupancyRate = offPeakSupply > 0 ? Math.round((offPeakSold * 100.0 / offPeakSupply) * 10.0) / 10.0 : 0.0;

        req.setAttribute("cinemas", cinemas);
        req.setAttribute("rooms", rooms);
        req.setAttribute("selectedCinemaId", cinemaId);
        req.setAttribute("selectedRoomId", roomId);
        req.setAttribute("selectedRoomType", roomType != null ? roomType : "ALL");
        req.setAttribute("selectedTimeSlot", timeSlot != null ? timeSlot : "ALL");

        req.setAttribute("roomSummaries", roomSummaries);
        req.setAttribute("showtimeDetails", showtimeDetails);

        req.setAttribute("overallOccupancyRate", overallOccupancyRate);
        req.setAttribute("primeTimeOccupancyRate", primeTimeOccupancyRate);
        req.setAttribute("offPeakOccupancyRate", offPeakOccupancyRate);
        req.setAttribute("totalSoldSeats", totalSold);
        req.setAttribute("totalSeatSupply", totalSupply);
        req.setAttribute("totalShowtimesCount", showtimeDetails.size());

        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/occupancy-report.jsp").forward(req, resp);
    }
}
