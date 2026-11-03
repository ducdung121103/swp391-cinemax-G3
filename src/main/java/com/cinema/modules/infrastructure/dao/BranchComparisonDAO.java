package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import com.cinema.modules.infrastructure.dto.CinemaMetricDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enterprise BI & Branch Performance Comparison Data Access Object (M-10.2).
 * Strictly enforces canonical revenue definition:
 * payments.status='SUCCESS' JOIN bookings.status='CONFIRMED' AND b.is_deleted=0.
 */
public class BranchComparisonDAO {

    private static final Logger log = LoggerFactory.getLogger(BranchComparisonDAO.class);

    /**
     * Get distinct active cities where cinema branches operate.
     */
    public List<String> getAvailableCities() {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM cinemas WHERE is_deleted = 0 AND city IS NOT NULL ORDER BY city ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cities.add(rs.getString("city"));
            }
        } catch (SQLException e) {
            log.error("Error retrieving cinema cities", e);
        }
        return cities;
    }

    /**
     * Retrieve all base cinema entities matching city and inclusion of inactive branches.
     */
    public List<CinemaMetricDTO> getBaseCinemas(String city, boolean includeInactive) {
        List<CinemaMetricDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, cinema_code, name, city, is_active FROM cinemas WHERE is_deleted = 0"
        );
        if (!includeInactive) {
            sql.append(" AND is_active = 1");
        }
        if (city != null && !city.trim().isEmpty() && !"ALL".equalsIgnoreCase(city.trim())) {
            sql.append(" AND city = ?");
        }
        sql.append(" ORDER BY name ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (city != null && !city.trim().isEmpty() && !"ALL".equalsIgnoreCase(city.trim())) {
                ps.setString(1, city.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CinemaMetricDTO dto = CinemaMetricDTO.builder()
                            .cinemaId(rs.getLong("id"))
                            .cinemaCode(rs.getString("cinema_code"))
                            .cinemaName(rs.getString("name"))
                            .city(rs.getString("city"))
                            .active(rs.getBoolean("is_active"))
                            .revenue(BigDecimal.ZERO)
                            .comparisonRevenue(BigDecimal.ZERO)
                            .growthPercent(BigDecimal.ZERO)
                            .revenueSharePercent(BigDecimal.ZERO)
                            .ticketRevenue(BigDecimal.ZERO)
                            .fnbRevenue(BigDecimal.ZERO)
                            .confirmedBookings(0)
                            .ticketsSold(0)
                            .averageTicketPrice(BigDecimal.ZERO)
                            .roomsCount(0)
                            .showtimesCount(0)
                            .occupancyRate(BigDecimal.ZERO)
                            .revenuePerRoom(BigDecimal.ZERO)
                            .revenuePerShowtime(BigDecimal.ZERO)
                            .topMovieName("—")
                            .performanceTier(CinemaMetricDTO.PerformanceTier.BELOW_AVERAGE)
                            .build();
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving base cinemas", e);
        }
        return list;
    }

    /**
     * Map screening room count per cinema branch.
     */
    public Map<Long, Integer> getRoomCounts() {
        Map<Long, Integer> map = new HashMap<>();
        String sql = "SELECT cinema_id, COUNT(*) AS room_count FROM screening_rooms WHERE is_deleted = 0 GROUP BY cinema_id";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getLong("cinema_id"), rs.getInt("room_count"));
            }
        } catch (SQLException e) {
            log.error("Error retrieving room counts per branch", e);
        }
        return map;
    }

    /**
     * Aggregate bookings, revenue, F&B revenue, and ticket counts per cinema branch.
     */
    public Map<Long, CinemaBookingAggregate> getBookingAggregates(LocalDate from, LocalDate to) {
        Map<Long, CinemaBookingAggregate> map = new HashMap<>();
        String sql = """
            WITH BookingMetrics AS (
                SELECT 
                    b.id AS booking_id,
                    b.cinema_id,
                    p.amount AS payment_amount,
                    ISNULL((SELECT SUM(oi.subtotal) FROM order_items oi WHERE oi.booking_id = b.id), 0) AS fnb_amount,
                    (SELECT COUNT(t.id) FROM tickets t WHERE t.booking_id = b.id AND t.is_deleted = 0) AS ticket_count
                FROM bookings b
                INNER JOIN payments p ON p.booking_id = b.id AND p.status = 'SUCCESS'
                WHERE b.status = 'CONFIRMED'
                  AND b.is_deleted = 0
                  AND b.booking_time >= ? AND b.booking_time < ?
            )
            SELECT 
                cinema_id,
                SUM(payment_amount) AS total_revenue,
                SUM(fnb_amount) AS total_fnb,
                COUNT(booking_id) AS booking_count,
                SUM(ticket_count) AS total_tickets
            FROM BookingMetrics
            GROUP BY cinema_id
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CinemaBookingAggregate agg = new CinemaBookingAggregate();
                    agg.cinemaId = rs.getLong("cinema_id");
                    agg.revenue = rs.getBigDecimal("total_revenue") != null ? rs.getBigDecimal("total_revenue") : BigDecimal.ZERO;
                    agg.fnbRevenue = rs.getBigDecimal("total_fnb") != null ? rs.getBigDecimal("total_fnb") : BigDecimal.ZERO;
                    agg.bookingCount = rs.getInt("booking_count");
                    agg.ticketsSold = rs.getInt("total_tickets");
                    map.put(agg.cinemaId, agg);
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving booking aggregates per cinema", e);
        }
        return map;
    }

    /**
     * Map total revenue per cinema in the comparison period.
     */
    public Map<Long, BigDecimal> getComparisonRevenues(LocalDate compFrom, LocalDate compTo) {
        Map<Long, BigDecimal> map = new HashMap<>();
        String sql = """
            SELECT 
                b.cinema_id,
                SUM(p.amount) AS comp_revenue
            FROM bookings b
            INNER JOIN payments p ON p.booking_id = b.id AND p.status = 'SUCCESS'
            WHERE b.status = 'CONFIRMED'
              AND b.is_deleted = 0
              AND b.booking_time >= ? AND b.booking_time < ?
            GROUP BY b.cinema_id
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(compFrom.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(compTo.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getLong("cinema_id"),
                            rs.getBigDecimal("comp_revenue") != null ? rs.getBigDecimal("comp_revenue") : BigDecimal.ZERO);
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving comparison revenues per cinema", e);
        }
        return map;
    }

    /**
     * Calculate showtimes count and occupancy rate per cinema.
     * Uses canonical occupancy formula: total sold tickets / sum of room capacity across scheduled showtimes.
     */
    public Map<Long, ShowtimesAndOccupancy> getShowtimesAndOccupancy(LocalDate from, LocalDate to) {
        Map<Long, ShowtimesAndOccupancy> map = new HashMap<>();
        String sql = """
            WITH ShowtimeAgg AS (
                SELECT 
                    s.id AS showtime_id,
                    r.cinema_id,
                    r.total_capacity,
                    (SELECT COUNT(t.id) FROM tickets t WHERE t.showtime_id = s.id AND t.is_deleted = 0) AS sold_seats
                FROM showtimes s
                INNER JOIN screening_rooms r ON r.id = s.screening_room_id AND r.is_deleted = 0
                WHERE s.start_time >= ? AND s.start_time < ?
                  AND s.is_deleted = 0
            )
            SELECT 
                cinema_id,
                COUNT(showtime_id) AS showtimes_count,
                SUM(total_capacity) AS total_capacity,
                SUM(sold_seats) AS total_sold
            FROM ShowtimeAgg
            GROUP BY cinema_id
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShowtimesAndOccupancy so = new ShowtimesAndOccupancy();
                    so.cinemaId = rs.getLong("cinema_id");
                    so.showtimesCount = rs.getInt("showtimes_count");
                    int capacity = rs.getInt("total_capacity");
                    int sold = rs.getInt("total_sold");
                    if (capacity > 0) {
                        so.occupancyRate = BigDecimal.valueOf(sold * 100.0 / capacity)
                                .setScale(2, RoundingMode.HALF_UP);
                    } else {
                        so.occupancyRate = BigDecimal.ZERO;
                    }
                    map.put(so.cinemaId, so);
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving showtimes and occupancy per cinema", e);
        }
        return map;
    }

    /**
     * Find the highest-grossing movie for each cinema branch within the given period.
     */
    public Map<Long, String> getTopMoviePerCinema(LocalDate from, LocalDate to) {
        Map<Long, String> map = new HashMap<>();
        String sql = """
            WITH CinemaMovieRev AS (
                SELECT 
                    b.cinema_id,
                    m.title AS movie_title,
                    SUM(p.amount) AS movie_revenue,
                    ROW_NUMBER() OVER (PARTITION BY b.cinema_id ORDER BY SUM(p.amount) DESC) AS rn
                FROM bookings b
                INNER JOIN payments p ON p.booking_id = b.id AND p.status = 'SUCCESS'
                INNER JOIN showtimes s ON s.id = b.showtime_id
                INNER JOIN movies m ON m.id = s.movie_id
                WHERE b.status = 'CONFIRMED'
                  AND b.is_deleted = 0
                  AND b.booking_time >= ? AND b.booking_time < ?
                GROUP BY b.cinema_id, m.title
            )
            SELECT cinema_id, movie_title FROM CinemaMovieRev WHERE rn = 1
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getLong("cinema_id"), rs.getString("movie_title"));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving top movie per cinema", e);
        }
        return map;
    }

    public static class CinemaBookingAggregate {
        public Long cinemaId;
        public BigDecimal revenue = BigDecimal.ZERO;
        public BigDecimal fnbRevenue = BigDecimal.ZERO;
        public int bookingCount;
        public int ticketsSold;
    }

    public static class ShowtimesAndOccupancy {
        public Long cinemaId;
        public int showtimesCount;
        public BigDecimal occupancyRate = BigDecimal.ZERO;
    }
}
