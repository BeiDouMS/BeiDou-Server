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
import org.gms.provider.ServerResourceResolver;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Matze
 */
public abstract class AbstractScriptManager {
    private static final Logger log = LoggerFactory.getLogger(AbstractScriptManager.class);
    private static final String SCRIPT_DIRECTORY = "scripts";
    private final ScriptEngineFactory sef;

    protected AbstractScriptManager() {
        sef = new ScriptEngineManager().getEngineByName("graal.js").getFactory();
    }

    protected ScriptEngine getInvocableScriptEngine(String path) {
        // 读取当前服务端语言配置，用于拼出 scripts-语言 目录名，例如 scripts-zh-CN。
        ServiceProperty serviceProperty = ServerManager.getApplicationContext().getBean(ServiceProperty.class);
        ServerResourceResolver resolver = getResolver();

        // 1. 优先加载默认脚本（文件系统）
        Path scriptPath = resolver.resolveDataPath(SCRIPT_DIRECTORY, path);
        ScriptEngine engine = null;
        if (Files.exists(scriptPath)) {
            engine = evalScriptFromFile(scriptPath, path);
        }

        // 2. 文件系统找不到时，尝试从 JAR 内置 classpath 加载默认脚本
        if (engine == null) {
            engine = evalScriptFromClasspath(SCRIPT_DIRECTORY + "/" + path, path);
        }

        // 3. 最后用语言目录脚本覆盖（只放已本地化的文件，不要求复制完整 scripts）
        Path scriptLangPath = resolver.resolveDataPath(SCRIPT_DIRECTORY + "-" + serviceProperty.getLanguage(), path);
        if (Files.exists(scriptLangPath)) {
            engine = evalScriptFromFile(scriptLangPath, path);
        }

        return engine;
    }

    protected ScriptEngine getInvocableScriptEngine(String path, Client c) {
        // 缓存键统一使用默认脚本前缀加相对路径，避免读取和写入缓存时使用不同 key。
        String scriptKey = SCRIPT_DIRECTORY + "/" + path;
        ScriptEngine engine = c.getScriptEngine(scriptKey);
        if (engine == null) {
            // 客户端当前没有缓存时，再按文件级 i18n 规则加载脚本。
            engine = getInvocableScriptEngine(path);
            c.setScriptEngine(scriptKey, engine);
        }

        return engine;
    }

    /**
     * 允许脚本通过 Java.type() 查找并调用服务端 Java 类。
     */
    private void enableScriptHostAccess(GraalJSScriptEngine engine) {
        // GraalJS 的 host 访问开关需要写入引擎作用域绑定。
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
    }

    // ── Script I/O ──────────────────────────────────────────────────────

    /** Evaluate a script from a filesystem path. Returns null on failure. */
    private ScriptEngine evalScriptFromFile(Path filePath, String logPath) {
        ScriptEngine engine = createEngine();
        if (engine == null) return null;
        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            engine.eval(br);
        } catch (final ScriptException | IOException t) {
            log.warn(I18nUtil.getLogMessage("AbstractScriptManager.getInvocableScriptEngine.warn1"), logPath, t);
            return null;
        }
        return engine;
    }

    /** Evaluate a script from the classpath (JAR built-in). Returns null if not found. */
    private ScriptEngine evalScriptFromClasspath(String classpathResource, String logPath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(classpathResource);
        if (is == null) {
            return null;
        }
        ScriptEngine engine = createEngine();
        if (engine == null) return null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            engine.eval(br);
        } catch (final ScriptException | IOException t) {
            log.warn(I18nUtil.getLogMessage("AbstractScriptManager.getInvocableScriptEngine.warn1"), logPath, t);
            return null;
        }
        return engine;
    }

    /** Create and configure a {@link GraalJSScriptEngine}. */
    private ScriptEngine createEngine() {
        ScriptEngine engine = sef.getScriptEngine();
        if (!(engine instanceof GraalJSScriptEngine graalScriptEngine)) {
            throw new IllegalStateException(I18nUtil.getExceptionMessage("AbstractScriptManager.getInvocableScriptEngine.exception1"));
        }
        enableScriptHostAccess(graalScriptEngine);
        return graalScriptEngine;
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private static ServerResourceResolver getResolver() {
        return ServerManager.getApplicationContext().getBean(ServerResourceResolver.class);
    }

    protected void resetContext(String path, Client c) {
        // 重置时使用同一个缓存 key，确保能清掉上面 setScriptEngine 写入的脚本引擎。
        c.removeScriptEngine(SCRIPT_DIRECTORY + "/" + path);
    }
}
