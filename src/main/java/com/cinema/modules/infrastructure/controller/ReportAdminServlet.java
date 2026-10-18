package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
import com.cinema.modules.infrastructure.dao.InfrastructureReportDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller Báo cáo Tỷ lệ Lấp đầy Phòng chiếu & Suất chiếu (Branch Occupancy Report - SRS: M-10.3).
 * Do Dũng (Leader - TV 1) phụ trách.
 */
@WebServlet(name = "ReportAdminServlet", urlPatterns = {"/admin/infrastructure/occupancy", "/admin/reports/occupancy", "/admin/reports"})
public class ReportAdminServlet extends HttpServlet {

    private final InfrastructureReportDAO reportDAO = new InfrastructureReportDAO();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        // Tải danh sách bộ lọc
        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        List<ScreeningRoom> rooms = (cinemaId != null && cinemaId > 0)
                ? screeningRoomService.getRoomsByCinema(cinemaId)
                : screeningRoomService.getAllRoomsWithCinema(null, null, null);

        // Báo cáo tổng hợp theo phòng chiếu
        List<InfrastructureReportDAO.RoomOccupancyDTO> roomSummaries = reportDAO.getRoomOccupancySummaries(cinemaId, roomType);

        // Báo cáo chi tiết theo từng suất chiếu
        List<InfrastructureReportDAO.ShowtimeOccupancyDTO> showtimeDetails = reportDAO.getShowtimeOccupancyDetails(cinemaId, roomId, roomType, timeSlot);

        // Tính toán các chỉ số KPI toàn cục từ dữ liệu suất chiếu phân tích
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

        // Truyền dữ liệu ra View
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
