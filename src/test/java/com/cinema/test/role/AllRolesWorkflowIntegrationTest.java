package com.cinema.test.role;

import com.cinema.common.constant.RoleConstant;
import com.cinema.common.filter.AuthorizationFilter;
import com.cinema.common.util.ValidationUtil;
import com.cinema.model.Role;
import com.cinema.model.Seat;
import com.cinema.model.SeatType;
import com.cinema.model.User;
import com.cinema.modules.booking.dao.BookingDAO;
import com.cinema.modules.booking.dao.OrderItemDAO;
import com.cinema.modules.booking.dao.PaymentDAO;
import com.cinema.modules.booking.dao.SeatHoldingDAO;
import com.cinema.modules.booking.dao.TicketDAO;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.VoucherService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import com.cinema.modules.infrastructure.dao.ScreeningRoomDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử tích hợp toàn diện luồng nghiệp vụ và phân quyền (RBAC) trên tất cả 5 Roles
 * (ADMIN, MANAGER, STAFF, CUSTOMER, GUEST), bảo đảm các tính năng cốt lõi của DungBD vận hành nhất quán.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("End-to-End Role & Workflow Integration Tests (DungBD System Audit)")
public class AllRolesWorkflowIntegrationTest {

    private AuthorizationFilter authorizationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain chain;

    @Mock
    private ScreeningRoomDAO screeningRoomDAO;

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

    @Mock
    private VoucherService voucherService;

    @BeforeEach
    void setUp() {
        authorizationFilter = new AuthorizationFilter();
    }

    private User createMockUser(String roleName, Long cinemaId) {
        Role role = new Role();
        role.setRoleName(roleName);

        User user = new User();
        user.setId(100L);
        user.setEmail(roleName.toLowerCase() + "@test.com");
        user.setRole(role);
        user.setCinemaId(cinemaId);
        return user;
    }

    private Seat createSeat(Long id, String row, int number, Long typeId, String typeCode) {
        SeatType st = new SeatType();
        st.setId(typeId);
        st.setTypeCode(typeCode);
        st.setName("COUPLE".equalsIgnoreCase(typeCode) ? "Ghế Đôi Sweetbox" : "Ghế Tiêu Chuẩn");

        Seat s = new Seat();
        s.setId(id);
        s.setSeatRow(row);
        s.setSeatNumber(number);
        s.setSeatCode(row + String.format("%02d", number));
        s.setSeatTypeId(typeId);
        s.setSeatType(st);
        s.setStatus("ACTIVE");
        s.setIsActive(true);
        return s;
    }

    // =========================================================================
    // 1. PHÂN QUYỀN GUEST / ANONYMOUS
    // =========================================================================
    @Test
    @DisplayName("GUEST: Truy cập khu vực Admin -> Chuyển hướng 302 về trang /login")
    void testGuestAccessAdminArea_ShouldRedirectToLogin() throws IOException, ServletException {
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        authorizationFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/login");
        verify(chain, never()).doFilter(request, response);
    }

    // =========================================================================
    // 2. PHÂN QUYỀN CUSTOMER
    // =========================================================================
    @Test
    @DisplayName("CUSTOMER: Cố tình truy cập bất kỳ trang Admin -> Chặn 403 Forbidden")
    void testCustomerAccessAdminArea_ShouldReturn403() throws IOException, ServletException {
        User customer = createMockUser(RoleConstant.CUSTOMER, null);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(customer);
        when(request.getRequestURI()).thenReturn("/admin/dashboard");

        authorizationFilter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(chain, never()).doFilter(request, response);
    }

    // =========================================================================
    // 3. PHÂN QUYỀN STAFF
    // =========================================================================
    @Test
    @DisplayName("STAFF: Được phép vào Quầy POS và Scanner soát vé")
    void testStaffAccessPosAndScanner_ShouldPass() throws IOException, ServletException {
        User staff = createMockUser(RoleConstant.STAFF, 1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(staff);

        // Test 1: Quầy POS
        when(request.getRequestURI()).thenReturn("/operation/pos");
        authorizationFilter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);

        // Test 2: Máy quét QR soát vé
        when(request.getRequestURI()).thenReturn("/admin/operation/scanner");
        authorizationFilter.doFilter(request, response, chain);
        verify(chain, atLeastOnce()).doFilter(request, response);
    }

    @Test
    @DisplayName("STAFF: Cố tình truy cập Hạ tầng hoặc Báo cáo -> Chặn 403 Forbidden")
    void testStaffAccessAdminInfrastructureAndReports_ShouldReturn403() throws IOException, ServletException {
        User staff = createMockUser(RoleConstant.STAFF, 1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(staff);
        when(request.getRequestURI()).thenReturn("/admin/infrastructure/cinemas");

        authorizationFilter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(chain, never()).doFilter(request, response);
    }

    // =========================================================================
    // 4. PHÂN QUYỀN MANAGER
    // =========================================================================
    @Test
    @DisplayName("MANAGER: Được phép vào Phòng chiếu, Thiết kế sơ đồ ghế, Báo cáo Lấp đầy của chi nhánh mình")
    void testManagerAccessBranchInfrastructureAndOccupancy_ShouldPass() throws IOException, ServletException {
        User manager = createMockUser(RoleConstant.MANAGER, 1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(manager);

        // 1. Quản lý phòng chiếu
        when(request.getRequestURI()).thenReturn("/admin/infrastructure/rooms");
        authorizationFilter.doFilter(request, response, chain);

        // 2. Thiết kế sơ đồ ghế
        when(request.getRequestURI()).thenReturn("/admin/infrastructure/seats");
        authorizationFilter.doFilter(request, response, chain);

        // 3. Báo cáo tỷ lệ lấp đầy
        when(request.getRequestURI()).thenReturn("/admin/reports/occupancy");
        authorizationFilter.doFilter(request, response, chain);

        verify(chain, atLeastOnce()).doFilter(request, response);
    }

    @Test
    @DisplayName("MANAGER: Cố tình truy cập Báo cáo Doanh thu (M-10.1) -> Chặn 403 Forbidden nghiêm ngặt")
    void testManagerAccessRevenueReport_ShouldReturn403() throws IOException, ServletException {
        User manager = createMockUser(RoleConstant.MANAGER, 1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(manager);
        when(request.getRequestURI()).thenReturn("/admin/reports/revenue");

        authorizationFilter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq("Báo cáo Doanh thu chỉ dành riêng cho Quản trị viên (Admin)!"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("MANAGER: Cố tình truy cập Quản lý Phim hoặc Kiểm duyệt Review -> Chặn 403 Forbidden")
    void testManagerAccessMovieCatalogAndReviews_ShouldReturn403() throws IOException, ServletException {
        User manager = createMockUser(RoleConstant.MANAGER, 1L);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(manager);

        when(request.getRequestURI()).thenReturn("/admin/catalog/movies");
        authorizationFilter.doFilter(request, response, chain);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq("Chỉ Admin hệ thống mới được quyền quản lý danh mục Phim!"));

        when(request.getRequestURI()).thenReturn("/admin/reviews");
        authorizationFilter.doFilter(request, response, chain);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq("Chỉ Admin hệ thống mới được quyền kiểm duyệt đánh giá phim!"));
    }

    // =========================================================================
    // 5. PHÂN QUYỀN ADMIN
    // =========================================================================
    @Test
    @DisplayName("ADMIN: Toàn quyền truy cập Báo cáo Doanh thu, Cụm Rạp, Cấu hình Hệ thống")
    void testAdminAccessAllInfrastructureAndReports_ShouldPass() throws IOException, ServletException {
        User admin = createMockUser(RoleConstant.ADMIN, null);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(admin);

        // 1. Báo cáo doanh thu
        when(request.getRequestURI()).thenReturn("/admin/reports/revenue");
        authorizationFilter.doFilter(request, response, chain);

        // 2. Cấu hình hệ thống
        when(request.getRequestURI()).thenReturn("/admin/infrastructure/settings");
        authorizationFilter.doFilter(request, response, chain);

        // 3. Quản lý cụm rạp
        when(request.getRequestURI()).thenReturn("/admin/infrastructure/cinemas");
        authorizationFilter.doFilter(request, response, chain);

        verify(chain, atLeastOnce()).doFilter(request, response);
    }

    @Test
    @DisplayName("ADMIN: Cố tình vào Quầy POS trực tiếp -> Chặn 403 Forbidden")
    void testAdminAccessPosCounter_ShouldReturn403() throws IOException, ServletException {
        User admin = createMockUser(RoleConstant.ADMIN, null);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(admin);
        when(request.getRequestURI()).thenReturn("/operation/pos");

        authorizationFilter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq("Admin không có quyền sử dụng quầy POS!"));
        verify(chain, never()).doFilter(request, response);
    }

    // =========================================================================
    // 6. TÍNH NĂNG DUNGBD: VALIDATION GHẾ ĐÔI SWEETBOX (ADMIN DESIGNER & SERVICE)
    // =========================================================================
    @Test
    @DisplayName("DungBD M-04.3: Chọn 3 ghế Sweetbox đổi loại -> Báo lỗi số lượng chẵn")
    void testCoupleSeatValidation_OddQuantity_ShouldThrow() {
        Seat s1 = createSeat(1L, "C", 7, 3L, "COUPLE");
        Seat s2 = createSeat(2L, "C", 8, 3L, "COUPLE");
        Seat s3 = createSeat(3L, "C", 9, 3L, "COUPLE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ValidationUtil.validateCoupleSeats(Arrays.asList(s1, s2, s3))
        );
        assertTrue(ex.getMessage().contains("chẵn") || ex.getMessage().contains("lẻ"),
                "Thông báo phải giải thích rõ quy tắc số lượng chẵn");
    }

    @Test
    @DisplayName("DungBD M-04.3: Chọn 2 ghế Sweetbox không liền kề (C07 và C09) -> Báo lỗi liền kề")
    void testCoupleSeatValidation_NonAdjacent_ShouldThrow() {
        Seat s1 = createSeat(1L, "C", 7, 3L, "COUPLE");
        Seat s2 = createSeat(2L, "C", 9, 3L, "COUPLE"); // Cách 1 ô

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ValidationUtil.validateCoupleSeats(Arrays.asList(s1, s2))
        );
        assertTrue(ex.getMessage().contains("liền kề"), "Thông báo phải giải thích quy tắc liền kề");
    }

    @Test
    @DisplayName("DungBD M-04.3: Chọn cặp ghế Sweetbox hợp lệ (C07 và C08) -> Hợp lệ thành công")
    void testCoupleSeatValidation_ValidPair_ShouldPass() {
        Seat s1 = createSeat(1L, "C", 7, 3L, "COUPLE");
        Seat s2 = createSeat(2L, "C", 8, 3L, "COUPLE");

        assertDoesNotThrow(() -> ValidationUtil.validateCoupleSeats(Arrays.asList(s1, s2)));
    }

    @Test
    @DisplayName("DungBD M-04.3: ScreeningRoomService kiểm tra toàn phòng khi đổi loại ghế ngăn chặn xé lẻ cặp")
    void testScreeningRoomService_UpdateBatchSeats_CoupleValidation() {
        ScreeningRoomService roomService = new ScreeningRoomServiceImpl(null, screeningRoomDAO);

        // Trường hợp người dùng chọn 3 ghế để đổi sang Sweetbox
        List<Long> seatIds = Arrays.asList(1L, 2L, 3L);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                roomService.updateBatchSeats(1L, seatIds, 3L, "ACTIVE", true)
        );
        assertTrue(ex.getMessage().contains("chẵn") || ex.getMessage().contains("Sweetbox"));
        verify(screeningRoomDAO, never()).updateBatchSeats(any(), any(), any(), any(), any());
    }

    // =========================================================================
    // 7. TÍNH NĂNG DUNGBD: ÁP DỤNG GHẾ ĐÔI KHI KHÁCH ĐẶT VÉ (BOOKING ENGINE)
    // =========================================================================
    @Test
    @DisplayName("DungBD M-04.3 & TV4: Khách hàng cố tình giữ 1 ghế lẻ Sweetbox -> Bị từ chối giữ ghế")
    void testBookingEngine_HoldSeats_SingleCoupleSeat_ShouldReject() {
        BookingEngineService bookingEngine = new BookingEngineServiceImpl(
                bookingDAO, ticketDAO, orderItemDAO, paymentDAO, seatHoldingDAO, voucherService, screeningRoomDAO
        );

        Seat singleCoupleSeat = createSeat(10L, "J", 1, 3L, "COUPLE");
        when(screeningRoomDAO.findSeatsByIds(Collections.singletonList(10L)))
                .thenReturn(Collections.singletonList(singleCoupleSeat));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingEngine.holdSeats(1L, Collections.singletonList(10L), "session-test")
        );
        assertTrue(ex.getMessage().contains("chẵn") || ex.getMessage().contains("Sweetbox"));
    }

    @Test
    @DisplayName("DungBD M-04.3 & TV4: Khách hàng cố tình tạo booking với 1 ghế lẻ Sweetbox -> Bị từ chối tạo booking")
    void testBookingEngine_CreateBooking_SingleCoupleSeat_ShouldReject() {
        BookingEngineService bookingEngine = new BookingEngineServiceImpl(
                bookingDAO, ticketDAO, orderItemDAO, paymentDAO, seatHoldingDAO, voucherService, screeningRoomDAO
        );

        Seat singleCoupleSeat = createSeat(10L, "J", 1, 3L, "COUPLE");
        when(screeningRoomDAO.findSeatsByIds(Collections.singletonList(10L)))
                .thenReturn(Collections.singletonList(singleCoupleSeat));

        CreateBookingDTO dto = CreateBookingDTO.builder()
                .showtimeId(1L)
                .seatIds(Collections.singletonList(10L))
                .ticketPrices(Collections.singletonList(new BigDecimal("120000")))
                .sessionId("session-test")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingEngine.createBooking(dto)
        );
        assertTrue(ex.getMessage().contains("chẵn") || ex.getMessage().contains("Sweetbox"));
    }
}
