package com.cinema.modules.booking;

import com.cinema.common.transaction.TransactionManager;
import com.cinema.model.Booking;
import com.cinema.model.Payment;
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
        assertEquals("PENDING", result.getStatus());
        // 85k + 85k + 79k = 249,000 VND
        assertEquals(new BigDecimal("249000.00"), result.getFinalAmount());

        verify(ticketDAO, times(1)).insertTickets(any(), eq(101L), eq(1L), eq(dto.getSeatIds()),
                eq(dto.getTicketPrices()), eq("PENDING"));
        verify(orderItemDAO, times(1)).insertFnbOrderItems(any(), eq(101L), eq(dto.getFnbItems()));
        verify(paymentDAO, times(1)).insertPendingVnpayPayment(any(), eq(101L), eq(new BigDecimal("249000.00")));
        verify(seatHoldingDAO, times(1)).releaseHoldings(any(), eq(1L), eq(dto.getSeatIds()));
    }

    @Test
    @DisplayName("Should confirm a pending VNPay booking only after a matching successful return")
    void testFinalizeSuccessfulVnpayPayment() throws SQLException {
        Booking booking = Booking.builder()
                .bookingCode("BK-1")
                .showtimeId(1L)
                .finalAmount(new BigDecimal("100000.00"))
                .status("PENDING")
                .build();
        booking.setId(101L);
        Payment payment = Payment.builder()
                .bookingId(101L)
                .paymentMethod("VNPAY")
                .amount(new BigDecimal("100000.00"))
                .status("PENDING")
                .build();
        when(bookingDAO.findByCode(any(), eq("BK-1"))).thenReturn(booking);
        when(paymentDAO.findByBookingIdForUpdate(any(), eq(101L))).thenReturn(payment);
        when(paymentDAO.updatePendingVnpayPayment(any(), eq(101L), any(), eq("VN-123"), eq("SUCCESS")))
                .thenReturn(true);
        when(bookingDAO.updatePendingStatus(any(), eq(101L), eq("CONFIRMED"))).thenReturn(true);
        when(ticketDAO.findSeatIdsByBooking(any(), eq(101L))).thenReturn(List.of(10L, 11L));
        when(ticketDAO.activatePendingTickets(any(), eq(101L))).thenReturn(2);
        when(ticketDAO.findReceiptItems(any(), eq(101L))).thenReturn(List.of());

        boolean finalized = bookingEngineService.finalizeVnpayPayment(
                "BK-1", new BigDecimal("100000.00"), "VN-123", true);

        assertTrue(finalized);
        verify(bookingDAO).updatePendingStatus(any(), eq(101L), eq("CONFIRMED"));
        verify(paymentDAO).updatePendingVnpayPayment(any(), eq(101L), any(), eq("VN-123"), eq("SUCCESS"));
        verify(ticketDAO).activatePendingTickets(any(), eq(101L));
        verify(seatHoldingDAO).releaseHoldings(any(), eq(1L), eq(List.of(10L, 11L)));
    }

    @Test
    @DisplayName("Should cancel a failed VNPay booking and restore reserved inventory")
    void testFinalizeFailedVnpayPayment() throws SQLException {
        Booking booking = Booking.builder()
                .bookingCode("BK-2")
                .showtimeId(1L)
                .finalAmount(new BigDecimal("100000.00"))
                .status("PENDING")
                .build();
        booking.setId(102L);
        Payment payment = Payment.builder()
                .bookingId(102L)
                .paymentMethod("VNPAY")
                .amount(new BigDecimal("100000.00"))
                .status("PENDING")
                .build();
        when(bookingDAO.findByCode(any(), eq("BK-2"))).thenReturn(booking);
        when(paymentDAO.findByBookingIdForUpdate(any(), eq(102L))).thenReturn(payment);
        when(paymentDAO.updatePendingVnpayPayment(any(), eq(102L), any(), isNull(), eq("FAILED")))
                .thenReturn(true);
        when(bookingDAO.updatePendingStatus(any(), eq(102L), eq("CANCELLED"))).thenReturn(true);
        when(ticketDAO.findSeatIdsByBooking(any(), eq(102L))).thenReturn(List.of(12L));

        boolean finalized = bookingEngineService.finalizeVnpayPayment(
                "BK-2", new BigDecimal("100000.00"), null, false);

        assertFalse(finalized);
        verify(orderItemDAO).restoreFnbStock(any(), eq(102L));
        verify(ticketDAO).deleteBookingTickets(any(), eq(102L));
        verify(seatHoldingDAO).releaseHoldings(any(), eq(1L), eq(List.of(12L)));
    }

    @Test
    @DisplayName("Should renew payment expiry only for the owning customer with a pending VNPay payment")
    void testRenewPendingVnpayBooking() throws SQLException {
        Booking booking = Booking.builder()
                .bookingCode("BK-3")
                .userId(7L)
                .finalAmount(new BigDecimal("50000.00"))
                .status("PENDING")
                .build();
        booking.setId(103L);
        Payment payment = Payment.builder()
                .bookingId(103L)
                .paymentMethod("VNPAY")
                .amount(new BigDecimal("50000.00"))
                .status("PENDING")
                .build();
        when(bookingDAO.findByCode(any(), eq("BK-3"))).thenReturn(booking);
        when(paymentDAO.findByBookingIdForUpdate(any(), eq(103L))).thenReturn(payment);
        when(bookingDAO.refreshPendingVnpayExpiry(any(), eq(103L), eq(7L))).thenReturn(true);

        BookingResult result = bookingEngineService.renewPendingVnpayBooking("BK-3", 7L);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(new BigDecimal("50000.00"), result.getFinalAmount());
        verify(bookingDAO).refreshPendingVnpayExpiry(any(), eq(103L), eq(7L));
    }

    @Test
    @DisplayName("Should reject renewal of another customer's pending booking")
    void testRenewPendingVnpayBookingRejectsOtherOwner() throws SQLException {
        Booking booking = Booking.builder()
                .bookingCode("BK-4")
                .userId(8L)
                .finalAmount(new BigDecimal("50000.00"))
                .status("PENDING")
                .build();
        booking.setId(104L);
        when(bookingDAO.findByCode(any(), eq("BK-4"))).thenReturn(booking);

        assertNull(bookingEngineService.renewPendingVnpayBooking("BK-4", 7L));
        verifyNoInteractions(paymentDAO);
    }

    @Test
    @DisplayName("Should cancel an owned pending order and release its seats")
    void testCancelPendingVnpayBooking() throws SQLException {
        Booking booking = Booking.builder()
                .bookingCode("BK-5")
                .userId(7L)
                .showtimeId(1L)
                .finalAmount(new BigDecimal("50000.00"))
                .status("PENDING")
                .build();
        booking.setId(105L);
        Payment payment = Payment.builder()
                .bookingId(105L)
                .paymentMethod("VNPAY")
                .amount(new BigDecimal("50000.00"))
                .status("PENDING")
                .build();
        when(bookingDAO.findByCode(any(), eq("BK-5"))).thenReturn(booking);
        when(paymentDAO.findByBookingIdForUpdate(any(), eq(105L))).thenReturn(payment);
        when(paymentDAO.updatePendingVnpayPayment(any(), eq(105L), any(), isNull(), eq("FAILED")))
                .thenReturn(true);
        when(bookingDAO.updatePendingStatus(any(), eq(105L), eq("CANCELLED"))).thenReturn(true);
        when(ticketDAO.findSeatIdsByBooking(any(), eq(105L))).thenReturn(List.of(15L));

        assertTrue(bookingEngineService.cancelPendingVnpayBooking("BK-5", 7L));
        verify(orderItemDAO).restoreFnbStock(any(), eq(105L));
        verify(ticketDAO).deleteBookingTickets(any(), eq(105L));
        verify(seatHoldingDAO).releaseHoldings(any(), eq(1L), eq(List.of(15L)));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ticketPrices is null or empty")
    void testCreateBookingWithoutTicketPricesThrowsException() {
        CreateBookingDTO dto = CreateBookingDTO.builder()
                .userId(5L)
                .showtimeId(1L)
                .seatIds(List.of(10L, 11L))
                .ticketPrices(null)
                .channel("ONLINE")
                .paymentMethod("VNPAY")
                .build();

        assertThrows(IllegalArgumentException.class, () -> bookingEngineService.createBooking(dto));
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
        when(ticketDAO.updateStatus(15L, "USED")).thenReturn(true);

        boolean updated = bookingEngineService.updateTicketStatus(15L, "USED");
        assertTrue(updated);
        verify(ticketDAO, times(1)).updateStatus(15L, "USED");
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
