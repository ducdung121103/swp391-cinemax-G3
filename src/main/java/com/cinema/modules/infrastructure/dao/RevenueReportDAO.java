package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import com.cinema.common.dto.PageResult;
import com.cinema.model.Cinema;
import com.cinema.model.Movie;
import com.cinema.modules.infrastructure.dto.BookingDetailDTO;
import com.cinema.modules.infrastructure.dto.RevenueBreakdownDTO;
import com.cinema.modules.infrastructure.dto.RevenueFilterDTO;
import com.cinema.modules.infrastructure.dto.RevenueKpiDTO;
import com.cinema.modules.infrastructure.dto.RevenueTimeSeriesDTO;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for Revenue & Sales Reports (SRS: M-10.1).
 * Canonical revenue definition: payments.status = 'SUCCESS' AND bookings.status = 'CONFIRMED' AND is_deleted = 0.
 */
public class RevenueReportDAO {

    private static final Logger logger = LoggerFactory.getLogger(RevenueReportDAO.class);

    /**
     * Aggregates key revenue KPIs for a given date range and filter criteria.
     */
    public RevenueKpiDTO getKpiMetrics(LocalDate from, LocalDate to, Long cinemaId, Long movieId, String channel) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH BookingMetrics AS ( ");
        sql.append("  SELECT b.id, p.amount, b.total_tickets_amount, b.total_fnb_amount, b.discount_amount ");
        sql.append("  FROM payments p ");
        sql.append("  JOIN bookings b ON p.booking_id = b.id ");
        sql.append("  JOIN showtimes st ON b.showtime_id = st.id ");
        sql.append("  JOIN screening_rooms sr ON st.screening_room_id = sr.id ");
        sql.append("  JOIN cinemas c ON sr.cinema_id = c.id ");
        sql.append("  JOIN movies m ON st.movie_id = m.id ");
        sql.append("  WHERE p.status = 'SUCCESS' AND b.status = 'CONFIRMED' AND b.is_deleted = 0 ");
        sql.append("    AND sr.is_deleted = 0 AND c.is_deleted = 0 ");
        sql.append("    AND p.payment_time >= ? AND p.payment_time <= ? ");

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(from.atStartOfDay()));
        params.add(Timestamp.valueOf(to.atTime(23, 59, 59, 999999999)));

        appendCommonFilters(sql, params, cinemaId, movieId, channel);
        sql.append(") ");
        sql.append("SELECT ");
        sql.append("  COALESCE(SUM(amount), 0) AS total_revenue, ");
        sql.append("  COALESCE(SUM(total_tickets_amount), 0) AS ticket_revenue, ");
        sql.append("  COALESCE(SUM(total_fnb_amount), 0) AS fnb_revenue, ");
        sql.append("  COALESCE(SUM(discount_amount), 0) AS discount_amount, ");
        sql.append("  COUNT(id) AS confirmed_bookings, ");
        sql.append("  COALESCE((SELECT COUNT(t.id) FROM tickets t WHERE t.booking_id IN (SELECT id FROM BookingMetrics) AND t.status IN ('VALID', 'USED') AND t.is_deleted = 0), 0) AS tickets_sold ");
        sql.append("FROM BookingMetrics");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal totalRev = rs.getBigDecimal("total_revenue").setScale(0, RoundingMode.HALF_UP);
                    BigDecimal ticketRev = rs.getBigDecimal("ticket_revenue").setScale(0, RoundingMode.HALF_UP);
                    BigDecimal fnbRev = rs.getBigDecimal("fnb_revenue").setScale(0, RoundingMode.HALF_UP);
                    BigDecimal discAmt = rs.getBigDecimal("discount_amount").setScale(0, RoundingMode.HALF_UP);
                    int bookings = rs.getInt("confirmed_bookings");
                    int tickets = rs.getInt("tickets_sold");

                    BigDecimal aov = bookings > 0 ? totalRev.divide(BigDecimal.valueOf(bookings), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                    BigDecimal atp = tickets > 0 ? ticketRev.divide(BigDecimal.valueOf(tickets), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;

                    return RevenueKpiDTO.builder()
                            .totalRevenue(totalRev)
                            .ticketRevenue(ticketRev)
                            .fnbRevenue(fnbRev)
                            .discountAmount(discAmt)
                            .totalConfirmedBookings(bookings)
                            .totalTicketsSold(tickets)
                            .averageOrderValue(aov)
                            .averageTicketPrice(atp)
                            .build();
                }
            }
        } catch (SQLException e) {
            logger.error("Error computing revenue KPI metrics", e);
        }

        return RevenueKpiDTO.builder().build();
    }



    /**
     * Retrieves revenue breakdown by Movie (Top N).
     */
    public List<RevenueBreakdownDTO> getRevenueByMovie(LocalDate from, LocalDate to, Long cinemaId, Long movieId, String channel, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH BookingMovie AS ( ");
        sql.append("  SELECT b.id AS booking_id, m.id AS movie_id, m.title AS movie_title, p.amount ");
        sql.append("  FROM payments p ");
        sql.append("  JOIN bookings b ON p.booking_id = b.id ");
        sql.append("  JOIN showtimes st ON b.showtime_id = st.id ");
        sql.append("  JOIN screening_rooms sr ON st.screening_room_id = sr.id ");
        sql.append("  JOIN cinemas c ON sr.cinema_id = c.id ");
        sql.append("  JOIN movies m ON st.movie_id = m.id ");
        sql.append("  WHERE p.status = 'SUCCESS' AND b.status = 'CONFIRMED' AND b.is_deleted = 0 ");
        sql.append("    AND sr.is_deleted = 0 AND c.is_deleted = 0 ");
        sql.append("    AND p.payment_time >= ? AND p.payment_time <= ? ");

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(from.atStartOfDay()));
        params.add(Timestamp.valueOf(to.atTime(23, 59, 59, 999999999)));

        appendCommonFilters(sql, params, cinemaId, movieId, channel);
        sql.append(") ");
        sql.append("SELECT TOP (?) ");
        sql.append("  bm.movie_id, bm.movie_title, ");
        sql.append("  COALESCE(SUM(bm.amount), 0) AS total_revenue, ");
        sql.append("  COUNT(bm.booking_id) AS bookings_count, ");
        sql.append("  COALESCE((SELECT COUNT(t.id) FROM tickets t WHERE t.booking_id IN (SELECT bm2.booking_id FROM BookingMovie bm2 WHERE bm2.movie_id = bm.movie_id) AND t.status IN ('VALID', 'USED') AND t.is_deleted = 0), 0) AS tickets_sold ");
        sql.append("FROM BookingMovie bm ");
        sql.append("GROUP BY bm.movie_id, bm.movie_title ");
        sql.append("ORDER BY total_revenue DESC");

        params.add(limit > 0 ? limit : 10);
        return executeBreakdownQuery(sql.toString(), params, "movie_id", "movie_title");
    }

    /**
     * Retrieves revenue breakdown by Cinema.
     */
    public List<RevenueBreakdownDTO> getRevenueByCinema(LocalDate from, LocalDate to, Long cinemaId, Long movieId, String channel) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH BookingCinema AS ( ");
        sql.append("  SELECT b.id AS booking_id, c.id AS cinema_id, c.name AS cinema_name, p.amount ");
        sql.append("  FROM payments p ");
        sql.append("  JOIN bookings b ON p.booking_id = b.id ");
        sql.append("  JOIN showtimes st ON b.showtime_id = st.id ");
        sql.append("  JOIN screening_rooms sr ON st.screening_room_id = sr.id ");
        sql.append("  JOIN cinemas c ON sr.cinema_id = c.id ");
        sql.append("  JOIN movies m ON st.movie_id = m.id ");
        sql.append("  WHERE p.status = 'SUCCESS' AND b.status = 'CONFIRMED' AND b.is_deleted = 0 ");
        sql.append("    AND sr.is_deleted = 0 AND c.is_deleted = 0 ");
        sql.append("    AND p.payment_time >= ? AND p.payment_time <= ? ");

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(from.atStartOfDay()));
        params.add(Timestamp.valueOf(to.atTime(23, 59, 59, 999999999)));

        appendCommonFilters(sql, params, cinemaId, movieId, channel);
        sql.append(") ");
        sql.append("SELECT ");
        sql.append("  bc.cinema_id, bc.cinema_name, ");
        sql.append("  COALESCE(SUM(bc.amount), 0) AS total_revenue, ");
        sql.append("  COUNT(bc.booking_id) AS bookings_count, ");
        sql.append("  COALESCE((SELECT COUNT(t.id) FROM tickets t WHERE t.booking_id IN (SELECT bc2.booking_id FROM BookingCinema bc2 WHERE bc2.cinema_id = bc.cinema_id) AND t.status IN ('VALID', 'USED') AND t.is_deleted = 0), 0) AS tickets_sold ");
        sql.append("FROM BookingCinema bc ");
        sql.append("GROUP BY bc.cinema_id, bc.cinema_name ");
        sql.append("ORDER BY total_revenue DESC");

        return executeBreakdownQuery(sql.toString(), params, "cinema_id", "cinema_name");
    }



    /**
     * Retrieves paginated booking drill-down details.
     */
    public PageResult<BookingDetailDTO> getBookingDetailsPaged(RevenueFilterDTO filter) {
        int page = filter.getPage() > 0 ? filter.getPage() : 1;
        int pageSize = filter.getPageSize() > 0 ? filter.getPageSize() : 20;
        int offset = (page - 1) * pageSize;

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("WHERE p.status = 'SUCCESS' AND b.status = 'CONFIRMED' AND b.is_deleted = 0 ");
        whereClause.append("  AND sr.is_deleted = 0 AND c.is_deleted = 0 ");
        whereClause.append("  AND p.payment_time >= ? AND p.payment_time <= ? ");

        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(filter.getFrom().atStartOfDay()));
        params.add(Timestamp.valueOf(filter.getTo().atTime(23, 59, 59, 999999999)));

        appendCommonFilters(whereClause, params, filter.getCinemaId(), filter.getMovieId(), filter.getChannel());

        // 1. Count total elements
        String countSql = "SELECT COUNT(*) FROM payments p " +
                "JOIN bookings b ON p.booking_id = b.id " +
                "JOIN showtimes st ON b.showtime_id = st.id " +
                "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                "JOIN cinemas c ON sr.cinema_id = c.id " +
                "JOIN movies m ON st.movie_id = m.id " +
                whereClause;

        long totalElements = 0;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalElements = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting bookings for revenue drilldown", e);
        }

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        // 2. Fetch paginated records
        StringBuilder dataSql = new StringBuilder();
        dataSql.append("SELECT b.id AS booking_id, b.booking_code, p.payment_time, m.title AS movie_title, ");
        dataSql.append("       c.name AS cinema_name, sr.name AS room_name, b.channel, p.payment_method, ");
        dataSql.append("       b.total_tickets_amount, b.total_fnb_amount, b.discount_amount, b.final_amount, b.status ");
        dataSql.append("FROM payments p ");
        dataSql.append("JOIN bookings b ON p.booking_id = b.id ");
        dataSql.append("JOIN showtimes st ON b.showtime_id = st.id ");
        dataSql.append("JOIN screening_rooms sr ON st.screening_room_id = sr.id ");
        dataSql.append("JOIN cinemas c ON sr.cinema_id = c.id ");
        dataSql.append("JOIN movies m ON st.movie_id = m.id ");
        dataSql.append(whereClause);
        dataSql.append("ORDER BY p.payment_time DESC ");
        dataSql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(offset);
        dataParams.add(pageSize);

        List<BookingDetailDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(dataSql.toString())) {
            setParameters(ps, dataParams);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp pt = rs.getTimestamp("payment_time");
                    list.add(BookingDetailDTO.builder()
                            .bookingId(rs.getLong("booking_id"))
                            .bookingCode(rs.getString("booking_code"))
                            .paymentTime(pt != null ? pt.toLocalDateTime() : null)
                            .movieTitle(rs.getString("movie_title"))
                            .cinemaName(rs.getString("cinema_name"))
                            .roomName(rs.getString("room_name"))
                            .channel(rs.getString("channel"))
                            .paymentMethod(rs.getString("payment_method"))
                            .ticketsAmount(rs.getBigDecimal("total_tickets_amount").setScale(0, RoundingMode.HALF_UP))
                            .fnbAmount(rs.getBigDecimal("total_fnb_amount").setScale(0, RoundingMode.HALF_UP))
                            .discountAmount(rs.getBigDecimal("discount_amount").setScale(0, RoundingMode.HALF_UP))
                            .finalAmount(rs.getBigDecimal("final_amount").setScale(0, RoundingMode.HALF_UP))
                            .status(rs.getString("status"))
                            .build());
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching paginated bookings for revenue drilldown", e);
        }

        return PageResult.<BookingDetailDTO>builder()
                .content(list)
                .pageNumber(page)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    /**
     * Retrieves all movies that have showtimes or bookings for dropdown filter.
     */
    public List<Movie> getAllMoviesForFilter() {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT id, title, release_date, duration_minutes FROM movies WHERE is_deleted = 0 ORDER BY title ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Movie m = Movie.builder()
                        .title(rs.getString("title"))
                        .durationMinutes(rs.getInt("duration_minutes"))
                        .build();
                m.setId(rs.getLong("id"));
                list.add(m);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving movies for filter", e);
        }
        return list;
    }

    /**
     * Retrieves all non-deleted cinemas for dropdown filter.
     */
    public List<Cinema> getAllCinemasForFilter() {
        List<Cinema> list = new ArrayList<>();
        String sql = "SELECT id, cinema_code, name, city FROM cinemas WHERE is_deleted = 0 ORDER BY name ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cinema c = Cinema.builder()
                        .cinemaCode(rs.getString("cinema_code"))
                        .name(rs.getString("name"))
                        .city(rs.getString("city"))
                        .build();
                c.setId(rs.getLong("id"));
                list.add(c);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving cinemas for filter", e);
        }
        return list;
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    private void appendCommonFilters(StringBuilder sql, List<Object> params, Long cinemaId, Long movieId, String channel) {
        if (cinemaId != null && cinemaId > 0) {
            sql.append(" AND c.id = ? ");
            params.add(cinemaId);
        }
        if (movieId != null && movieId > 0) {
            sql.append(" AND m.id = ? ");
            params.add(movieId);
        }
        if (channel != null && !channel.isBlank() && !"ALL".equalsIgnoreCase(channel)) {
            sql.append(" AND b.channel = ? ");
            params.add(channel.trim().toUpperCase());
        }
    }

    private void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private List<RevenueBreakdownDTO> executeBreakdownQuery(String sql, List<Object> params, String keyCol, String nameCol) {
        List<RevenueBreakdownDTO> list = new ArrayList<>();
        BigDecimal totalAcrossAll = BigDecimal.ZERO;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal rev = rs.getBigDecimal("total_revenue").setScale(0, RoundingMode.HALF_UP);
                    totalAcrossAll = totalAcrossAll.add(rev);
                    list.add(RevenueBreakdownDTO.builder()
                            .categoryKey(String.valueOf(rs.getObject(keyCol)))
                            .categoryName(rs.getString(nameCol))
                            .revenue(rev)
                            .bookingsCount(rs.getInt("bookings_count"))
                            .ticketsSold(rs.getInt("tickets_sold"))
                            .build());
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing revenue breakdown query", e);
        }

        // Compute share percent
        if (totalAcrossAll.compareTo(BigDecimal.ZERO) > 0) {
            for (RevenueBreakdownDTO item : list) {
                double share = item.getRevenue().multiply(BigDecimal.valueOf(100))
                        .divide(totalAcrossAll, 1, RoundingMode.HALF_UP)
                        .doubleValue();
                item.setSharePercent(share);
            }
        }

        return list;
    }
}
