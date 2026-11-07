package com.cinema.modules.operation.controller;

import com.cinema.model.User;
import com.cinema.modules.booking.dao.BookingDAO;
import com.cinema.modules.booking.dto.CustomerBookingHistoryDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller phục vụ tra cứu mã đơn hàng và in vé cứng tại quầy (dành cho Staff và Manager).
 * Hỗ trợ tìm kiếm theo ký tự (mã đơn, tên khách, SĐT) và in vé cho khách mua online lẫn bán tại quầy POS.
 */
@WebServlet(name = "TicketPrintServlet", urlPatterns = {"/operation/ticket-print", "/manager/ticket-print"})
public class TicketPrintServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?msg=login_required");
            return;
        }

        String keyword = req.getParameter("keyword");
        if (keyword == null || keyword.isBlank()) {
            keyword = req.getParameter("code");
        }

        Long cinemaId = currentUser.getCinemaId();

        if (keyword != null && !keyword.isBlank()) {
            keyword = keyword.trim();
            req.setAttribute("searchedCode", keyword);

            // Tìm kiếm ký tự (mã đơn, tên khách hàng, SĐT)
            List<CustomerBookingHistoryDTO> searchResults = bookingDAO.searchBookingsForPrinting(keyword, cinemaId, 20);

            if (searchResults.isEmpty()) {
                req.setAttribute("errorMessage", "Không tìm thấy đơn hàng nào khớp với từ khóa: \"" + keyword + "\"");
            } else if (searchResults.size() == 1) {
                // Khớp duy nhất 1 đơn hàng -> Mở trực tiếp để in vé
                req.setAttribute("booking", searchResults.get(0));
            } else {
                // Khớp nhiều đơn hàng: kiểm tra xem có đơn nào trùng khớp chính xác 100% mã đơn hay không
                CustomerBookingHistoryDTO exactMatch = null;
                for (CustomerBookingHistoryDTO dto : searchResults) {
                    if (dto.getBookingCode() != null && dto.getBookingCode().equalsIgnoreCase(keyword)) {
                        exactMatch = dto;
                        break;
                    }
                }

                if (exactMatch != null) {
                    req.setAttribute("booking", exactMatch);
                } else {
                    req.setAttribute("searchResults", searchResults);
                    req.setAttribute("infoMessage", "Tìm thấy " + searchResults.size() + " đơn hàng phù hợp với từ khóa \"" + keyword + "\". Vui lòng chọn đơn cần in bên dưới:");
                }
            }
        }

        List<CustomerBookingHistoryDTO> recentBookings = bookingDAO.findRecentConfirmedBookings(cinemaId, 15);
        req.setAttribute("recentBookings", recentBookings);
        req.setAttribute("staff", currentUser);

        req.getRequestDispatcher("/WEB-INF/views/admin/operation/ticket-print.jsp").forward(req, resp);
    }
}
