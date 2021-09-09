/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server;

import client.Character;
import provider.Data;
import provider.DataProvider;
import provider.DataProviderFactory;
import provider.DataTool;
import provider.wz.WZFiles;
import tools.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author RonanLana
 */

/**
 * Only used in 1 script that gives players information about where skillbooks can be found
 */
public class SkillbookInformationProvider {
    private static volatile Map<Integer, SkillBookEntry> foundSkillbooks = new HashMap<>();

    public enum SkillBookEntry {
        UNAVAILABLE,
        QUEST,
        QUEST_BOOK,
        QUEST_REWARD,
        REACTOR,
        SCRIPT
    }

    private static final int SKILLBOOK_MIN_ITEMID = 2280000;
    private static final int SKILLBOOK_MAX_ITEMID = 2300000;  // exclusively

    public static void loadAllSkillbookInformation() {
        Map<Integer, SkillBookEntry> loadedSkillbooks = new HashMap<>();
        loadedSkillbooks.putAll(fetchSkillbooksFromQuests());
        loadedSkillbooks.putAll(fetchSkillbooksFromReactors());
        loadedSkillbooks.putAll(fetchSkillbooksFromScripts());
        SkillbookInformationProvider.foundSkillbooks = loadedSkillbooks;
    }

    private static boolean is4thJobSkill(int itemid) {
        return itemid / 10000 % 10 == 2;
    }

    private static boolean isSkillBook(int itemid) {
        return itemid >= SKILLBOOK_MIN_ITEMID && itemid < SKILLBOOK_MAX_ITEMID;
    }

    private static boolean isQuestBook(int itemid) {
        return itemid >= 4001107 && itemid <= 4001114 || itemid >= 4161015 && itemid <= 4161023;
    }

    private static int fetchQuestbook(Data checkData, String quest) {
        Data questStartData = checkData.getChildByPath(quest).getChildByPath("0");

        Data startReqItemData = questStartData.getChildByPath("item");
        if (startReqItemData != null) {
            for (Data itemData : startReqItemData.getChildren()) {
                int itemId = DataTool.getInt("id", itemData, 0);
                if (isQuestBook(itemId)) {
                    return itemId;
                }
            }
        }

        Data startReqQuestData = questStartData.getChildByPath("quest");
        if (startReqQuestData != null) {
            Set<Integer> reqQuests = new HashSet<>();

            for (Data questStatusData : startReqQuestData.getChildren()) {
                int reqQuest = DataTool.getInt("id", questStatusData, 0);
                if (reqQuest > 0) {
                    reqQuests.add(reqQuest);
                }
            }

            for (Integer reqQuest : reqQuests) {
                int book = fetchQuestbook(checkData, Integer.toString(reqQuest));
                if (book > -1) {
                    return book;
                }
            }
        }

        return -1;
    }

    private static Map<Integer, SkillBookEntry> fetchSkillbooksFromQuests() {
        DataProvider questDataProvider = DataProviderFactory.getDataProvider(WZFiles.QUEST);
        Data actData = questDataProvider.getData("Act.img");
        Data checkData = questDataProvider.getData("Check.img");

        final Map<Integer, SkillBookEntry> loadedSkillbooks = new HashMap<>();
        for (Data questData : actData.getChildren()) {
            for (Data questStatusData : questData.getChildren()) {
                for (Data questNodeData : questStatusData.getChildren()) {
                    String actNodeName = questNodeData.getName();
                    if (actNodeName.contentEquals("item")) {
                        for (Data questItemData : questNodeData.getChildren()) {
                            int itemId = DataTool.getInt("id", questItemData, 0);
                            int itemCount = DataTool.getInt("count", questItemData, 0);

                            if (isSkillBook(itemId) && itemCount > 0) {
                                int questbook = fetchQuestbook(checkData, questData.getName());
                                if (questbook < 0) {
                                    loadedSkillbooks.put(itemId, SkillBookEntry.QUEST);
                                } else {
                                    loadedSkillbooks.put(itemId, SkillBookEntry.QUEST_BOOK);
                                }
                            }
                        }
                    } else if (actNodeName.contentEquals("skill")) {
                        for (Data questSkillData : questNodeData.getChildren()) {
                            int skillId = DataTool.getInt("id", questSkillData, 0);
                            if (is4thJobSkill(skillId)) {
                                // negative itemids are skill rewards

                                int questbook = fetchQuestbook(checkData, questData.getName());
                                if (questbook < 0) {
                                    loadedSkillbooks.put(-skillId, SkillBookEntry.QUEST_REWARD);
                                } else {
                                    loadedSkillbooks.put(-skillId, SkillBookEntry.QUEST_BOOK);
                                }
                            }
                        }
                    }
                }
            }
        }

        return loadedSkillbooks;
    }

    private static Map<Integer, SkillBookEntry> fetchSkillbooksFromReactors() {
        Map<Integer, SkillBookEntry> loadedSkillbooks = new HashMap<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT itemid FROM reactordrops WHERE itemid >= ? AND itemid < ?;")) {
            ps.setInt(1, SKILLBOOK_MIN_ITEMID);
            ps.setInt(2, SKILLBOOK_MAX_ITEMID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.isBeforeFirst()) {
                    while (rs.next()) {
                        loadedSkillbooks.put(rs.getInt("itemid"), SkillBookEntry.REACTOR);
                    }
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return loadedSkillbooks;
    }

    private static void listFiles(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFiles(file.getAbsolutePath(), files);
            }
        }
    }

    private static List<File> listFilesFromDirectoryRecursively(String directory) {
        ArrayList<File> files = new ArrayList<>();
        listFiles(directory, files);

        return files;
    }

    private static Set<Integer> findMatchingSkillbookIdsOnFile(String fileContent) {
        Set<Integer> skillbookIds = new HashSet<>(4);

        Matcher searchM = Pattern.compile("22(8|9)[0-9]{4}").matcher(fileContent);
        int idx = 0;
        while (searchM.find(idx)) {
            idx = searchM.end();
            skillbookIds.add(Integer.valueOf(fileContent.substring(searchM.start(), idx)));
        }

        return skillbookIds;
    }

    private static String readFileToString(File file, String encoding) throws IOException {
        Scanner scanner = new Scanner(file, encoding);
        String text = "";
        try {
            try {
                text = scanner.useDelimiter("\\A").next();
            } finally {
                scanner.close();
            }
        } catch (NoSuchElementException e) {
        }

        return text;
    }

    private static Map<Integer, SkillBookEntry> fileSearchMatchingData(File file) {
        Map<Integer, SkillBookEntry> scriptFileSkillbooks = new HashMap<>();

        try {
            String fileContent = readFileToString(file, "UTF-8");

            Set<Integer> skillbookIds = findMatchingSkillbookIdsOnFile(fileContent);
            for (Integer skillbookId : skillbookIds) {
                scriptFileSkillbooks.put(skillbookId, SkillBookEntry.SCRIPT);
            }
        } catch (IOException ioe) {
            System.out.println("Failed to read " + file.getName() + ".");
            ioe.printStackTrace();
        }

        return scriptFileSkillbooks;
    }

    private static Map<Integer, SkillBookEntry> fetchSkillbooksFromScripts() {
        Map<Integer, SkillBookEntry> scriptSkillbooks = new HashMap<>();

        for (File file : listFilesFromDirectoryRecursively("./scripts")) {
            if (file.getName().endsWith(".js")) {
                scriptSkillbooks.putAll(fileSearchMatchingData(file));
            }
        }

        return scriptSkillbooks;
    }

    public static SkillBookEntry getSkillbookAvailability(int itemId) {
        SkillBookEntry sbe = foundSkillbooks.get(itemId);
        return sbe != null ? sbe : SkillBookEntry.UNAVAILABLE;
    }

    public static List<Integer> getTeachableSkills(Character chr) {
        List<Integer> list = new ArrayList<>();

        for (Integer book : foundSkillbooks.keySet()) {
            if (book >= 0) {
                continue;
            }

            int skillid = -book;
            if (skillid / 10000 == chr.getJob().getId()) {
                if (chr.getMasterLevel(skillid) == 0) {
                    list.add(-skillid);
                }
            }
        }

        return list;
    }

}
