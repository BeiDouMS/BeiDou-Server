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

    WZFiles(String name) {
        this.fileName = name + ".wz";
    }

    public File getFile() {
        return new File(DIRECTORY, fileName);
    }

    public String getFilePath() {
        return getFile().getPath();
    }
}
