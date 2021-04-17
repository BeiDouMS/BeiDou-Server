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
package scripting;

import client.MapleClient;
import tools.FilePrinter;

import javax.script.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Matze
 */
public abstract class AbstractScriptManager {
    private final ScriptEngineFactory sef;

    protected AbstractScriptManager() {
        sef = new ScriptEngineManager().getEngineByName("graal.js").getFactory();
    }

    protected ScriptEngine getScriptEngine(String path) {
        path = "scripts/" + path;
        File scriptFile = new File(path);
        if (!scriptFile.exists()) {
            return null;
        }

        ScriptEngine engine = sef.getScriptEngine();
        applyLooserGraalSecurity(engine);

        try (FileReader fr = new FileReader(scriptFile)) {
            engine.eval(fr);
        } catch (final ScriptException | IOException t) {
            FilePrinter.printError(FilePrinter.INVOCABLE + path.substring(12), t, path);
            return null;
        }

        return engine;
    }

    protected ScriptEngine getScriptEngine(String path, MapleClient c) {
        ScriptEngine engine = c.getScriptEngine("scripts/" + path);
        if (engine == null) {
            engine = getScriptEngine(path);
            c.setScriptEngine(path, engine);
        }

        return engine;
    }

    private void applyLooserGraalSecurity(ScriptEngine scriptEngine) {
        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
    }

    protected void resetContext(String path, MapleClient c) {
        c.removeScriptEngine("scripts/" + path);
    }
}
