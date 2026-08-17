package soloMapling.server;

import java.io.FileReader;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlReader;

public class MapleVersionManager {

    public static int version = 55;
    public static int itemPoolVersion = 55;

    private static Map<String, String> npcReleaseVersions;
    private static Map<String, String> portalReleaseVersions;

    private static final String portalVersionYaml = "src/main/java/soloMapling/server/portal_versions.yaml";
    private static final String npcVersionYaml = "src/main/java/soloMapling/server/npc_versions.yaml";


    public static int getItemPoolVersion() {
        return itemPoolVersion;
    }

    public static int getVersion() {
        return version;
    }


    public static void loadNPCVersions(String yamlFilePath) {
        try {
            YamlReader reader = new YamlReader(new FileReader(yamlFilePath));
            Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) reader.read();
            npcReleaseVersions = data.get("npc_versions");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNPCinCurrentVersion(int npcId) {
        if (npcReleaseVersions == null) {
            loadNPCVersions(npcVersionYaml);
        }

        String npcVersion = (npcReleaseVersions.get((String.valueOf(npcId))));
        if (npcVersion == null) {
            return true;  // NPC not found in omit list
        }

        return (Integer.parseInt(npcVersion) <= getVersion());
    }

    public static void loadPortalVersions(String yamlFilePath) {
        try {
            YamlReader reader = new YamlReader(new FileReader(yamlFilePath));
            Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) reader.read();
            portalReleaseVersions = data.get("portal_versions");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPortalinCurrentVersion(int portalId) {
        if (portalReleaseVersions == null) {
            loadPortalVersions(portalVersionYaml);
        }

        String portalVersion = (portalReleaseVersions.get((String.valueOf(portalId))));
        if (portalVersion == null) {
            return true;  // Portal not found in omit list
        }

        return (Integer.parseInt(portalVersion) <= getVersion());
    }

}
