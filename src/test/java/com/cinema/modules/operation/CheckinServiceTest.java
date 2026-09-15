package com.cinema.modules.operation;

import com.cinema.model.Ticket;
import com.cinema.model.TicketCheckinLog;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.operation.dao.TicketCheckinLogDAO;
import com.cinema.modules.operation.service.CheckinService;
import com.cinema.modules.operation.service.impl.CheckinServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for CheckinService (TV 5 QR Code Scanner)")
class CheckinServiceTest {

    @Mock
    private BookingEngineService bookingEngineService;

    @Mock
    private TicketCheckinLogDAO checkinLogDAO;

    private CheckinService checkinService;

    @BeforeEach
    void setUp() {
        checkinService = new CheckinServiceImpl(bookingEngineService, checkinLogDAO);
    }

    @Test
    @DisplayName("Should successfully check-in a valid ticket and update status to CHECKED_IN")
    void testCheckinSuccess() {
        Ticket validTicket = Ticket.builder()
                .bookingId(101L)
                .showtimeId(1L)
                .seatId(50L)
                .barcode("TK-VALID-12345")
                .ticketPrice(new BigDecimal("85000.00"))
                .status("VALID")
                .build();
        validTicket.setId(10L);

        when(bookingEngineService.getTicketByBarcode("TK-VALID-12345")).thenReturn(validTicket);

        TicketCheckinLog result = checkinService.checkinTicket("TK-VALID-12345", 3L);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(10L, result.getTicketId());
        assertEquals(3L, result.getStaffId());
        assertEquals("CHECKED_IN", validTicket.getStatus());

        verify(bookingEngineService, times(1)).updateTicketStatus(10L, "CHECKED_IN");
        verify(checkinLogDAO, times(1)).insertLog(any(TicketCheckinLog.class));
    }

    @Test
    @DisplayName("Should reject ticket if already checked-in (Anti-fraud duplicate entry)")
    void testCheckinAlreadyUsed() {
        Ticket usedTicket = Ticket.builder()
                .barcode("TK-USED-12345")
                .status("CHECKED_IN")
                .build();
        usedTicket.setId(11L);

        when(bookingEngineService.getTicketByBarcode("TK-USED-12345")).thenReturn(usedTicket);

        TicketCheckinLog result = checkinService.checkinTicket("TK-USED-12345", 3L);

        assertNotNull(result);
        assertEquals("REJECTED_ALREADY_USED", result.getStatus());

        verify(bookingEngineService, never()).updateTicketStatus(anyLong(), anyString());
        verify(checkinLogDAO, never()).insertLog(any(TicketCheckinLog.class));
    }

    @Test
    @DisplayName("Should reject checkin when ticket barcode does not exist")
    void testCheckinNotFound() {
        when(bookingEngineService.getTicketByBarcode("TK-NONEXISTENT")).thenReturn(null);

        TicketCheckinLog result = checkinService.checkinTicket("TK-NONEXISTENT", 3L);

        assertNotNull(result);
        assertEquals("REJECTED_NOT_FOUND", result.getStatus());
        assertNull(result.getTicketId());

        verify(checkinLogDAO, never()).insertLog(any());
    }

    @Test
    @DisplayName("Should reject checkin when ticket status is refunded or invalid")
    void testCheckinRefundedTicket() {
        Ticket refundedTicket = Ticket.builder()
                .barcode("TK-REFUNDED")
                .status("REFUNDED")
                .build();
        refundedTicket.setId(12L);

        when(bookingEngineService.getTicketByBarcode("TK-REFUNDED")).thenReturn(refundedTicket);

        TicketCheckinLog result = checkinService.checkinTicket("TK-REFUNDED", 3L);

        assertNotNull(result);
        assertEquals("REJECTED_INVALID_STATUS", result.getStatus());
    }
}
