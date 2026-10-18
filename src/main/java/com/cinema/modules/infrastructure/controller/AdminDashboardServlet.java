package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.User;
import com.cinema.modules.infrastructure.dao.InfrastructureReportDAO;
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
import java.util.List;

/**
 * Controller hiển thị Bảng điều khiển Quản trị Trung tâm (Admin Dashboard - Function 6 - Iter 2).
 * Do Leader (TV 1 - Dũng) phụ trách quản trị tổng quan hệ sinh thái rạp.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();
    private final InfrastructureReportDAO reportDAO = new InfrastructureReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        // 1. KPI chỉ số toàn cục từ database
        BigDecimal totalRevenue = reportDAO.getTotalRevenue();
        int totalConfirmedBookings = reportDAO.getTotalConfirmedBookings();
        int totalValidTickets = reportDAO.getTotalValidTickets();
        double avgOccupancyRate = reportDAO.getAverageOccupancyRate();

        int totalCinemas = screeningRoomService.getTotalCinemasCount();
        int totalRooms = screeningRoomService.getTotalRoomsCount();
        int totalSeats = screeningRoomService.getTotalSeatsCount();
        int maintenanceSeats = reportDAO.getMaintenanceSeatsCount();

        // 2. Hiệu suất kinh doanh theo cụm rạp
        List<InfrastructureReportDAO.BranchPerformanceDTO> branchPerformances = reportDAO.getBranchPerformanceList();

        // 3. Giao dịch đặt vé & thanh toán mới nhất
        List<InfrastructureReportDAO.RecentTransactionDTO> recentTransactions = reportDAO.getRecentTransactions(8);

        // 4. Danh sách cụm rạp và phòng chiếu
        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        List<ScreeningRoom> recentRooms = screeningRoomService.getAllRoomsWithCinema(null, null, null);

        // Truyền attributes ra JSP
        req.setAttribute("totalRevenue", totalRevenue);
        req.setAttribute("totalConfirmedBookings", totalConfirmedBookings);
        req.setAttribute("totalValidTickets", totalValidTickets);
        req.setAttribute("avgOccupancyRate", avgOccupancyRate);

        req.setAttribute("totalCinemas", totalCinemas > 0 ? totalCinemas : (cinemas != null ? cinemas.size() : 0));
        req.setAttribute("totalRooms", totalRooms > 0 ? totalRooms : (recentRooms != null ? recentRooms.size() : 0));
        req.setAttribute("totalSeats", totalSeats);
        req.setAttribute("maintenanceSeats", maintenanceSeats);

        req.setAttribute("branchPerformances", branchPerformances);
        req.setAttribute("recentTransactions", recentTransactions);
        req.setAttribute("cinemas", cinemas);
        req.setAttribute("recentRooms", recentRooms);

        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
