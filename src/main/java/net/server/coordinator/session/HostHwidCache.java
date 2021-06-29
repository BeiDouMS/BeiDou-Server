package net.server.coordinator.session;

import net.server.Server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class HostHwidCache {
    private final ConcurrentHashMap<String, HostHwid> hostHwidCache = new ConcurrentHashMap<>(); // Key: remoteHost

    void clearExpired() {
        SessionDAO.deleteExpiredHwidAccounts();

        Instant now = Instant.ofEpochMilli(Server.getInstance().getCurrentTime());
        List<String> remoteHostsToRemove = new ArrayList<>();
        for (Map.Entry<String, HostHwid> entry : hostHwidCache.entrySet()) {
            if (now.isAfter(entry.getValue().expiry())) {
                remoteHostsToRemove.add(entry.getKey());
            }
        }

        for (String remoteHost : remoteHostsToRemove) {
            hostHwidCache.remove(remoteHost);
        }
    }

    void addEntry(String remoteHost, String remoteHwid) {
        hostHwidCache.put(remoteHost, HostHwid.createWithDefaultExpiry(remoteHwid));
    }

    HostHwid getEntry(String remoteHost) {
        return hostHwidCache.get(remoteHost);
    }

    String removeEntryAndGetItsHwid(String remoteHost) {
        HostHwid hostHwid = hostHwidCache.remove(remoteHost);
        return hostHwid == null ? null : hostHwid.hwid();
    }

    String getEntryHwid(String remoteHost) {
        HostHwid hostHwid = hostHwidCache.get(remoteHost);
        return hostHwid == null ? null : hostHwid.hwid();
    }

}
