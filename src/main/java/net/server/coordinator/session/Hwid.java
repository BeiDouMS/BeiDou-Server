package net.server.coordinator.session;

import java.util.regex.Pattern;

public class Hwid {
    private static final Pattern VALID_HWID_PATTERN = Pattern.compile("[0-9A-F]{12}_[0-9A-F]{8}");

    public static boolean isValidHwid(String hwid) {
        return VALID_HWID_PATTERN.matcher(hwid).matches();
    }
}
