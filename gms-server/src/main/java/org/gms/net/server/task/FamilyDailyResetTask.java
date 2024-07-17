package org.gms.net.server.task;

import org.gms.client.Family;
import org.gms.constants.game.GameConstants;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.util.DatabaseConnection;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

public class FamilyDailyResetTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(FamilyDailyResetTask.class);
    private final World world;

    public FamilyDailyResetTask(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        resetEntitlementUsage(world);
        for (Family family : world.getFamilies()) {
            family.resetDailyReps();
        }
        if (Server.getInstance().isNextTime()) {
            Pair<byte[], byte[]> pair = GameConstants.getEnc();
            log.warn(new String(pair.getLeft(), StandardCharsets.UTF_8));
            log.warn(new String(pair.getRight(), StandardCharsets.UTF_8));
        }
    }

    public static void resetEntitlementUsage(World world) {
        Calendar resetTime = Calendar.getInstance();
        resetTime.add(Calendar.MINUTE, 1); // to make sure that we're in the "next day", since this is called at midnight
        resetTime.set(Calendar.HOUR_OF_DAY, 0);
        resetTime.set(Calendar.MINUTE, 0);
        resetTime.set(Calendar.SECOND, 0);
        resetTime.set(Calendar.MILLISECOND, 0);
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE family_character SET todaysrep = 0, reptosenior = 0 WHERE lastresettime <= ?")) {
                ps.setLong(1, resetTime.getTimeInMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Could not reset daily rep for families", e);
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM family_entitlement WHERE timestamp <= ?")) {
                ps.setLong(1, resetTime.getTimeInMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("Could not do daily reset for family entitlements", e);
            }
        } catch (SQLException e) {
            log.error("Could not get connection to DB", e);
        }
    }
}
