package com.cinema.modules.operation.service;

import com.cinema.model.TicketCheckinLog;

/**
 * Public Service Interface quản lý soát vé qua mã QR / Barcode tại cửa phòng chiếu (TV 5).
 */
public interface CheckinService {
    TicketCheckinLog checkinTicket(String barcode, Long staffId);
}
