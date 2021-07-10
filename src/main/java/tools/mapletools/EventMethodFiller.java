package tools.mapletools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author RonanLana
 * <p>
 * This application objective is to read all scripts from the event folder
 * and fill empty functions for every function name not yet present in the
 * script.
 * <p>
 * Estimated parse time: 10 seconds
 */
public class EventMethodFiller {
    private static boolean foundMatchingDataOnFile(String fileContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(fileContent);
        return matcher.find();
    }

    private static void fileSearchMatchingData(File file, Map<Pattern, String> functions) {
        try {
            String fileContent = FileUtils.readFileToString(file, "UTF-8");
            List<String> fillFunctions = new LinkedList<>();

            for (Map.Entry<Pattern, String> f : functions.entrySet()) {
                if (!foundMatchingDataOnFile(fileContent, f.getKey())) {
                    fillFunctions.add(f.getValue());
                }
            }

            if (!fillFunctions.isEmpty()) {
                System.out.println("Filling out " + file.getName() + "...");

                FileWriter fileWriter = new FileWriter(file, true);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                printWriter.println();
                printWriter.println();
                printWriter.println("// ---------- FILLER FUNCTIONS ----------");
                printWriter.println();

                for (String s : fillFunctions) {
                    printWriter.println(s);
                    printWriter.println();
                }

                printWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void filterDirectorySearchMatchingData(String directoryPath, Map<Pattern, String> functions) {
        Iterator iter = FileUtils.iterateFiles(new File(directoryPath), new String[]{"sql", "js", "txt", "java"}, true);

        while (iter.hasNext()) {
            File file = (File) iter.next();
            fileSearchMatchingData(file, functions);
        }
    }

    private static Pattern compileJsFunctionPattern(String function) {
        String jsFunction = "function(\\s)+";
        return Pattern.compile(jsFunction + function);
    }

    private static Map<Pattern, String> getFunctions() {
        Map<Pattern, String> functions = new HashMap<>();
        functions.put(compileJsFunctionPattern("playerEntry"), "function playerEntry(eim, player) {}");
        functions.put(compileJsFunctionPattern("playerExit"), "function playerExit(eim, player) {}");
        functions.put(compileJsFunctionPattern("scheduledTimeout"), "function scheduledTimeout(eim) {}");
        functions.put(compileJsFunctionPattern("playerUnregistered"), "function playerUnregistered(eim, player) {}");
        functions.put(compileJsFunctionPattern("changedLeader"), "function changedLeader(eim, leader) {}");
        functions.put(compileJsFunctionPattern("monsterKilled"), "function monsterKilled(mob, eim) {}");
        functions.put(compileJsFunctionPattern("allMonstersDead"), "function allMonstersDead(eim) {}");
        functions.put(compileJsFunctionPattern("playerDisconnected"), "function playerDisconnected(eim, player) {}");
        functions.put(compileJsFunctionPattern("monsterValue"), "function monsterValue(eim, mobid) {return 0;}");
        functions.put(compileJsFunctionPattern("dispose"), "function dispose() {}");
        functions.put(compileJsFunctionPattern("leftParty"), "function leftParty(eim, player) {}");
        functions.put(compileJsFunctionPattern("disbandParty"), "function disbandParty(eim, player) {}");
        functions.put(compileJsFunctionPattern("clearPQ"), "function clearPQ(eim) {}");
        functions.put(compileJsFunctionPattern("afterSetup"), "function afterSetup(eim) {}");
        functions.put(compileJsFunctionPattern("cancelSchedule"), "function cancelSchedule() {}");
        functions.put(compileJsFunctionPattern("setup"), "function setup(eim, leaderid) {}");
        //put(compileJsFunctionPattern("getEligibleParty"), "function getEligibleParty(party) {}"); not really needed
        return functions;
    }

    public static void main(String[] args) {
        filterDirectorySearchMatchingData(ToolConstants.SCRIPTS_PATH + "/event", getFunctions());
    }

}
