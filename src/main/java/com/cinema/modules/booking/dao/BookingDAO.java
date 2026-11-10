package com.cinema.modules.booking.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Booking;
import com.cinema.modules.booking.dto.CustomerBookingHistoryDTO;
import com.cinema.modules.booking.dto.TicketReceipt;
import com.cinema.modules.booking.dto.ShowtimeReminderDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DAO quản lý bảng bookings (TV 4).
 */
public class BookingDAO {

    private final TicketDAO ticketDAO = new TicketDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    public Long insertBooking(Connection conn, Booking b) throws SQLException {
        if (b.getBookingCode() == null || b.getBookingCode().isBlank()) {
            String code = "BK-" + System.currentTimeMillis() + "-"
                    + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            b.setBookingCode(code);
        }

        String sql = "INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, " +
                "total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getBookingCode());
            if (b.getUserId() != null)
                ps.setLong(2, b.getUserId());
            else
                ps.setNull(2, java.sql.Types.BIGINT);
            if (b.getStaffId() != null)
                ps.setLong(3, b.getStaffId());
            else
                ps.setNull(3, java.sql.Types.BIGINT);
            ps.setLong(4, b.getShowtimeId());
            if (b.getVoucherId() != null)
                ps.setLong(5, b.getVoucherId());
            else
                ps.setNull(5, java.sql.Types.BIGINT);
            ps.setString(6, b.getChannel() != null ? b.getChannel() : "ONLINE");
            ps.setBigDecimal(7, b.getTotalTicketsAmount());
            ps.setBigDecimal(8, b.getTotalFnbAmount());
            ps.setBigDecimal(9, b.getDiscountAmount());
            ps.setBigDecimal(10, b.getFinalAmount());
            ps.setString(11, b.getStatus() != null ? b.getStatus() : "CONFIRMED");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public Booking findByCode(Connection conn, String bookingCode) throws SQLException {
        String sql = "SELECT * FROM bookings WITH (UPDLOCK, HOLDLOCK) WHERE booking_code = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Booking booking = Booking.builder()
                            .bookingCode(rs.getString("booking_code"))
                            .userId(rs.getObject("user_id") != null ? rs.getLong("user_id") : null)
                            .staffId(rs.getObject("staff_id") != null ? rs.getLong("staff_id") : null)
                            .showtimeId(rs.getLong("showtime_id"))
                            .voucherId(rs.getObject("voucher_id") != null ? rs.getLong("voucher_id") : null)
                            .channel(rs.getString("channel"))
                            .totalTicketsAmount(rs.getBigDecimal("total_tickets_amount"))
                            .totalFnbAmount(rs.getBigDecimal("total_fnb_amount"))
                            .discountAmount(rs.getBigDecimal("discount_amount"))
                            .finalAmount(rs.getBigDecimal("final_amount"))
                            .status(rs.getString("status"))
                            .build();
                    booking.setId(rs.getLong("id"));
                    return booking;
                }
            }
        }
        return null;
    }

    public boolean updatePendingStatus(Connection conn, Long bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, updated_at = GETDATE() "
                + "WHERE id = ? AND status = 'PENDING' AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, bookingId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean refreshPendingVnpayExpiry(Connection conn, Long bookingId, Long userId) throws SQLException {
        String sql = "UPDATE bookings SET updated_at = GETDATE() "
                + "WHERE id = ? AND user_id = ? AND status = 'PENDING' AND is_deleted = 0 "
                + "AND EXISTS (SELECT 1 FROM payments WHERE booking_id = bookings.id "
                + "AND payment_method = 'VNPAY' AND status = 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setLong(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public List<String> findExpiredVnpayBookingCodes(Connection conn) throws SQLException {
        String sql = "SELECT b.booking_code FROM bookings b WITH (UPDLOCK, READPAST) "
                + "JOIN payments p WITH (UPDLOCK, READPAST) ON p.booking_id = b.id "
                + "WHERE b.status = 'PENDING' AND b.is_deleted = 0 "
                + "AND p.payment_method = 'VNPAY' AND p.status = 'PENDING' "
                + "AND b.updated_at <= DATEADD(MINUTE, -15, GETDATE())";
        List<String> codes = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) codes.add(rs.getString("booking_code"));
        }
        return codes;
    }

    public boolean updateStatusByCode(Connection conn, String bookingCode, String status) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, updated_at = GETDATE() WHERE booking_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, bookingCode);
            return ps.executeUpdate() > 0;
        }
    }

    public List<ShowtimeReminderDTO> findBookingsNeedingReminder(int withinMinutes) {
        String sql = "SELECT b.id, b.user_id, m.title AS movie_title, st.start_time, " +
                "sr.name AS room_name, c.name AS cinema_name " +
                "FROM bookings b " +
                "JOIN showtimes st ON b.showtime_id = st.id " +
                "JOIN movies m ON st.movie_id = m.id " +
                "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                "LEFT JOIN cinemas c ON sr.cinema_id = c.id " +
                "WHERE b.user_id IS NOT NULL AND b.is_deleted = 0 AND b.status = 'CONFIRMED' " +
                "AND st.is_deleted = 0 AND st.status = 'SCHEDULED' " +
                "AND st.start_time > SYSDATETIME() " +
                "AND st.start_time <= DATEADD(MINUTE, ?, SYSDATETIME())";
        List<ShowtimeReminderDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, withinMinutes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp start = rs.getTimestamp("start_time");
                    list.add(new ShowtimeReminderDTO(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("movie_title"),
                            start == null ? null : start.toLocalDateTime(),
                            rs.getString("room_name"),
                            rs.getString("cinema_name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<CustomerBookingHistoryDTO> findHistoryByUserId(Long userId) {
        if (userId == null)
            return List.of();
        String sql = "SELECT b.id AS booking_id, b.booking_code, b.created_at, b.status, b.channel, " +
                "b.total_tickets_amount, b.total_fnb_amount, b.discount_amount, b.final_amount, " +
                "p.payment_method, u.id AS customer_id, u.full_name AS customer_name, " +
                "u.phone AS customer_phone, u.email AS customer_email, " +
                "m.id AS movie_id, m.title AS movie_title, m.poster_url, " +
                "m.duration_minutes, m.age_rating, st.start_time, st.end_time, st.experience_format, " +
                "sr.name AS room_name, c.name AS cinema_name, c.address AS cinema_address " +
                "FROM bookings b " +
                "JOIN showtimes st ON b.showtime_id = st.id " +
                "JOIN movies m ON st.movie_id = m.id " +
                "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                "LEFT JOIN cinemas c ON sr.cinema_id = c.id " +
                "LEFT JOIN users u ON b.user_id = u.id " +
                "LEFT JOIN ( " +
                "    SELECT booking_id, MAX(payment_method) AS payment_method " +
                "    FROM payments GROUP BY booking_id " +
                ") p ON p.booking_id = b.id " +
                "WHERE b.user_id = ? AND b.is_deleted = 0 " +
                "ORDER BY b.created_at DESC";
        List<CustomerBookingHistoryDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToCustomerBookingHistoryDTO(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public CustomerBookingHistoryDTO findHistoryDetailByCode(String bookingCode, Long userId) {
        if (bookingCode == null || bookingCode.isBlank())
            return null;
        String sql = "SELECT b.id AS booking_id, b.booking_code, b.created_at, b.status, b.channel, " +
                "b.total_tickets_amount, b.total_fnb_amount, b.discount_amount, b.final_amount, " +
                "p.payment_method, u.id AS customer_id, u.full_name AS customer_name, " +
                "u.phone AS customer_phone, u.email AS customer_email, " +
                "m.id AS movie_id, m.title AS movie_title, m.poster_url, " +
                "m.duration_minutes, m.age_rating, st.start_time, st.end_time, st.experience_format, " +
                "sr.name AS room_name, c.name AS cinema_name, c.address AS cinema_address " +
                "FROM bookings b " +
                "JOIN showtimes st ON b.showtime_id = st.id " +
                "JOIN movies m ON st.movie_id = m.id " +
                "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                "LEFT JOIN cinemas c ON sr.cinema_id = c.id " +
                "LEFT JOIN users u ON b.user_id = u.id " +
                "LEFT JOIN ( " +
                "    SELECT booking_id, MAX(payment_method) AS payment_method " +
                "    FROM payments GROUP BY booking_id " +
                ") p ON p.booking_id = b.id " +
                "WHERE b.booking_code = ? AND (b.user_id = ? OR ? IS NULL) AND b.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingCode.trim());
            if (userId != null)
                ps.setLong(2, userId);
            else
                ps.setNull(2, java.sql.Types.BIGINT);
            if (userId != null)
                ps.setLong(3, userId);
            else
                ps.setNull(3, java.sql.Types.BIGINT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomerBookingHistoryDTO(rs, conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CustomerBookingHistoryDTO> findRecentConfirmedBookings(Long cinemaId, int limit) {
        List<CustomerBookingHistoryDTO> list = new ArrayList<>();
        String sql = "SELECT TOP (?) b.id AS booking_id, b.booking_code, b.created_at, b.status, b.channel, " +
                "b.total_tickets_amount, b.total_fnb_amount, b.discount_amount, b.final_amount, " +
                "p.payment_method, u.id AS customer_id, u.full_name AS customer_name, " +
                "u.phone AS customer_phone, u.email AS customer_email, " +
                "m.id AS movie_id, m.title AS movie_title, m.poster_url, " +
                "m.duration_minutes, m.age_rating, st.start_time, st.end_time, st.experience_format, " +
                "sr.name AS room_name, c.name AS cinema_name, c.address AS cinema_address " +
                "FROM bookings b " +
                "JOIN showtimes st ON b.showtime_id = st.id " +
                "JOIN movies m ON st.movie_id = m.id " +
                "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                "JOIN cinemas c ON sr.cinema_id = c.id " +
                "LEFT JOIN users u ON b.user_id = u.id " +
                "LEFT JOIN ( " +
                "    SELECT booking_id, MAX(payment_method) AS payment_method " +
                "    FROM payments GROUP BY booking_id " +
                ") p ON p.booking_id = b.id " +
                "WHERE (c.id = ? OR ? IS NULL) AND b.status = 'CONFIRMED' AND b.is_deleted = 0 " +
                "ORDER BY b.id DESC";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 10);
            if (cinemaId != null)
                ps.setLong(2, cinemaId);
            else
                ps.setNull(2, java.sql.Types.BIGINT);
            if (cinemaId != null)
                ps.setLong(3, cinemaId);
            else
                ps.setNull(3, java.sql.Types.BIGINT);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToCustomerBookingHistoryDTO(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm kiếm đơn hàng phục vụ in vé tại quầy bằng từ khóa (mã đơn, tên khách hàng, SĐT, email).
     * Cho phép gõ ký tự bất kỳ, không yêu cầu gõ đúng 100% toàn bộ mã đơn.
     */
    public List<CustomerBookingHistoryDTO> searchBookingsForPrinting(String keyword, Long cinemaId, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return findRecentConfirmedBookings(cinemaId, limit);
        }
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT TOP (?) b.id AS booking_id, b.booking_code, b.created_at, b.status, b.channel, " +
                "b.total_tickets_amount, b.total_fnb_amount, b.discount_amount, b.final_amount, " +
                "p.payment_method, u.id AS customer_id, u.full_name AS customer_name, " +
                "u.phone AS customer_phone, u.email AS customer_email, " +
                "m.id AS movie_id, m.title AS movie_title, m.poster_url, " +
                "m.duration_minutes, m.age_rating, st.start_time, st.end_time, st.experience_format, " +
                "sr.name AS room_name, c.name AS cinema_name, c.address AS cinema_address " +
                "FROM bookings b " +
                "JOIN showtimes st ON b.showtime_id = st.id " +
                "JOIN movies m ON st.movie_id = m.id " +
                "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                "LEFT JOIN cinemas c ON sr.cinema_id = c.id " +
                "LEFT JOIN users u ON b.user_id = u.id " +
                "LEFT JOIN ( " +
                "    SELECT booking_id, MAX(payment_method) AS payment_method " +
                "    FROM payments GROUP BY booking_id " +
                ") p ON p.booking_id = b.id " +
                "WHERE b.status = 'CONFIRMED' AND b.is_deleted = 0 " +
                "  AND (b.booking_code LIKE ? OR u.full_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ?) " +
                "ORDER BY (CASE WHEN ? IS NOT NULL AND c.id = ? THEN 0 ELSE 1 END), b.id DESC";
        List<CustomerBookingHistoryDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 20);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            if (cinemaId != null) {
                ps.setLong(6, cinemaId);
                ps.setLong(7, cinemaId);
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
                ps.setNull(7, java.sql.Types.BIGINT);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToCustomerBookingHistoryDTO(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private CustomerBookingHistoryDTO mapRowToCustomerBookingHistoryDTO(ResultSet rs, Connection conn) throws SQLException {
        Long bookingId = rs.getLong("booking_id");
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Timestamp startTs = rs.getTimestamp("start_time");
        Timestamp endTs = rs.getTimestamp("end_time");

        LocalDateTime createdAtLdt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;
        LocalDateTime startLdt = startTs != null ? startTs.toLocalDateTime() : null;
        LocalDateTime endLdt = endTs != null ? endTs.toLocalDateTime() : null;

        Long customerId = rs.getObject("customer_id") != null ? rs.getLong("customer_id") : null;
        String customerName = rs.getString("customer_name");
        String customerPhone = rs.getString("customer_phone");
        String customerEmail = rs.getString("customer_email");

        String displayCustomerName = (customerName != null && !customerName.isBlank()) ? customerName : "Khách lẻ tại quầy";
        String formattedShowtime = "";
        if (startLdt != null) {
            formattedShowtime = startLdt.format(DateTimeFormatter.ofPattern("HH:mm")) + " · " + startLdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        String formattedCreatedAt = createdAtLdt != null ? createdAtLdt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")) : "";

        List<TicketReceipt> tickets = ticketDAO.findReceiptItems(conn, bookingId);
        String seatCodes = (tickets != null && !tickets.isEmpty()) ?
                tickets.stream().map(TicketReceipt::getSeatCode).filter(c -> c != null && !c.isBlank()).collect(Collectors.joining(", "))
                : "Chưa có ghế";

        return CustomerBookingHistoryDTO.builder()
                .bookingId(bookingId)
                .bookingCode(rs.getString("booking_code"))
                .createdAt(createdAtLdt)
                .status(rs.getString("status"))
                .channel(rs.getString("channel"))
                .totalTicketsAmount(rs.getBigDecimal("total_tickets_amount"))
                .totalFnbAmount(rs.getBigDecimal("total_fnb_amount"))
                .discountAmount(rs.getBigDecimal("discount_amount"))
                .finalAmount(rs.getBigDecimal("final_amount"))
                .paymentMethod(rs.getString("payment_method"))
                .customerId(customerId)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .customerEmail(customerEmail)
                .displayCustomerName(displayCustomerName)
                .formattedShowtime(formattedShowtime)
                .formattedCreatedAt(formattedCreatedAt)
                .seatCodesString(seatCodes)
                .ticketCount(tickets != null ? tickets.size() : 0)
                .upcoming(startLdt != null && startLdt.isAfter(LocalDateTime.now()))
                .movieId(rs.getLong("movie_id"))
                .movieTitle(rs.getString("movie_title"))
                .posterUrl(rs.getString("poster_url"))
                .durationMinutes((Integer) rs.getObject("duration_minutes"))
                .ageRating(rs.getString("age_rating"))
                .showtimeStartTime(startLdt)
                .showtimeEndTime(endLdt)
                .experienceFormat(rs.getString("experience_format"))
                .roomName(rs.getString("room_name"))
                .cinemaName(rs.getString("cinema_name"))
                .cinemaAddress(rs.getString("cinema_address"))
                .tickets(tickets)
                .fnbItems(orderItemDAO.findReceiptItems(conn, bookingId))
                .build();
    }
}
