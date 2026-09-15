package com.cinema.modules.operation.service.impl;

import com.cinema.model.Ticket;
import com.cinema.model.TicketCheckinLog;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import com.cinema.modules.operation.dao.TicketCheckinLogDAO;
import com.cinema.modules.operation.service.CheckinService;

import java.time.LocalDateTime;

/**
 * Cài đặt nghiệp vụ Soát vé bằng Camera QR hoặc máy bắn mã vạch (TV 5).
 */
public class CheckinServiceImpl implements CheckinService {
    private final BookingEngineService bookingEngineService = new BookingEngineServiceImpl();
    private final TicketCheckinLogDAO checkinLogDAO = new TicketCheckinLogDAO();

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

        if ("CHECKED_IN".equalsIgnoreCase(ticket.getStatus())) {
            log.setStatus("REJECTED_ALREADY_USED");
            return log;
        }

        if (!"VALID".equalsIgnoreCase(ticket.getStatus())) {
            log.setStatus("REJECTED_INVALID_STATUS");
            return log;
        }

        // Hợp lệ -> Checkin thành công
        log.setStatus("SUCCESS");
        checkinLogDAO.insertLog(log);
        return log;
    }
}
