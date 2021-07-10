package provider.wz;

import java.io.File;

public enum WZFiles {
    QUEST("Quest"),
    ETC("Etc"),
    ITEM("Item"),
    CHARACTER("Character"),
    STRING("String"),
    LIST("List"),
    MOB("Mob"),
    MAP("Map"),
    NPC("Npc"),
    REACTOR("Reactor"),
    SKILL("Skill"),
    SOUND("Sound"),
    UI("UI");

    public static final String DIRECTORY = "wz";

    private final String fileName;

    WZFiles(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return new File(getFilePath());
    }

    public String getFilePath() {
        return String.format("%s/%s.wz", DIRECTORY, fileName);
    }
}
