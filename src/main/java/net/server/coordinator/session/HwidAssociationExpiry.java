package net.server.coordinator.session;

import net.server.Server;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class HwidAssociationExpiry {
    public static Instant getHwidAccountExpiry(int relevance) {
        return Instant.ofEpochMilli(Server.getInstance().getCurrentTime()).plusMillis(hwidExpirationUpdate(relevance));
    }

    private static long hwidExpirationUpdate(int relevance) {
        int degree = getHwidExpirationDegree(relevance);

        final long baseHours = switch (degree) {
            case 0 -> 2;
            case 1 -> TimeUnit.DAYS.toHours(1);
            case 2 -> TimeUnit.DAYS.toHours(7);
            default -> TimeUnit.DAYS.toHours(70);
        };

        int subdegreeTime = (degree * 3) + 1;
        if (subdegreeTime > 10) {
            subdegreeTime = 10;
        }

        return TimeUnit.HOURS.toMillis(baseHours + subdegreeTime);
    }

    private static int getHwidExpirationDegree(int relevance) {
        int degree = 1;
        int subdegree;
        while ((subdegree = 5 * degree) <= relevance) {
            relevance -= subdegree;
            degree++;
        }

        return --degree;
    }
}
