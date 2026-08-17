package soloMapling.ArtificialPlayer.BotTownSystem;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Machine-owned sidecar for pinned town spots appended by the "mark this spot" command. Kept SEPARATE
// from the hand-authored TownPresence.yaml so appending a pin never has to rewrite (and clobber the
// comments of) that file. Plain append-friendly text - one pin per line: "<mapId>: <x>,<y>". Merged
// into each map's pins at config load. Safe to hand-edit or delete lines.
public final class TownPinsStore {

    private TownPinsStore() {
    }

    private static final String PATH =
            "src/main/java/soloMapling/ArtificialPlayer/BotTownSystem/TownPins.txt";

    // Append one pin. Writes a header the first time the file is created.
    public static synchronized void addPin(int mapId, int x, int y) {
        try {
            File f = new File(PATH);
            boolean writeHeader = !f.exists();
            try (FileWriter fw = new FileWriter(f, true)) {
                if (writeHeader) {
                    fw.write("# Machine-owned pinned town spots (appended by !env townpresence mark).\n");
                    fw.write("# One per line:  <mapId>: <x>,<y>   - merged into TownPresence.yaml pins at load.\n");
                    fw.write("# Safe to hand-edit or delete lines.\n");
                }
                fw.write(mapId + ": " + x + "," + y + "\n");
            }
        } catch (IOException e) {
            System.out.println("[TownPinsStore] failed to append pin: " + e.getMessage());
        }
    }

    // mapId -> its pinned points. Empty if the file doesn't exist yet.
    public static synchronized Map<Integer, List<Point>> load() {
        Map<Integer, List<Point>> out = new HashMap<>();
        File f = new File(PATH);
        if (!f.exists()) {
            return out;
        }
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#")) {
                    continue;
                }
                int colon = s.indexOf(':');
                int comma = s.indexOf(',');
                if (colon < 0 || comma < 0 || comma < colon) {
                    continue;
                }
                try {
                    int mapId = Integer.parseInt(s.substring(0, colon).trim());
                    int x = Integer.parseInt(s.substring(colon + 1, comma).trim());
                    int y = Integer.parseInt(s.substring(comma + 1).trim());
                    out.computeIfAbsent(mapId, k -> new ArrayList<>()).add(new Point(x, y));
                } catch (NumberFormatException ignored) {
                    // skip a malformed line rather than fail the whole load
                }
            }
        } catch (IOException e) {
            System.out.println("[TownPinsStore] failed to read pins: " + e.getMessage());
        }
        return out;
    }

    public static List<Point> forMap(int mapId) {
        return load().getOrDefault(mapId, List.of());
    }
}
