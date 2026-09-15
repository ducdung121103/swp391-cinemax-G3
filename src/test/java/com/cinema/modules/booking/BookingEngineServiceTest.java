package com.cinema.modules.booking;

import com.cinema.common.transaction.TransactionManager;
import com.cinema.model.Booking;
import com.cinema.model.Ticket;
import com.cinema.modules.booking.dao.*;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for BookingEngineService (TV 4 Core Booking Engine)")
class BookingEngineServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private BookingDAO bookingDAO;

    @Mock
    private TicketDAO ticketDAO;

    @Mock
    private OrderItemDAO orderItemDAO;

    @Mock
    private PaymentDAO paymentDAO;

    @Mock
    private SeatHoldingDAO seatHoldingDAO;

    private BookingEngineService bookingEngineService;

    @BeforeEach
    void setUp() {
        TransactionManager.setTestConnection(mockConnection);
        bookingEngineService = new BookingEngineServiceImpl(bookingDAO, ticketDAO, orderItemDAO, paymentDAO, seatHoldingDAO);
    }

    @AfterEach
    void tearDown() {
        TransactionManager.clearTestConnection();
    }

    @Test
    @DisplayName("Should successfully create booking calculating tickets and F&B total accurately")
    void testCreateBookingWithFnb() throws SQLException {
        CreateBookingDTO dto = CreateBookingDTO.builder()
                .userId(5L)
                .showtimeId(1L)
                .seatIds(List.of(10L, 11L))
                .ticketPrices(List.of(new BigDecimal("85000.00"), new BigDecimal("85000.00")))
                .fnbItems(Map.of(8L, 1)) // 1 Solo Combo
                .channel("ONLINE")
                .paymentMethod("VNPAY")
                .sessionId("SESSION-12345")
                .build();

        when(orderItemDAO.calculateFnbTotal(any(), eq(dto.getFnbItems()))).thenReturn(new BigDecimal("79000.00"));
        when(bookingDAO.insertBooking(any(), any(Booking.class))).thenReturn(101L);

        BookingResult result = bookingEngineService.createBooking(dto);

        assertNotNull(result);
        assertEquals(101L, result.getBookingId());
        assertEquals("SUCCESS", result.getStatus());
        // 85k + 85k + 79k = 249,000 VND
        assertEquals(new BigDecimal("249000.00"), result.getFinalAmount());

        verify(ticketDAO, times(1)).insertTickets(any(), eq(101L), eq(1L), eq(dto.getSeatIds()), eq(dto.getTicketPrices()));
        verify(orderItemDAO, times(1)).insertFnbOrderItems(any(), eq(101L), eq(dto.getFnbItems()));
        verify(paymentDAO, times(1)).insertPayment(any(), eq(101L), eq("VNPAY"), eq(new BigDecimal("249000.00")));
        verify(seatHoldingDAO, times(1)).releaseHoldings(any(), eq(1L), eq(dto.getSeatIds()));
    }

    @Test
    @DisplayName("Should successfully hold seats for 5 minutes")
    void testHoldSeats() throws SQLException {
        List<Long> seatIds = List.of(20L, 21L);
        boolean held = bookingEngineService.holdSeats(1L, seatIds, "SESSION-ABC");

        assertTrue(held);
        verify(seatHoldingDAO, times(1)).holdSeat(any(), eq(1L), eq(20L), eq("SESSION-ABC"));
        verify(seatHoldingDAO, times(1)).holdSeat(any(), eq(1L), eq(21L), eq("SESSION-ABC"));
    }

    @Test
    @DisplayName("Should update ticket status via TicketDAO")
    void testUpdateTicketStatus() {
        when(ticketDAO.updateStatus(15L, "CHECKED_IN")).thenReturn(true);

        boolean updated = bookingEngineService.updateTicketStatus(15L, "CHECKED_IN");
        assertTrue(updated);
        verify(ticketDAO, times(1)).updateStatus(15L, "CHECKED_IN");
    }

    @Test
    @DisplayName("Should find ticket by barcode")
    void testGetTicketByBarcode() {
        Ticket mockTicket = Ticket.builder().barcode("TK-TEST").build();
        when(ticketDAO.findByBarcode("TK-TEST")).thenReturn(mockTicket);

        Ticket found = bookingEngineService.getTicketByBarcode("TK-TEST");
        assertNotNull(found);
        assertEquals("TK-TEST", found.getBarcode());
    }
}
