package net.server.coordinator.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DatabaseConnection;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {
    private static final Logger log = LoggerFactory.getLogger(SessionDAO.class);

    public static void deleteExpiredHwidAccounts() {
        final String query = "DELETE FROM hwidaccounts WHERE expiresat < CURRENT_TIMESTAMP";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warn("Failed to delete expired hwidaccounts", e);
        }
    }

    public static List<String> getHwidsForAccount(Connection con, int accountId) throws SQLException {
        final List<String> hwids = new ArrayList<>();

        final String query = "SELECT hwid FROM hwidaccounts WHERE accountid = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hwids.add(rs.getString("hwid"));
                }
            }
        }

        return hwids;
    }

    public static void registerAccountAccess(Connection con, int accountId, String remoteHwid, Instant expiry)
            throws SQLException {
        final String query = "INSERT INTO hwidaccounts (accountid, hwid, expiresat) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, accountId);
            ps.setString(2, remoteHwid);
            ps.setTimestamp(3, Timestamp.from(expiry));

            ps.executeUpdate();
        }
    }
}
