package tools.mapletools;

import org.apache.commons.io.FileUtils;
import provider.wz.WZFiles;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author RonanLana
 * <p>
 * The main objective of this tool is to locate all mapids that doesn't have
 * the "info" node in their WZ node tree.
 */
public class MapInfoRetriever {
    private static final Path OUTPUT_FILE = ToolConstants.getOutputFile("map_info_report.txt");
    private static final List<Integer> missingInfo = new ArrayList<>();

    private static BufferedReader bufferedReader = null;
    private static byte status = 0;
    private static boolean hasInfo;

    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[50];
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
    }

    private static boolean translateToken(String token) {
        String d;
        int temp;

        if (token.contains("/imgdir")) {
            status -= 1;
        } else if (token.contains("imgdir")) {
            if (status == 1) {
                d = getName(token);
                if (d.contains("info")) {
                    hasInfo = true;
                    return true;
                }

                temp = status;
                forwardCursor(temp);
            }

            status += 1;
        }

        return false;
    }

    private static void searchMapDirectory(int mapArea) {
        final File mapDirectory = new File(WZFiles.MAP.getFilePath() + "/Map/Map" + mapArea);
        try {
            Iterator<File> iter = FileUtils.iterateFiles(mapDirectory, new String[]{"xml"}, true);
            System.out.println("Parsing map area " + mapArea);

            while (iter.hasNext()) {
                File file = iter.next();
                searchMapFile(file);
            }
        } catch (UncheckedIOException e) {
            System.err.println("Directory " + mapDirectory.getPath() + " does not exist");
        }
    }

    private static void searchMapFile(File file) {
        // This will reference one line at a time
        String line = null;

        try {
            InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(fileReader);

            hasInfo = false;
            status = 0;

            while ((line = bufferedReader.readLine()) != null) {
                if (translateToken(line)) {
                    break;
                }
            }

            if (!hasInfo) {
                missingInfo.add(Integer.valueOf(file.getName().split(".img.xml")[0]));
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException ex) {
            System.out.println("Error reading file '" + file.getName() + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private static void writeReport() {
		try (PrintWriter printWriter = new PrintWriter(Files.newOutputStream(OUTPUT_FILE))) {
			if (!missingInfo.isEmpty()) {
				for (Integer i : missingInfo) {
					printWriter.println(i);
				}
			} else {
				printWriter.println("All map files contain 'info' node.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static void main(String[] args) {
        for (int i = 0; i <= 9; i++) {
            searchMapDirectory(i);
        }
        writeReport();
    }

}

