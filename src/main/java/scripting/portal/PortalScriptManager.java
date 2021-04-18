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
package scripting.portal;

import client.MapleClient;
import scripting.AbstractScriptManager;
import server.maps.MaplePortal;
import tools.FilePrinter;

import javax.script.Invocable;
import java.util.HashMap;
import java.util.Map;

public class PortalScriptManager extends AbstractScriptManager {
    private static final PortalScriptManager instance = new PortalScriptManager();

    private final Map<String, Invocable> scripts = new HashMap<>();

    public static PortalScriptManager getInstance() {
        return instance;
    }

    private Invocable getPortalScript(String scriptName) {
        String scriptPath = "portal/" + scriptName + ".js";
        Invocable iv = scripts.get(scriptPath);
        if (iv != null) {
            return iv;
        }
        
        iv = (Invocable) getInvocableScriptEngine(scriptPath);
        if (iv == null) {
            return null;
        }
        
        scripts.put(scriptPath, iv);
        return iv;
    }

    public boolean executePortalScript(MaplePortal portal, MapleClient c) {
        try {
            Invocable iv = getPortalScript(portal.getScriptName());
            if (iv != null) {
                boolean couldWarp = (boolean) iv.invokeFunction("enter", new PortalPlayerInteraction(c, portal));
                return couldWarp;
            }
        } catch (Exception e) {
            FilePrinter.printError(FilePrinter.PORTAL + portal.getScriptName() + ".txt", e);
        }
        return false;
    }

    public void reloadPortalScripts() {
        scripts.clear();
    }
}