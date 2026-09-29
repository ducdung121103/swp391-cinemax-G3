package com.cinema.integration;

import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.common.transaction.TransactionManager;
import com.cinema.model.Movie;
import com.cinema.model.Showtime;
import com.cinema.model.Voucher;
import com.cinema.modules.booking.dao.OrderItemDAO;
import com.cinema.modules.booking.dao.VoucherDAO;
import com.cinema.modules.booking.service.VoucherService;
import com.cinema.modules.booking.service.impl.VoucherServiceImpl;
import com.cinema.modules.catalog.dao.MovieDAO;
import com.cinema.modules.catalog.dao.ShowtimeDAO;
import com.cinema.modules.catalog.dao.TicketPricingDAO;
import com.cinema.modules.catalog.service.PricingService;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.PricingServiceImpl;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.identity.service.LoyaltyService;
import com.cinema.modules.identity.service.impl.LoyaltyServiceImpl;
import com.cinema.modules.infrastructure.dao.ScreeningRoomDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Kiểm thử Tích hợp Luồng Nghiệp vụ (Business Logic Integration Test)")
public class BusinessLogicIntegrationTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPs;

    @Mock
    private ResultSet mockRs;

    @BeforeEach
    void setUp() throws SQLException {
        TransactionManager.setTestConnection(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
    }

    @AfterEach
    void tearDown() {
        TransactionManager.clearTestConnection();
    }

    @Test
    @DisplayName("2.3.1 - Tính giá vé suất chiếu tối cuối tuần: Base price (110k) + Surcharge VIP (15k) = 125.000đ (không phải 85.000đ)")
    void testWeekendPrimeVipTicketPriceCalculation() {
        TicketPricingDAO pricingDAO = mock(TicketPricingDAO.class);
        PricingService pricingService = new PricingServiceImpl(pricingDAO);

        // Suất chiếu tối thứ Bảy lúc 19:30 (WEEKEND + PRIME)
        LocalDateTime saturdayPrime = LocalDateTime.of(2026, 10, 3, 19, 30);
        String format = "2D";
        Long vipSeatTypeId = 2L;

        when(pricingDAO.findBasePrice(saturdayPrime, format)).thenReturn(new BigDecimal("110000.00"));
        when(pricingDAO.findSeatTypeSurcharge(vipSeatTypeId)).thenReturn(new BigDecimal("15000.00"));

        BigDecimal calculatedPrice = pricingService.calculateTicketPrice(1L, vipSeatTypeId, saturdayPrime, format);

        assertNotNull(calculatedPrice);
        assertEquals(new BigDecimal("125000.00"), calculatedPrice, "Giá vé VIP tối cuối tuần phải là 110.000 + 15.000 = 125.000đ");
        assertNotEquals(new BigDecimal("85000.00"), calculatedPrice, "Giá vé không được fallback về mức 85.000đ cố định");
    }

    @Test
    @DisplayName("2.3.2 - Áp voucher hợp lệ vs Voucher hết hạn/hết lượt: 1 case áp thành công, các case vi phạm bị từ chối rõ ràng")
    void testVoucherValidationAndRejection() {
        VoucherDAO voucherDAO = mock(VoucherDAO.class);
        VoucherService voucherService = new VoucherServiceImpl(voucherDAO);

        // Case A: Voucher hợp lệ
        Voucher validVoucher = Voucher.builder()
                .code("DISCOUNT20")
                .discountType("PERCENT")
                .discountVal(new BigDecimal("20.00"))
                .minOrderAmount(new BigDecimal("100000.00"))
                .maxDiscount(new BigDecimal("50000.00"))
                .usageLimit(100)
                .usedCount(10)
                .validFrom(LocalDateTime.now().minusDays(2))
                .validTo(LocalDateTime.now().plusDays(2))
                .build();
        validVoucher.setId(1L);
        validVoucher.setIsActive(true);
        when(voucherDAO.findById(any(), eq(1L))).thenReturn(validVoucher);

        BigDecimal discount = voucherService.calculateAndValidateDiscount(mockConnection, 1L, new BigDecimal("200000.00"));
        assertEquals(new BigDecimal("40000.00"), discount, "200.000 x 20% = 40.000đ");

        // Case B: Voucher hết hạn
        Voucher expiredVoucher = Voucher.builder()
                .code("EXPIRED")
                .validFrom(LocalDateTime.now().minusDays(10))
                .validTo(LocalDateTime.now().minusDays(1))
                .build();
        expiredVoucher.setId(2L);
        expiredVoucher.setIsActive(true);
        when(voucherDAO.findById(any(), eq(2L))).thenReturn(expiredVoucher);

        IllegalStateException exExpired = assertThrows(IllegalStateException.class, () ->
                voucherService.calculateAndValidateDiscount(mockConnection, 2L, new BigDecimal("200000.00"))
        );
        assertTrue(exExpired.getMessage().contains("hết hạn sử dụng"));

        // Case C: Voucher hết lượt sử dụng
        Voucher exhaustedVoucher = Voucher.builder()
                .code("FULL")
                .usageLimit(5)
                .usedCount(5)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .build();
        exhaustedVoucher.setId(3L);
        exhaustedVoucher.setIsActive(true);
        when(voucherDAO.findById(any(), eq(3L))).thenReturn(exhaustedVoucher);

        IllegalStateException exFull = assertThrows(IllegalStateException.class, () ->
                voucherService.calculateAndValidateDiscount(mockConnection, 3L, new BigDecimal("200000.00"))
        );
        assertTrue(exFull.getMessage().contains("hết số lượt sử dụng"));
    }

    @Test
    @DisplayName("2.3.3 - Đặt F&B vượt tồn kho: Hệ thống từ chối và ném ngoại lệ rõ ràng, không cho đặt hàng")
    void testFnbOutOfStockRejection() throws SQLException {
        OrderItemDAO orderItemDAO = new OrderItemDAO();

        // Giả lập rạp ID 1, mặt hàng Bắp ngọt ID 10
        when(mockPs.executeQuery()).thenAnswer(inv -> {
            ResultSet rs = mock(ResultSet.class);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("name")).thenReturn("Bắp rang bơ");
            when(rs.getBigDecimal("price")).thenReturn(new BigDecimal("45000.00"));
            when(rs.getInt("stock_quantity")).thenReturn(3); // Tồn kho chỉ còn 3
            when(rs.getInt("warning_threshold")).thenReturn(5);
            return rs;
        });

        Map<Long, Integer> requestedFnb = new HashMap<>();
        requestedFnb.put(10L, 8); // Đặt 8 phần > 3 phần tồn kho

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                orderItemDAO.insertFnbOrderItems(mockConnection, 999L, requestedFnb)
        );
        assertTrue(ex.getMessage().contains("không đủ số lượng tồn kho"));
    }

    @Test
    @DisplayName("2.3.4 - Tạo suất chiếu đè giờ: isAvailable() từ chối nếu trùng lịch chiếu hoặc lấn vào đệm 15p dọn phòng")
    void testShowtimeOverlapDetectionWithCleaningBuffer() {
        ShowtimeDAO showtimeDAO = mock(ShowtimeDAO.class);
        MovieDAO movieDAO = mock(MovieDAO.class);
        SystemSettingDAO systemSettingDAO = mock(SystemSettingDAO.class);
        ScreeningRoomService screeningRoomService = mock(ScreeningRoomService.class);

        ShowtimeService showtimeService = new ShowtimeServiceImpl(showtimeDAO, movieDAO, systemSettingDAO, screeningRoomService);

        Movie movie = Movie.builder().durationMinutes(120).build();
        movie.setId(100L);
        when(movieDAO.findById(100L)).thenReturn(movie);
        when(systemSettingDAO.getIntSetting("CLEANING_BUFFER_MINUTES", 15)).thenReturn(15);

        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 14, 0);
        Showtime showtime = Showtime.builder()
                .movieId(100L)
                .screeningRoomId(1L)
                .startTime(start)
                .build();

        // 120p phim + 15p dọn phòng = 135p -> kết thúc lúc 16:15
        LocalDateTime expectedEnd = start.plusMinutes(135);

        // Giả lập phòng chiếu bận trong khung giờ này
        when(screeningRoomService.isRoomAvailable(eq(1L), eq(start), eq(expectedEnd))).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                showtimeService.createShowtime(showtime)
        );
        assertEquals(expectedEnd, showtime.getEndTime(), "End time phải được tính tự động gồm thời lượng phim + 15p dọn phòng");
        assertTrue(ex.getMessage().contains("Phòng chiếu đang bận hoặc đang trong lịch bảo trì"));
    }

    @Test
    @DisplayName("2.3.5 - LoyaltyService addPoints & deductPoints: Khớp chuẩn cột points_change, balance_after, transaction_type EARN/REDEEM")
    void testLoyaltyPointsAndBalanceAfterIntegrity() throws SQLException {
        LoyaltyService loyaltyService = new LoyaltyServiceImpl();

        // Mock đọc số dư hiện tại của user = 100
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("loyalty_points")).thenReturn(100);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        // 1. Kiểm tra addPoints
        boolean addSuccess = loyaltyService.addPoints(10L, 500L, 50, "Tích điểm xem phim");
        assertTrue(addSuccess);
        verify(mockConnection, atLeastOnce()).prepareStatement(sqlCaptor.capture());

        // Kiểm tra câu lệnh INSERT point_histories có chứa đúng tên cột
        boolean foundInsertHistory = sqlCaptor.getAllValues().stream().anyMatch(sql ->
                sql.contains("INSERT INTO point_histories") &&
                sql.contains("points_change") &&
                sql.contains("balance_after") &&
                sql.contains("transaction_type") &&
                sql.contains("reason")
        );
        assertTrue(foundInsertHistory, "Câu lệnh INSERT phải dùng đúng các cột points_change, balance_after, transaction_type, reason");

        // 2. Kiểm tra deductPoints
        reset(mockConnection, mockPs, mockRs);
        TransactionManager.setTestConnection(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("loyalty_points")).thenReturn(100);

        boolean deductSuccess = loyaltyService.deductPoints(10L, 500L, 30, "Đổi bắp nước");
        assertTrue(deductSuccess);
    }
}
