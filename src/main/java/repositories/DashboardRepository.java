package repositories;

import config.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepository extends DBContext {

    // --- KPI Queries ---
    public Double getTotalRevenueForMonth(int month, int year) {
        String sql = """
                     SELECT SUM(price) FROM (
                        SELECT price FROM online_tickets WHERE MONTH(created_at) = ? AND YEAR(created_at) = ?
                        UNION ALL
                        SELECT price FROM counter_tickets WHERE MONTH(sold_at) = ? AND YEAR(sold_at) = ?
                    ) AS combined_revenue
                """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, month);
            st.setInt(2, year);
            st.setInt(3, month);
            st.setInt(4, year);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTicketsSoldForMonth(int month, int year) {
        String sql = """
                    SELECT COUNT(*) FROM (
                        SELECT ticket_id FROM online_tickets WHERE MONTH(created_at) = ? AND YEAR(created_at) = ?
                        UNION ALL
                        SELECT ticket_id FROM counter_tickets WHERE MONTH(sold_at) = ? AND YEAR(sold_at) = ?
                    ) AS combined_tickets
                """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, month);
            st.setInt(2, year);
            st.setInt(3, month);
            st.setInt(4, year);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getNewCustomersCount(int month, int year) {
        String sql = "SELECT COUNT(*) FROM users WHERE role_id = 2 AND MONTH(created_at) = ? AND YEAR(created_at) = ?";
        // Assuming role_id 2 is CUSTOMER.
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, month);
            st.setInt(2, year);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getActiveBranchesCount() {
        String sql = "SELECT COUNT(*) FROM cinema_branches WHERE is_active = 1";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalBranchesCount() {
        String sql = "SELECT COUNT(*) FROM cinema_branches";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // --- Chart Queries ---

    public java.util.Map<String, Object> getRevenueByBranch() {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        data.put("label", "Doanh thu");

        String sql = """
                    SELECT cb.branch_name, SUM(combined.price) as revenue
                    FROM (
                        SELECT showtime_id, price FROM online_tickets
                        UNION ALL
                        SELECT showtime_id, price FROM counter_tickets
                    ) AS combined
                    JOIN showtimes s ON combined.showtime_id = s.showtime_id
                    JOIN screening_rooms sr ON s.room_id = sr.room_id
                    JOIN cinema_branches cb ON sr.branch_id = cb.branch_id
                    GROUP BY cb.branch_name
                """;

        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                labels.add(rs.getString("branch_name"));
                values.add(rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.put("labels", labels);
        data.put("data", values);
        return data;
    }

    public java.util.Map<String, Object> getTopMovies(int limit) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        data.put("label", "Doanh thu");

        String sql = """
                    SELECT TOP (?) m.title, SUM(combined.price) as revenue
                    FROM (
                        SELECT showtime_id, price FROM online_tickets
                        UNION ALL
                        SELECT showtime_id, price FROM counter_tickets
                    ) AS combined
                    JOIN showtimes s ON combined.showtime_id = s.showtime_id
                    JOIN movies m ON s.movie_id = m.movie_id
                    GROUP BY m.title
                    ORDER BY revenue DESC
                """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, limit);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    labels.add(rs.getString("title"));
                    values.add(rs.getDouble("revenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.put("labels", labels);
        data.put("data", values);
        return data;
    }

    public java.util.Map<String, Object> getRevenueTrend(int months) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        data.put("label", "Doanh thu");

        // Generate last N months trend
        String sql = """
                     SELECT
                        CONCAT(MONTH(date_col), '/', YEAR(date_col)) as month_col,
                        SUM(price) as revenue,
                        MAX(date_col) as sort_date
                     FROM (
                        SELECT CAST(created_at AS DATE) as date_col, price FROM online_tickets
                        WHERE created_at >= DATEADD(month, -?, GETDATE())
                        UNION ALL
                        SELECT CAST(sold_at AS DATE) as date_col, price FROM counter_tickets
                        WHERE sold_at >= DATEADD(month, -?, GETDATE())
                    ) AS combined
                    GROUP BY MONTH(date_col), YEAR(date_col)
                    ORDER BY sort_date ASC
                """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, months);
            st.setInt(2, months);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    labels.add(rs.getString("month_col"));
                    values.add(rs.getDouble("revenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        data.put("labels", labels);
        data.put("data", values);
        return data;
    }

    public List<java.util.Map<String, Object>> getRecentTransactions(int limit) {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        String refinedSql = """
                     SELECT TOP (?) * FROM (
                        SELECT
                            b.booking_code as ticket_code,
                            COALESCE(u.fullName, 'Online Guest') as customer_name,
                            m.title as movie_title,
                            cb.branch_name,
                            ot.price as amount,
                            ot.created_at as time_transaction
                        FROM online_tickets ot
                        LEFT JOIN bookings b ON ot.booking_id = b.booking_id
                        LEFT JOIN users u ON b.user_id = u.user_id
                        JOIN showtimes s ON ot.showtime_id = s.showtime_id
                        JOIN movies m ON s.movie_id = m.movie_id
                        JOIN screening_rooms sr ON s.room_id = sr.room_id
                        JOIN cinema_branches cb ON sr.branch_id = cb.branch_id

                        UNION ALL

                        SELECT
                            CONCAT('C-', ct.ticket_id) as ticket_code,
                            ct.customer_name,
                            m.title,
                            cb.branch_name,
                            ct.price,
                            ct.sold_at
                        FROM counter_tickets ct
                        JOIN showtimes s ON ct.showtime_id = s.showtime_id
                        JOIN movies m ON s.movie_id = m.movie_id
                        JOIN screening_rooms sr ON s.room_id = sr.room_id
                        JOIN cinema_branches cb ON sr.branch_id = cb.branch_id
                    ) AS combined
                    ORDER BY time_transaction DESC
                """;

        try (PreparedStatement st = connection.prepareStatement(refinedSql)) {
            st.setInt(1, limit);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> t = new java.util.HashMap<>();
                    t.put("ticketCode", rs.getString("ticket_code"));
                    t.put("customerName", rs.getString("customer_name"));
                    t.put("movieTitle", rs.getString("movie_title"));
                    t.put("branchName", rs.getString("branch_name"));
                    t.put("amount", rs.getDouble("amount"));
                    if (rs.getTimestamp("time_transaction") != null) {
                        java.time.LocalDateTime ldt = rs.getTimestamp("time_transaction").toLocalDateTime();
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                                .ofPattern("HH:mm dd/MM/yyyy");
                        t.put("time", ldt.format(formatter));
                    } else {
                        t.put("time", "");
                    }
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public java.util.Map<String, Object> getMovieStatus() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        String sql = """
                    SELECT
                        SUM(CASE WHEN CAST(release_date AS DATE) <= CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) as now_showing,
                        SUM(CASE WHEN CAST(release_date AS DATE) > CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) as coming_soon
                    FROM movies
                """;
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                status.put("nowShowing", rs.getInt("now_showing"));
                status.put("comingSoon", rs.getInt("coming_soon"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }
}
