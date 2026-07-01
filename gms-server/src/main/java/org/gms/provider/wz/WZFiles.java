package org.gms.provider.wz;

import org.gms.manager.ServerManager;
import org.gms.property.ServiceProperty;
import org.gms.provider.ServerResourceResolver;

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

    private final String fileName;
    public static final String DIRECTORY = "wz";

    WZFiles(String name) {
        this.fileName = name + ".wz";
    }

    public Path getFile() {
        Path langPath = getLanguageFile();

        // 语言目录存在时优先使用，否则回退到原始 WZ
        return Files.exists(langPath) ? langPath : getBaseFile();
    }

    public Path getBaseFile() {
        return getResolver().resolveDataPath(DIRECTORY, fileName);
    }

    public Path getLanguageFile() {
        ServiceProperty serviceProperty = ServerManager.getApplicationContext().getBean(ServiceProperty.class);
        String langDir = DIRECTORY + "-" + serviceProperty.getLanguage();
        return getResolver().resolveDataPath(langDir, fileName);
    }

    public String getFilePath() {
        return getFile().toString();
    }

    private static ServerResourceResolver getResolver() {
        return ServerManager.getApplicationContext().getBean(ServerResourceResolver.class);
    }
}
