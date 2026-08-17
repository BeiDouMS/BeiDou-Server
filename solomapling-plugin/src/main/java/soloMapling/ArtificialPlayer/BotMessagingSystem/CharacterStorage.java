package soloMapling.ArtificialPlayer.BotMessagingSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotSM;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CharacterStorage {
    // Fable Phase 1 (F7): these registries are written from parallel spawn waves, the
    // dispatcher pool, and bot lifecycle code at once - they must be thread-safe.
    private static Map<Integer, BotSM> activeBotMap = new ConcurrentHashMap<>();
    private static List<Character> currentRespondants = new CopyOnWriteArrayList<>(); // Current respondants
    private static List<Character> inquirer = new CopyOnWriteArrayList<>();
    private static final List<Integer> invisibleBotList = Arrays.asList(100);
    private static int currentIndex = 0;

    public static void addActiveBot(int id, BotSM character) {
        activeBotMap.put(id, character);
    }

    public static void removeActiveBot(int id) {
        activeBotMap.remove(id);
    }

    public static boolean botLoggedIn(int id) {
        return activeBotMap.containsKey(id);
    }

    public static BotSM getBotById(int id) {
        return activeBotMap.get(id);
    }

    public static Map<Integer, BotSM> getAllBots() {
        return activeBotMap;
    }

//    public static Character getCharacterById(int id) {
//        if (botLoggedIn(id)) {
//            return activeBotMap.get(id).getChr();
//        }
//        return null;
//    }


    public static void printAllBots() {
        for (Map.Entry<Integer, BotSM> entry : activeBotMap.entrySet()) {
            int id = entry.getKey();
            BotSM bot = entry.getValue();
            System.out.println("ID: " + id + ", Bot: " + bot + ", IGN: " + bot.getChr().getName());
        }
    }

    public void updateAllBots() {
        for (BotSM bot : activeBotMap.values()) {
            if (bot.getRunning()) {
                bot.updateState();
            }
        }
    }

    public static void addPlayer(Character player) {
        currentRespondants.add(player);
    }

    public static void removePlayer(Character player) {
        currentRespondants.remove(player);
    }

    public static boolean checkIfRespondant(Character player) {
        return currentRespondants.contains(player);
    }

    public static List<Character> getCurrentRespondants() {
        return currentRespondants;
    }

    public static void addInquirer(Character player) {
        inquirer.add(player);
    }

    public static void removeInquirer(Character player) {
        inquirer.remove(player);
    }

    public static boolean checkIfInquirer(Character player) {
        return inquirer.contains(player);
    }

    public static boolean checkIfInvisibleBot(int id) {
        return invisibleBotList.contains(id);
    }

    //    public static synchronized Character getNextInvisibleBot() {
//        int result = invisibleBotList.get(currentIndex);
//        currentIndex = (currentIndex + 1) % invisibleBotList.size();
//        return getCharacterById(result);
//    }

}