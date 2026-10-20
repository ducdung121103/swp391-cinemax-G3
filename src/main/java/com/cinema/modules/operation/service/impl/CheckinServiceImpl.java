package com.cinema.modules.operation.service.impl;

import com.cinema.model.Ticket;
import com.cinema.model.TicketCheckinLog;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import com.cinema.modules.operation.dao.TicketCheckinLogDAO;
import com.cinema.modules.operation.service.CheckinService;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Cài đặt nghiệp vụ Soát vé bằng Camera QR hoặc máy bắn mã vạch (TV 5).
 */
public class CheckinServiceImpl implements CheckinService {
    private final BookingEngineService bookingEngineService;
    private final TicketCheckinLogDAO checkinLogDAO;

    public CheckinServiceImpl() {
        this.bookingEngineService = new BookingEngineServiceImpl();
        this.checkinLogDAO = new TicketCheckinLogDAO();
    }

    public CheckinServiceImpl(BookingEngineService bookingEngineService, TicketCheckinLogDAO checkinLogDAO) {
        this.bookingEngineService = bookingEngineService;
        this.checkinLogDAO = checkinLogDAO;
    }

    @Override
    public TicketCheckinLog checkinTicket(String barcode, Long staffId) {
        Ticket ticket = bookingEngineService.getTicketByBarcode(barcode);

        TicketCheckinLog log = new TicketCheckinLog();
        log.setStaffId(staffId);
        log.setCheckinTime(LocalDateTime.now());

        if (ticket == null) {
            log.setStatus("REJECTED_NOT_FOUND");
            return log;
        }

        log.setTicketId(ticket.getId());
        log.setTicket(ticket);

        if ("USED".equalsIgnoreCase(ticket.getStatus()) || "CHECKED_IN".equalsIgnoreCase(ticket.getStatus())) {
            log.setStatus("REJECTED_ALREADY_USED");
            return log;
        }

        if (!"VALID".equalsIgnoreCase(ticket.getStatus())) {
            log.setStatus("REJECTED_INVALID_STATUS");
            return log;
        }

        // Hợp lệ -> Đánh dấu vé đã sử dụng (USED) và ghi log checkin thành công
        if (!bookingEngineService.updateTicketStatus(ticket.getId(), "USED")) {
            throw new IllegalStateException("Không thể cập nhật trạng thái vé sau khi soát.");
        }
        ticket.setStatus("USED");
        log.setStatus("SUCCESS");
        try {
            checkinLogDAO.insertLog(log);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể ghi nhật ký soát vé.", e);
        }
        return log;
    }
}
