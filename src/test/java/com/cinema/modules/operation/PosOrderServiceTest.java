package com.cinema.modules.operation;

import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.operation.service.PosOrderService;
import com.cinema.modules.operation.service.impl.PosOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for PosOrderService (TV 5 POS Counter Checkout)")
class PosOrderServiceTest {

    @Mock
    private BookingEngineService bookingEngineService;

    private PosOrderService posOrderService;

    @BeforeEach
    void setUp() {
        posOrderService = new PosOrderServiceImpl(bookingEngineService);
    }

    @Test
    @DisplayName("Should assign POS channel and delegate checkout to TV 4 Core Booking Engine")
    void testPosCheckoutDelegation() {
        CreateBookingDTO request = CreateBookingDTO.builder()
                .staffId(3L)
                .showtimeId(1L)
                .seatIds(List.of(15L, 16L))
                .paymentMethod("CASH")
                .channel("ONLINE") // Ban đầu là Online
                .build();

        BookingResult mockResult = BookingResult.builder()
                .bookingId(888L)
                .bookingCode("BK-2024-888")
                .finalAmount(new BigDecimal("170000.00"))
                .status("SUCCESS")
                .message("Đặt vé và thanh toán thành công!")
                .build();

        when(bookingEngineService.createBooking(any(CreateBookingDTO.class))).thenReturn(mockResult);

        BookingResult result = posOrderService.processCounterCheckout(request);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(888L, result.getBookingId());

        // Kiểm tra request truyền sang TV 4 đã được chuyển đổi channel thành POS
        ArgumentCaptor<CreateBookingDTO> captor = ArgumentCaptor.forClass(CreateBookingDTO.class);
        verify(bookingEngineService, times(1)).createBooking(captor.capture());
        assertEquals("POS", captor.getValue().getChannel());
        assertEquals(3L, captor.getValue().getStaffId());
    }
}
