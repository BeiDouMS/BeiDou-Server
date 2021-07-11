package tools.mapletools;

import provider.wz.WZFiles;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author RonanLana
 * <p>
 * This application parses the Character.wz folder inputted and adds/updates the "info/level"
 * node on every known equipment id. This addition enables client-side view of the equipment
 * level attribute on every equipment in the game, given proper item visibility, be it from
 * own equipments or from other players.
 * <p>
 * Estimated parse time: 10 seconds
 */
public class DojoUpdate {
    private static final File INPUT_DIRECTORY = new File(WZFiles.MAP.getFile(), "/Map/Map9");
    private static final File OUTPUT_DIRECTORY = ToolConstants.getOutputFile("dojo-maps");
    private static final int DOJO_MIN_MAP_ID = 925_020_100;
    private static final int DOJO_MAX_MAP_ID = 925_033_804;
    private static final int INITIAL_STRING_LENGTH = 250;

    private static PrintWriter printWriter = null;
    private static BufferedReader bufferedReader = null;
    private static boolean isDojoMapid;
    private static byte status;

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[INITIAL_STRING_LENGTH];
        try {
            token.getChars(i, j, dest, 0);
        } catch (StringIndexOutOfBoundsException e) {
            // do nothing
            return "";
        } catch (Exception e) {
            System.out.println("error in: " + token + "");
            e.printStackTrace();
            try {
                Thread.sleep(100000000);
            } catch (Exception ex) {
            }
        }


        d = new String(dest);
        return (d.trim());
    }

    private static void forwardCursor(int st) {
        String line = null;

        try {
            while (status >= st && (line = bufferedReader.readLine()) != null) {
                simpleToken(line);
                printWriter.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void simpleToken(String token) {
        if (token.contains("/imgdir")) {
            status -= 1;
        } else if (token.contains("imgdir")) {
            status += 1;
        }
    }

    private static void translateToken(String token) {
        if (token.contains("/imgdir")) {
            status -= 1;
            printWriter.println(token);
        } else if (token.contains("imgdir")) {
            printWriter.println(token);
            status += 1;

            if (status == 2) {
                String d = getName(token);
                if (!d.contentEquals("info")) {
                    forwardCursor(status);
                }
            } else if (status > 2) {
                forwardCursor(status);
            }
        } else {
            if (status == 2 && isDojoMapid) {
                String item = getName(token);

                if (item.contentEquals("onFirstUserEnter")) {
                    printWriter.println("    <string name=\"onFirstUserEnter\" value=\"dojang_1st\"/>");
                } else if (item.contentEquals("onUserEnter")) {
                    printWriter.println("    <string name=\"onUserEnter\" value=\"dojang_Eff\"/>");
                } else {
                    printWriter.println(token);
                }
            } else {
                printWriter.println(token);
            }
        }
    }

    private static int getMapId(String fileName) {
        return Integer.parseInt(fileName.substring(0, 9));
    }

    private static void parseDojoData(File file, String curPath) throws IOException {
        int mapId = getMapId(file.getName());
        isDojoMapid = isDojoMapId(mapId);
        if (!isDojoMapid) {
            return;
        }

        printWriter = new PrintWriter(OUTPUT_DIRECTORY.getPath() + "/" + curPath + file.getName(), StandardCharsets.UTF_8);

        InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        bufferedReader = new BufferedReader(fileReader);

        status = 0;

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            translateToken(line);
        }

        bufferedReader.close();
        fileReader.close();

        printFileFooter();
        printWriter.close();
    }

    private static boolean isDojoMapId(int mapId) {
        return mapId >= DOJO_MIN_MAP_ID && mapId <= DOJO_MAX_MAP_ID;
    }

    private static void printFileFooter() {
        printWriter.println("<!--");
        printWriter.println(" # WZ XML File updated by the MapleDojoUpdate feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account info from the server-side WZ.xmls.");
        printWriter.println("-->");
    }

    private static void parseDirectoryDojoData(String curPath) {
        File folder = new File(OUTPUT_DIRECTORY, curPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        System.out.println("Parsing directory '" + curPath + "'");
        folder = new File(INPUT_DIRECTORY, curPath);
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                try {
                    parseDojoData(file, curPath);
                } catch (FileNotFoundException ex) {
                    System.out.println("Unable to open dojo file " + file.getAbsolutePath() + ".");
                } catch (IOException ex) {
                    System.out.println("Error reading dojo file " + file.getAbsolutePath() + ".");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                parseDirectoryDojoData(curPath + file.getName() + "/");
            }
        }
    }

    public static void main(String[] args) {
        parseDirectoryDojoData("");
    }
}
