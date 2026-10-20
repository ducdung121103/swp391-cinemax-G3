package com.cinema.modules.operation.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.TicketCheckinLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DAO quản lý nhật ký soát vé (TV 5).
 */
public class TicketCheckinLogDAO {

    public Long insertLog(TicketCheckinLog log) throws SQLException {
        String sql = "INSERT INTO ticket_checkin_logs (ticket_id, staff_id, status) VALUES (?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, log.getTicketId());
            if (log.getStaffId() != null) ps.setLong(2, log.getStaffId()); else ps.setNull(2, java.sql.Types.BIGINT);
            ps.setString(3, log.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return null;
    }
}
