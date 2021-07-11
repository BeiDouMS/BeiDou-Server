package net.server.coordinator.session;

import java.util.regex.Pattern;

public record Hwid (String hwid) {
    private static final int HWID_LENGTH = 8;
    // First part is a mac address (without dashes), second part is the hwid
    private static final Pattern VALID_RAW_HWID_PATTERN = Pattern.compile("[0-9A-F]{12}_[0-9A-F]{8}");

    public static boolean isValidRawHwid(String rawHwid) {
        return VALID_RAW_HWID_PATTERN.matcher(rawHwid).matches();
    }

    public static Hwid fromClientString(String clientString) throws IllegalArgumentException {
        if (clientString == null) {
            throw new IllegalArgumentException("clientString must not be null");
        }

        String[] split = clientString.split("_");
        if (split.length != 2 || split[1].length() != HWID_LENGTH) {
            throw new IllegalArgumentException("Hwid validation failed for hwid: " + clientString);
        }

        StringBuilder newHwid = new StringBuilder();
        String convert = split[1];

        int len = convert.length();
        for (int i = len - 2; i >= 0; i -= 2) {
            newHwid.append(convert, i, i + 2);
        }
        newHwid.insert(4, "-");

        return new Hwid(newHwid.toString());
    }
}
