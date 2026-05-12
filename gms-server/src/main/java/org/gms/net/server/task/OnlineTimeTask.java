package org.gms.net.server.task;

import org.gms.client.Character;
import org.gms.constants.string.ExtendKey;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class OnlineTimeTask implements Runnable {
    private final AtomicReference<LocalDate> lastUpdated = new AtomicReference<>(LocalDate.now());
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void run() {
        if (!Server.getInstance().isOnline()) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            return;
        }
        LocalDate now = LocalDate.now();
        boolean isNextDay = now.isAfter(lastUpdated.get());
        for (final Channel chan : Server.getInstance().getAllChannels()) {
            if (chan == null || chan.getPlayerStorage() == null) {
                continue;
            }
            for (final Character chr : chan.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                int onlineTime = chr.getCurrentOnlineTime();
                if (onlineTime == -1) {
                    // 避免异常导致running恒为true
                    onlineTime = getInitialOnlineTime(chr);
                } else {
                    onlineTime += 5;
                }
                if (isNextDay || onlineTime < 0) {
                    onlineTime = 0;
                }
                chr.setCurrentOnlineTime(onlineTime);
            }
        }
        running.set(false);
        lastUpdated.set(now);
    }

    private int getInitialOnlineTime(Character chr) {
        try {
            String timeStr = chr.getAbstractPlayerInteraction().getAccountExtendValue(ExtendKey.ONLINE_TIME.getKey(), true);
            return timeStr == null ? 0 : Integer.parseInt(timeStr);
        } catch (Exception e) {
            return 0;
        }
    }
}
