package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO phục vụ Báo cáo Phân tích Hạ tầng, Tỷ lệ Lấp đầy Phòng chiếu và Admin Dashboard (TV 1 - Dũng).
 * Tách biệt độc lập 100%, chỉ đọc dữ liệu SQL trực tiếp, không phụ thuộc vào DAO module khác.
 */
public class InfrastructureReportDAO {

    // =========================================================================
    // DTO INNER CLASSES CHO BÁO CÁO & DASHBOARD
    // =========================================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchPerformanceDTO {
        private Long cinemaId;
        private String cinemaCode;
        private String cinemaName;
        private String city;
        private boolean isActive;
        private int totalRooms;
        private int totalShowtimes;
        private int totalBookings;
        private BigDecimal totalRevenue;
        private double revenueSharePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentTransactionDTO {
        private Long bookingId;
        private String bookingCode;
        private String customerName;
        private String movieTitle;
        private String cinemaName;
        private String roomName;
        private String channel;
        private String paymentMethod;
        private BigDecimal finalAmount;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomOccupancyDTO {
        private Long roomId;
        private String roomName;
        private Long cinemaId;
        private String cinemaName;
        private String roomType;
        private int totalCapacity;
        private int maintenanceSeatsCount;
        private int totalShowtimes;
        private int totalSeatSupply;
        private int totalSoldTickets;
        private double occupancyRate;
        private BigDecimal roomTicketRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShowtimeOccupancyDTO {
        private Long showtimeId;
        private Long movieId;
        private String movieTitle;
        private String posterUrl;
        private Long cinemaId;
        private String cinemaName;
        private Long roomId;
        private String roomName;
        private String experienceFormat;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String timeSlot; // PRIME_TIME, OFF_PEAK
        private int capacity;
        private int soldSeats;
        private int availableSeats;
        private double occupancyRate;
        private BigDecimal revenue;
    }

    // =========================================================================
    // 1. KPI TỔNG QUAN HỆ THỐNG (ADMIN DASHBOARD)
    // =========================================================================

    public BigDecimal getTotalRevenue() {
        return getTotalRevenue(null);
    }

    public BigDecimal getTotalRevenue(Long cinemaId) {
        String sql;
        if (cinemaId == null) {
            sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'SUCCESS'";
        } else {
            sql = "SELECT COALESCE(SUM(p.amount), 0) FROM payments p " +
                  "JOIN bookings b ON p.booking_id = b.id " +
                  "JOIN showtimes st ON b.showtime_id = st.id " +
                  "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                  "WHERE p.status = 'SUCCESS' AND sr.cinema_id = ?";
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cinemaId != null) ps.setLong(1, cinemaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1).setScale(0, RoundingMode.HALF_UP);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public int getTotalConfirmedBookings() {
        return getTotalConfirmedBookings(null);
    }

    public int getTotalConfirmedBookings(Long cinemaId) {
        String sql;
        if (cinemaId == null) {
            sql = "SELECT COUNT(*) FROM bookings WHERE status = 'CONFIRMED' AND is_deleted = 0";
        } else {
            sql = "SELECT COUNT(DISTINCT b.id) FROM bookings b " +
                  "JOIN showtimes st ON b.showtime_id = st.id " +
                  "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                  "WHERE b.status = 'CONFIRMED' AND b.is_deleted = 0 AND sr.cinema_id = ?";
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cinemaId != null) ps.setLong(1, cinemaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalValidTickets() {
        return getTotalValidTickets(null);
    }

    public int getTotalValidTickets(Long cinemaId) {
        String sql;
        if (cinemaId == null) {
            sql = "SELECT COUNT(*) FROM tickets WHERE status IN ('VALID', 'USED') AND is_deleted = 0";
        } else {
            sql = "SELECT COUNT(*) FROM tickets t " +
                  "JOIN showtimes st ON t.showtime_id = st.id " +
                  "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                  "WHERE t.status IN ('VALID', 'USED') AND t.is_deleted = 0 AND sr.cinema_id = ?";
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cinemaId != null) ps.setLong(1, cinemaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getMaintenanceSeatsCount() {
        return getMaintenanceSeatsCount(null);
    }

    public int getMaintenanceSeatsCount(Long cinemaId) {
        String sql;
        if (cinemaId == null) {
            sql = "SELECT COUNT(*) FROM seats WHERE (status = 'MAINTENANCE' OR is_active = 0) AND is_deleted = 0";
        } else {
            sql = "SELECT COUNT(*) FROM seats s JOIN screening_rooms sr ON s.screening_room_id = sr.id " +
                  "WHERE (s.status = 'MAINTENANCE' OR s.is_active = 0) AND s.is_deleted = 0 AND sr.cinema_id = ?";
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cinemaId != null) ps.setLong(1, cinemaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getAverageOccupancyRate() {
        return getAverageOccupancyRate(null);
    }

    public double getAverageOccupancyRate(Long cinemaId) {
        String sql = "SELECT " +
                     "  CAST(COALESCE(SUM(sub.sold_tickets), 0) AS FLOAT) * 100.0 / NULLIF(SUM(sub.capacity), 0) AS avg_rate " +
                     "FROM (" +
                     "  SELECT st.id, sr.total_capacity AS capacity, " +
                     "         (SELECT COUNT(*) FROM tickets t WHERE t.showtime_id = st.id AND t.is_deleted = 0 AND t.status IN ('VALID', 'USED')) AS sold_tickets " +
                     "  FROM showtimes st " +
                     "  JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                     "  WHERE st.is_deleted = 0 AND st.status != 'CANCELLED' " +
                     "    AND (? IS NULL OR sr.cinema_id = ?)" +
                     ") sub";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, cinemaId, java.sql.Types.BIGINT);
            ps.setObject(2, cinemaId, java.sql.Types.BIGINT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("avg_rate");
                    return Math.round(val * 10.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // =========================================================================
    // 2. HIỆU SUẤT THEO TỪNG CỤM RẠP (BRANCH PERFORMANCE)
    // =========================================================================

    public List<BranchPerformanceDTO> getBranchPerformanceList() {
        return getBranchPerformanceList(null);
    }

    public List<BranchPerformanceDTO> getBranchPerformanceList(Long cinemaId) {
        List<BranchPerformanceDTO> list = new ArrayList<>();
        BigDecimal totalSysRevenue = getTotalRevenue(cinemaId);

        String sql = "SELECT " +
                     "  c.id AS cinema_id, " +
                     "  c.cinema_code, " +
                     "  c.name AS cinema_name, " +
                     "  c.city, " +
                     "  c.is_active, " +
                     "  (SELECT COUNT(*) FROM screening_rooms sr WHERE sr.cinema_id = c.id AND sr.is_deleted = 0) AS total_rooms, " +
                     "  (SELECT COUNT(*) FROM showtimes st JOIN screening_rooms sr ON st.screening_room_id = sr.id WHERE sr.cinema_id = c.id AND st.is_deleted = 0) AS total_showtimes, " +
                     "  (SELECT COUNT(DISTINCT b.id) FROM bookings b JOIN showtimes st ON b.showtime_id = st.id JOIN screening_rooms sr ON st.screening_room_id = sr.id WHERE sr.cinema_id = c.id AND b.status = 'CONFIRMED' AND b.is_deleted = 0) AS total_bookings, " +
                     "  (SELECT COALESCE(SUM(p.amount), 0) FROM payments p JOIN bookings b ON p.booking_id = b.id JOIN showtimes st ON b.showtime_id = st.id JOIN screening_rooms sr ON st.screening_room_id = sr.id WHERE sr.cinema_id = c.id AND p.status = 'SUCCESS') AS total_revenue " +
                     "FROM cinemas c " +
                     "WHERE c.is_deleted = 0 ";
        if (cinemaId != null) {
            sql += "AND c.id = " + cinemaId + " ";
        }
        sql += "ORDER BY total_revenue DESC, c.id ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BigDecimal revenue = rs.getBigDecimal("total_revenue").setScale(0, RoundingMode.HALF_UP);
                double sharePercent = 0.0;
                if (totalSysRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    sharePercent = revenue.multiply(BigDecimal.valueOf(100))
                            .divide(totalSysRevenue, 1, RoundingMode.HALF_UP)
                            .doubleValue();
                }

                list.add(BranchPerformanceDTO.builder()
                        .cinemaId(rs.getLong("cinema_id"))
                        .cinemaCode(rs.getString("cinema_code"))
                        .cinemaName(rs.getString("cinema_name"))
                        .city(rs.getString("city"))
                        .isActive(rs.getBoolean("is_active"))
                        .totalRooms(rs.getInt("total_rooms"))
                        .totalShowtimes(rs.getInt("total_showtimes"))
                        .totalBookings(rs.getInt("total_bookings"))
                        .totalRevenue(revenue)
                        .revenueSharePercent(sharePercent)
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // 3. GIAO DỊCH GẦN ĐÂY (RECENT TRANSACTIONS)
    // =========================================================================

    public List<RecentTransactionDTO> getRecentTransactions(int limit) {
        return getRecentTransactions(limit, null);
    }

    public List<RecentTransactionDTO> getRecentTransactions(int limit, Long cinemaId) {
        List<RecentTransactionDTO> list = new ArrayList<>();
        String sql = "SELECT TOP (?) " +
                     "  b.id AS booking_id, " +
                     "  b.booking_code, " +
                     "  COALESCE(u.full_name, N'Khách vãng lai (POS)') AS customer_name, " +
                     "  m.title AS movie_title, " +
                     "  c.name AS cinema_name, " +
                     "  sr.name AS room_name, " +
                     "  b.channel, " +
                     "  p.payment_method, " +
                     "  b.final_amount, " +
                     "  b.status AS booking_status, " +
                     "  b.created_at " +
                     "FROM bookings b " +
                     "JOIN payments p ON p.booking_id = b.id " +
                     "LEFT JOIN users u ON b.user_id = u.id " +
                     "JOIN showtimes st ON b.showtime_id = st.id " +
                     "JOIN movies m ON st.movie_id = m.id " +
                     "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                     "JOIN cinemas c ON sr.cinema_id = c.id " +
                     "WHERE b.is_deleted = 0 ";
        if (cinemaId != null) {
            sql += "AND c.id = " + cinemaId + " ";
        }
        sql += "ORDER BY b.created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 8);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    list.add(RecentTransactionDTO.builder()
                            .bookingId(rs.getLong("booking_id"))
                            .bookingCode(rs.getString("booking_code"))
                            .customerName(rs.getString("customer_name"))
                            .movieTitle(rs.getString("movie_title"))
                            .cinemaName(rs.getString("cinema_name"))
                            .roomName(rs.getString("room_name"))
                            .channel(rs.getString("channel"))
                            .paymentMethod(rs.getString("payment_method"))
                            .finalAmount(rs.getBigDecimal("final_amount"))
                            .status(rs.getString("booking_status"))
                            .createdAt(ldt)
                            .build());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // 4. BÁO CÁO LẤP ĐẦY THEO PHÒNG CHIẾU (HALL OCCUPANCY SUMMARY)
    // =========================================================================

    private String buildShowtimeFilter(boolean pastOnly, String timeSlot) {
        StringBuilder f = new StringBuilder();
        if (pastOnly) {
            f.append(" AND st.start_time <= GETDATE() ");
        }
        if ("PRIME_TIME".equalsIgnoreCase(timeSlot)) {
            f.append(" AND DATEPART(HOUR, st.start_time) BETWEEN 18 AND 22 ");
        } else if ("OFF_PEAK".equalsIgnoreCase(timeSlot)) {
            f.append(" AND (DATEPART(HOUR, st.start_time) < 18 OR DATEPART(HOUR, st.start_time) > 22) ");
        }
        return f.toString();
    }

    public List<RoomOccupancyDTO> getRoomOccupancySummaries(Long cinemaId, Long roomId, String roomType,
                                                              boolean pastOnly, String timeSlot) {
        List<RoomOccupancyDTO> list = new ArrayList<>();
        String stFilter = buildShowtimeFilter(pastOnly, timeSlot);
        String sql = "SELECT " +
                     "  sr.id AS room_id, " +
                     "  sr.name AS room_name, " +
                     "  c.id AS cinema_id, " +
                     "  c.name AS cinema_name, " +
                     "  sr.room_type, " +
                     "  sr.total_capacity, " +
                     "  (SELECT COUNT(*) FROM seats s WHERE s.screening_room_id = sr.id AND (s.status = 'MAINTENANCE' OR s.is_active = 0) AND s.is_deleted = 0) AS maintenance_seats, " +
                     "  (SELECT COUNT(*) FROM showtimes st WHERE st.screening_room_id = sr.id AND st.is_deleted = 0 AND st.status != 'CANCELLED'" + stFilter + ") AS total_showtimes, " +
                     "  (SELECT COUNT(t.id) FROM tickets t JOIN showtimes st ON t.showtime_id = st.id WHERE st.screening_room_id = sr.id AND t.is_deleted = 0 AND t.status IN ('VALID', 'USED')" + stFilter + ") AS total_sold_tickets, " +
                     "  (SELECT COALESCE(SUM(t.ticket_price), 0) FROM tickets t JOIN showtimes st ON t.showtime_id = st.id WHERE st.screening_room_id = sr.id AND t.is_deleted = 0 AND t.status IN ('VALID', 'USED')" + stFilter + ") AS room_ticket_revenue " +
                     "FROM screening_rooms sr " +
                     "JOIN cinemas c ON sr.cinema_id = c.id " +
                     "WHERE sr.is_deleted = 0 " +
                     "  AND (? IS NULL OR sr.cinema_id = ?) " +
                     "  AND (? IS NULL OR sr.id = ?) " +
                     "  AND (? IS NULL OR sr.room_type = ?) " +
                     "ORDER BY c.id ASC, sr.id ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (cinemaId != null && cinemaId > 0) {
                ps.setLong(1, cinemaId);
                ps.setLong(2, cinemaId);
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
                ps.setNull(2, java.sql.Types.BIGINT);
            }

            if (roomId != null && roomId > 0) {
                ps.setLong(3, roomId);
                ps.setLong(4, roomId);
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            if (roomType != null && !roomType.isBlank() && !"ALL".equalsIgnoreCase(roomType)) {
                ps.setString(5, roomType.trim());
                ps.setString(6, roomType.trim());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int cap = rs.getInt("total_capacity");
                    int maintenance = rs.getInt("maintenance_seats");
                    int effectiveCap = Math.max(0, cap - maintenance);
                    int totalSt = rs.getInt("total_showtimes");
                    int totalSold = rs.getInt("total_sold_tickets");
                    int totalSeatSupply = totalSt * effectiveCap;
                    double rate = 0.0;
                    if (totalSeatSupply > 0) {
                        rate = Math.round((totalSold * 100.0 / totalSeatSupply) * 10.0) / 10.0;
                    }

                    list.add(RoomOccupancyDTO.builder()
                            .roomId(rs.getLong("room_id"))
                            .roomName(rs.getString("room_name"))
                            .cinemaId(rs.getLong("cinema_id"))
                            .cinemaName(rs.getString("cinema_name"))
                            .roomType(rs.getString("room_type"))
                            .totalCapacity(cap)
                            .maintenanceSeatsCount(rs.getInt("maintenance_seats"))
                            .totalShowtimes(totalSt)
                            .totalSeatSupply(totalSeatSupply)
                            .totalSoldTickets(totalSold)
                            .occupancyRate(rate)
                            .roomTicketRevenue(rs.getBigDecimal("room_ticket_revenue").setScale(0, RoundingMode.HALF_UP))
                            .build());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // 5. BÁO CÁO LẤP ĐẦY THEO TỪNG SUẤT CHIẾU (SHOWTIME OCCUPANCY DETAILS)
    // =========================================================================

    public List<ShowtimeOccupancyDTO> getShowtimeOccupancyDetails(Long cinemaId, Long roomId, String roomType,
                                                                  String timeSlot, boolean pastOnly) {
        List<ShowtimeOccupancyDTO> list = new ArrayList<>();
        String stFilter = buildShowtimeFilter(pastOnly, timeSlot);
        String sql = "SELECT " +
                     "  st.id AS showtime_id, " +
                     "  m.id AS movie_id, " +
                     "  m.title AS movie_title, " +
                     "  m.poster_url, " +
                     "  c.id AS cinema_id, " +
                     "  c.name AS cinema_name, " +
                     "  sr.id AS room_id, " +
                     "  sr.name AS room_name, " +
                     "  st.experience_format, " +
                     "  st.start_time, " +
                     "  st.end_time, " +
                     "  (sr.total_capacity - (SELECT COUNT(*) FROM seats s WHERE s.screening_room_id = sr.id AND (s.status = 'MAINTENANCE' OR s.is_active = 0) AND s.is_deleted = 0)) AS capacity, " +
                     "  (SELECT COUNT(*) FROM tickets t WHERE t.showtime_id = st.id AND t.is_deleted = 0 AND t.status IN ('VALID', 'USED')) AS sold_seats, " +
                     "  (SELECT COALESCE(SUM(t.ticket_price), 0) FROM tickets t WHERE t.showtime_id = st.id AND t.is_deleted = 0 AND t.status IN ('VALID', 'USED')) AS revenue " +
                     "FROM showtimes st " +
                     "JOIN movies m ON st.movie_id = m.id " +
                     "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                     "JOIN cinemas c ON sr.cinema_id = c.id " +
                     "WHERE st.is_deleted = 0 AND st.status != 'CANCELLED' " + stFilter +
                     "  AND (? IS NULL OR c.id = ?) " +
                     "  AND (? IS NULL OR sr.id = ?) " +
                     "  AND (? IS NULL OR sr.room_type = ?) " +
                     "ORDER BY st.start_time DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (cinemaId != null && cinemaId > 0) {
                ps.setLong(1, cinemaId);
                ps.setLong(2, cinemaId);
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
                ps.setNull(2, java.sql.Types.BIGINT);
            }

            if (roomId != null && roomId > 0) {
                ps.setLong(3, roomId);
                ps.setLong(4, roomId);
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            if (roomType != null && !roomType.isBlank() && !"ALL".equalsIgnoreCase(roomType)) {
                ps.setString(5, roomType.trim());
                ps.setString(6, roomType.trim());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp startTs = rs.getTimestamp("start_time");
                    Timestamp endTs = rs.getTimestamp("end_time");
                    LocalDateTime startTime = startTs != null ? startTs.toLocalDateTime() : LocalDateTime.now();
                    LocalDateTime endTime = endTs != null ? endTs.toLocalDateTime() : startTime.plusHours(2);

                    int hour = startTime.getHour();
                    String slot = (hour >= 18 && hour <= 22) ? "PRIME_TIME" : "OFF_PEAK";

                    // timeSlot đã được lọc trong SQL bằng DATEPART

                    int cap = rs.getInt("capacity");
                    int sold = rs.getInt("sold_seats");
                    int avail = Math.max(0, cap - sold);
                    double rate = 0.0;
                    if (cap > 0) {
                        rate = Math.round((sold * 100.0 / cap) * 10.0) / 10.0;
                    }

                    list.add(ShowtimeOccupancyDTO.builder()
                            .showtimeId(rs.getLong("showtime_id"))
                            .movieId(rs.getLong("movie_id"))
                            .movieTitle(rs.getString("movie_title"))
                            .posterUrl(rs.getString("poster_url"))
                            .cinemaId(rs.getLong("cinema_id"))
                            .cinemaName(rs.getString("cinema_name"))
                            .roomId(rs.getLong("room_id"))
                            .roomName(rs.getString("room_name"))
                            .experienceFormat(rs.getString("experience_format"))
                            .startTime(startTime)
                            .endTime(endTime)
                            .timeSlot(slot)
                            .capacity(cap)
                            .soldSeats(sold)
                            .availableSeats(avail)
                            .occupancyRate(rate)
                            .revenue(rs.getBigDecimal("revenue").setScale(0, RoundingMode.HALF_UP))
                            .build());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
