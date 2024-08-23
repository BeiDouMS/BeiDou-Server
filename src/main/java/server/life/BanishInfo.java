package server.life;

import java.util.Objects;

public record BanishInfo(int map, String portal, String msg) {
    public BanishInfo {
        Objects.requireNonNull(portal, "BanishInfo portal");
    }
}
