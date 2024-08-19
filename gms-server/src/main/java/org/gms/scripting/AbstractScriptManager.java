/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

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
package org.gms.scripting;

import org.gms.client.Client;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.gms.manager.ServerManager;
import org.gms.property.ServiceProperty;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Matze
 */
public abstract class AbstractScriptManager {
    private static final Logger log = LoggerFactory.getLogger(AbstractScriptManager.class);
    private final ScriptEngineFactory sef;

    protected AbstractScriptManager() {
        sef = new ScriptEngineManager().getEngineByName("graal.js").getFactory();
    }

    protected ScriptEngine getInvocableScriptEngine(String path) {
        // 优先取语言文件夹，没有则取scripts
        String scriptName = "scripts";
        ServiceProperty serviceProperty = ServerManager.getApplicationContext().getBean(ServiceProperty.class);
        String scriptLangName = scriptName + "-" + serviceProperty.getLanguage();

        Path scriptPath = Path.of(scriptName, path);
        Path scriptLangPath = Path.of(scriptLangName, path);

        Path actualPath;
        if (Files.exists(scriptLangPath)) {
            actualPath = scriptLangPath;
        } else if (Files.exists(scriptPath)){
            actualPath = scriptPath;
        } else {
            return null;
        }

        ScriptEngine engine = sef.getScriptEngine();
        if (!(engine instanceof GraalJSScriptEngine graalScriptEngine)) {
            throw new IllegalStateException(I18nUtil.getExceptionMessage("AbstractScriptManager.getInvocableScriptEngine.exception1"));
        }

        enableScriptHostAccess(graalScriptEngine);

        try (BufferedReader br = Files.newBufferedReader(actualPath, StandardCharsets.UTF_8)) {
            engine.eval(br);
        } catch (final ScriptException | IOException t) {
            log.warn(I18nUtil.getLogMessage("AbstractScriptManager.getInvocableScriptEngine.warn1"), path, t);
            return null;
        }

        return graalScriptEngine;
    }

    protected ScriptEngine getInvocableScriptEngine(String path, Client c) {
        ScriptEngine engine = c.getScriptEngine("scripts/" + path);
        if (engine == null) {
            engine = getInvocableScriptEngine(path);
            c.setScriptEngine(path, engine);
        }

        return engine;
    }

    /**
     * Allow usage of "Java.type()" in script to look up host class
     */
    private void enableScriptHostAccess(GraalJSScriptEngine engine) {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
    }

    protected void resetContext(String path, Client c) {
        c.removeScriptEngine("scripts/" + path);
    }
}
