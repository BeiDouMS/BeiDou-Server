package tools.mapletools;

import provider.wz.WZFiles;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author RonanLana
 * <p>
 * This application parses skillbook XMLs, filling up stack amount of those
 * items to 100 (eliminating limitations on held skillbooks, now using
 * default stack quantity expected from USE items).
 * <p>
 * Estimated parse time: 10 seconds
 */
public class SkillbookStackUpdate {
    private static final File INPUT_DIRECTORY = new File(WZFiles.ITEM.getFile(), "Consume");
    private static final File OUTPUT_DIRECTORY = ToolConstants.getOutputFile("skillbook-update");
    private static final int INITIAL_STRING_LENGTH = 50;

    private static PrintWriter printWriter = null;
    private static BufferedReader bufferedReader = null;
    private static int status = 0;

    private static boolean isSkillMasteryBook(int itemid) {
        return itemid >= 2280000 && itemid < 2300000;
    }

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        if (j < i) {
            return "0";           //node value containing 'name' in it's scope, cheap fix since we don't deal with strings anyway
        }

        dest = new char[INITIAL_STRING_LENGTH];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        return (d.trim());
    }

    private static String getValue(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("value");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[INITIAL_STRING_LENGTH];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        return (d.trim());
    }

    private static void forwardCursor(int st) {
        String line = null;

        try {
            while (status >= st && (line = bufferedReader.readLine()) != null) {
                simpleToken(line);
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
        printWriter.println(token);
    }

    private static void translateItemToken(String token) {
        if (token.contains("/imgdir")) {
            status -= 1;
        } else if (token.contains("imgdir")) {
            status += 1;

            if (status == 2) {      //itemid
                int itemid = Integer.parseInt(getName(token));

                if (!isSkillMasteryBook(itemid)) {
                    printWriter.println(token);
                    forwardCursor(status);
                    return;
                }
            }
        } else {
            if (status == 3) {
                if (getName(token).contentEquals("slotMax")) {
                    printWriter.println("      <int name=\"slotMax\" value=\"100\"/>");
                    return;
                }
            }
        }

        printWriter.println(token);
    }

    private static void parseItemFile(File file, File outputFile) {
        setupDirectories(outputFile);
        // This will reference one line at a time
        String line = null;

        try {
            printWriter = new PrintWriter(outputFile);
            InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                translateItemToken(line);
            }

            bufferedReader.close();
            fileReader.close();
            printWriter.close();
        } catch (IOException ex) {
            System.out.println("Error reading file '" + file.getName() + "'");
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupDirectories(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    private static void parseItemDirectory(File inputDirectory, File outputDirectory) {
        for (File f : inputDirectory.listFiles()) {
            parseItemFile(f, new File(outputDirectory, f.getName()));
        }
    }

    public static void main(String[] args) {
        System.out.println("Reading item files...");
        parseItemDirectory(INPUT_DIRECTORY, OUTPUT_DIRECTORY);
        System.out.println("Done!");
    }

}
