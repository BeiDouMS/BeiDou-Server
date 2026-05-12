package org.gms.constants.string;

import lombok.Getter;

public enum ExtendKey {
    ONLINE_TIME("每日在线时间");

    @Getter
    private final String key;

    ExtendKey(String key) {
        this.key = key;
    }
}
