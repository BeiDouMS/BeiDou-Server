package org.gms.provider.wz;

import org.gms.manager.ServerManager;
import org.gms.property.ServiceProperty;

import java.nio.file.Files;
import java.nio.file.Path;

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

    public static final String DIRECTORY = getWzDirectory();

    private final String fileName;

    WZFiles(String name) {
        this.fileName = name + ".wz";
    }

    public Path getFile() {
        return Path.of(DIRECTORY, fileName);
    }

    public String getFilePath() {
        return getFile().toString();
    }

    private static String getWzDirectory() {
        // Either provide a custom directory path through the "wz-path" property when launching the .jar, or don't provide one to use the default "wz" directory
        String propertyPath = System.getProperty("wz-path");
        if (propertyPath != null && Files.isDirectory(Path.of(propertyPath))) {
            return propertyPath;
        }
        final String oldWzPath = "wz";
        if (Files.exists(Path.of(oldWzPath))) {
            return oldWzPath;
        }
        ServiceProperty serviceProperty = ServerManager.getApplicationContext().getBean(ServiceProperty.class);
        return oldWzPath + "-" + serviceProperty.getLanguage();
    }
}
