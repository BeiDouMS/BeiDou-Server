package org.gms.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.command.Command;
import org.gms.dao.entity.CommandInfoDO;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class CommandInfoService {

    private List<Class<?>> commandClassList;

    @PostConstruct
    public void init() {
        log.info("commandClassList init start...");

        commandClassList = new ArrayList<>();
        commandClassList.addAll(getPackageClasses("org.gms.client.command.commands.gm0"));
        commandClassList.addAll(getPackageClasses("org.gms.client.command.commands.gm1"));
        commandClassList.addAll(getPackageClasses("org.gms.client.command.commands.gm2"));
        commandClassList.addAll(getPackageClasses("org.gms.client.command.commands.gm3"));
        commandClassList.addAll(getPackageClasses("org.gms.client.command.commands.gm4"));
        commandClassList.addAll(getPackageClasses("org.gms.client.command.commands.gm5"));
        commandClassList.addAll(getPackageClasses("org.gms.client.command.commands.gm6"));

        log.info("commandClassList init success, list sie={}", commandClassList.size());
    }

    private List<Class<?>> getPackageClasses(String packageName) {
        List<Class<?>> result = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String classPath = classLoader.getResource(packagePath).getPath();

        try {
            classPath = URLDecoder.decode(classPath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("", e);
            return result;
        }

        File directory = new File(classPath);
        File[] files = directory.listFiles();
        if (files != null) {
            Pattern pattern = Pattern.compile("\\.class$");
            for (File file : files) {
                if (pattern.matcher(file.getName()).find()) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        result.add(clazz);
                    } catch (ClassNotFoundException e) {
                        log.error("", e);
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public Class<? extends Command> getCommandClass(String commandClassName) {
        for (Class<?> clazz : commandClassList) {
            if (clazz.getSimpleName().equals(commandClassName)) {
                return (Class<? extends Command>) clazz;
            }
        }
        return null;
    }

    public List<CommandInfoDO> getLv0Commands() {
        List<CommandInfoDO> results = new ArrayList<>();

        CommandInfoDO commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("help");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("HelpCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("commands");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("HelpCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("droplimit");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("DropLimitCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("time");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("TimeCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("credits");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("StaffCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("uptime");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("UptimeCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("gacha");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("GachaCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("dispose");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("DisposeCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("changel");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("ChangeLanguageCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("equiplv");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("EquipLvCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("showrates");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("ShowRatesCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("rates");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("RatesCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("online");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("OnlineCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("gm");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("GmCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("reportbug");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("ReportBugCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("points");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("ReadPointsCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("joinevent");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("JoinEventCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("leaveevent");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("LeaveEventCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("ranks");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("RanksCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("str");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("StatStrCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("dex");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("StatDexCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("int");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("StatIntCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("luk");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("StatLukCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("enableauth");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("EnableAuthCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("toggleexp");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("ToggleExpCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("mylawn");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("MapOwnerClaimCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("bosshp");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("BossHpCommand");
        results.add(commandInfoDO);

        commandInfoDO = new CommandInfoDO();
        commandInfoDO.setSyntax("mobhp");
        commandInfoDO.setLevel(0);
        commandInfoDO.setCommandClassName("MobHpCommand");
        results.add(commandInfoDO);

        return results;
    }

}
