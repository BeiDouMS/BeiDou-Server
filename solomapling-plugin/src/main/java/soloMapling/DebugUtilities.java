package soloMapling;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DebugUtilities {


    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static boolean isDebugging() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean()
                .getInputArguments()
                .toString()
                .contains("jdwp");
    }

    public static void debugprint(Object... variables) {
        boolean printDebug = false;
        if (!printDebug) {
            return;
        }
        if (!isDebugging()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalTime.now().format(TIME_FORMATTER)).append("] ");
        sb.append("DEBUG: ");
        for (int i = 0; i < variables.length; i++) {
            sb.append(variables[i]);
            if (i < variables.length - 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb);
    }

    /*
    Pythonic way to create a string. For ease of use.
    String name = "Julia";
    int count = 42;
    String msg = fmt("Hello {}, you have {} items", name, count);
    // "Hello Julia, you have 42 items"
     */
    public static String fmt(String template, Object... args) {
        String result = template;
        for (Object arg : args) {
            result = result.replaceFirst("\\{\\}", String.valueOf(arg));
        }
        return result;
    }

    public static void main(String[] args) {
        // Example usage
        debugprint("Hello", 123, true, 45.67, "Another String");
        debugprint("Player", "ID:", 1001, "Status:", "Active");
    }
}
