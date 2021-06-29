/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
package net.server.coordinator.session;

import client.MapleCharacter;
import client.MapleClient;
import config.YamlConfig;
import net.server.Server;
import net.server.coordinator.login.LoginStorage;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 *
 * @author Ronan
 */
public class MapleSessionCoordinator {
    private static final Logger log = LoggerFactory.getLogger(MapleSessionCoordinator.class);
    private static final MapleSessionCoordinator instance = new MapleSessionCoordinator();
    
    public static MapleSessionCoordinator getInstance() {
        return instance;
    }
    
    public enum AntiMulticlientResult {
        SUCCESS,
        REMOTE_LOGGEDIN,
        REMOTE_REACHED_LIMIT,
        REMOTE_PROCESSING,
        REMOTE_NO_MATCH,
        MANY_ACCOUNT_ATTEMPTS,
        COORDINATOR_ERROR
    }

    private final SessionInitialization sessionInit = new SessionInitialization();
    private final LoginStorage loginStorage = new LoginStorage();
    private final Map<Integer, MapleClient> onlineClients = new HashMap<>(); // Key: account id
    private final Set<String> onlineRemoteHwids = new HashSet<>(); // Hwid/nibblehwid
    private final Map<String, Set<IoSession>> loginRemoteHosts = new HashMap<>(); // Key: Ip (+ nibblehwid)
    private final HostHwidCache hostHwidCache = new HostHwidCache();
    
    private MapleSessionCoordinator() {
    }

    private static boolean attemptAccountAccess(int accountId, String nibbleHwid, boolean routineCheck) {
        try (Connection con = DatabaseConnection.getConnection()) {
            List<HwidRelevance> hwidRelevances = SessionDAO.getHwidRelevance(con, accountId);
            for (HwidRelevance hwidRelevance : hwidRelevances) {
                if (hwidRelevance.hwid().endsWith(nibbleHwid)) {
                    if (!routineCheck) {
                        // better update HWID relevance as soon as the login is authenticated
                        Instant expiry = HwidAssociationExpiry.getHwidAccountExpiry(hwidRelevance.relevance());
                        SessionDAO.updateAccountAccess(con, nibbleHwid, accountId, expiry, hwidRelevance.getIncrementedRelevance());
                    }

                    return true;
                }
            }

            if (hwidRelevances.size() < YamlConfig.config.server.MAX_ALLOWED_ACCOUNT_HWID) {
                return true;
            }
        } catch (SQLException e) {
            log.warn("Failed to update account access. Account id: {}, nibbleHwid: {}", accountId, nibbleHwid, e);
        }

        return false;
    }
    
    public static String getSessionRemoteAddress(IoSession session) {
        return (String) session.getAttribute(MapleClient.CLIENT_REMOTE_ADDRESS);
    }
    
    public static String getSessionRemoteHost(IoSession session) {
        String nibbleHwid = (String) session.getAttribute(MapleClient.CLIENT_NIBBLEHWID);
        
        if (nibbleHwid != null) {
            return getSessionRemoteAddress(session) + "-" + nibbleHwid;
        } else {
            return getSessionRemoteAddress(session);
        }
    }

    private static MapleClient getSessionClient(IoSession session) {
        return (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
    }

    /**
     * Overwrites any existing online client for the account id, making sure to disconnect it as well.
     */
    public void updateOnlineClient(IoSession session) {
        MapleClient client = getSessionClient(session);

        if (client != null) {
            int accountId = client.getAccID();
            disconnectClientIfOnline(accountId);
            onlineClients.put(accountId, client);
        }
    }

    private void disconnectClientIfOnline(int accountId) {
        MapleClient ingameClient = onlineClients.get(accountId);
        if (ingameClient != null) {     // thanks MedicOP for finding out a loss of loggedin account uniqueness when using the CMS "Unstuck" feature
            ingameClient.forceDisconnect();
        }
    }

    public boolean canStartLoginSession(IoSession session) {
        if (!YamlConfig.config.server.DETERRED_MULTICLIENT) {
            return true;
        }

        String remoteHost = getSessionRemoteHost(session);
        final InitializationResult initResult = sessionInit.initialize(remoteHost);
        switch (initResult.getAntiMulticlientResult()) {
            case REMOTE_PROCESSING -> {
                return false;
            }
            case COORDINATOR_ERROR -> {
                return true;
            }
        }

        try {
            final HostHwid knownHwid = hostHwidCache.getEntry(remoteHost);
            if (knownHwid != null && onlineRemoteHwids.contains(knownHwid.hwid())) {
                return false;
            } else if  (loginRemoteHosts.containsKey(remoteHost)) {
                return false;
            }

            addRemoteHostSession(remoteHost, session);
            return true;
        } finally {
            sessionInit.finalize(remoteHost);
        }
    }

    private void addRemoteHostSession(String remoteHost, IoSession session) {
        Set<IoSession> sessions = new HashSet<>(2);
        sessions.add(session);
        loginRemoteHosts.put(remoteHost, sessions);
    }

    public void closeLoginSession(IoSession session) {
        String remoteHost = getSessionRemoteHost(session);
        removeRemoteHostSession(remoteHost, session);

        String nibbleHwid = (String) session.removeAttribute(MapleClient.CLIENT_NIBBLEHWID);
        if (nibbleHwid != null) {
            onlineRemoteHwids.remove(nibbleHwid);

            MapleClient client = getSessionClient(session);
            if (client != null) {
                MapleClient loggedClient = onlineClients.get(client.getAccID());

                // do not remove an online game session here, only login session
                if (loggedClient != null && loggedClient.getSessionId() == client.getSessionId()) {
                    onlineClients.remove(client.getAccID());
                }
            }
        }
    }

    private void removeRemoteHostSession(String remoteHost, IoSession session) {
        Set<IoSession> sessions = loginRemoteHosts.get(remoteHost);
        if (sessions != null) {
            sessions.remove(session);

            if (sessions.isEmpty()) {
                loginRemoteHosts.remove(remoteHost);
            }
        }
    }

    public AntiMulticlientResult attemptLoginSession(IoSession session, String nibbleHwid, int accountId, boolean routineCheck) {
        if (!YamlConfig.config.server.DETERRED_MULTICLIENT) {
            session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, nibbleHwid);
            return AntiMulticlientResult.SUCCESS;
        }

        String remoteHost = getSessionRemoteHost(session);
        InitializationResult initResult = sessionInit.initialize(remoteHost);
        if (initResult != InitializationResult.SUCCESS) {
            return initResult.getAntiMulticlientResult();
        }

        try {
            if (!loginStorage.registerLogin(accountId)) {
                return AntiMulticlientResult.MANY_ACCOUNT_ATTEMPTS;
            } else if (routineCheck && !attemptAccountAccess(accountId, nibbleHwid, routineCheck)) {
                return AntiMulticlientResult.REMOTE_REACHED_LIMIT;
            } else if (onlineRemoteHwids.contains(nibbleHwid)) {
                return AntiMulticlientResult.REMOTE_LOGGEDIN;
            } else if (!attemptAccountAccess(accountId, nibbleHwid, routineCheck)) {
                return AntiMulticlientResult.REMOTE_REACHED_LIMIT;
            }

            session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, nibbleHwid);
            onlineRemoteHwids.add(nibbleHwid);

            return AntiMulticlientResult.SUCCESS;
        } finally {
            sessionInit.finalize(remoteHost);
        }
    }

    public AntiMulticlientResult attemptGameSession(IoSession session, int accountId, String remoteHwid) {
        final String remoteHost = getSessionRemoteHost(session);
        if (!YamlConfig.config.server.DETERRED_MULTICLIENT) {
            hostHwidCache.addEntry(remoteHost, remoteHwid);
            hostHwidCache.addEntry(getSessionRemoteAddress(session), remoteHwid); // no HWID information on the loggedin newcomer session...
            return AntiMulticlientResult.SUCCESS;
        }

        final InitializationResult initResult = sessionInit.initialize(remoteHost);
        if (initResult != InitializationResult.SUCCESS) {
            return initResult.getAntiMulticlientResult();
        }
        
        try {
            String nibbleHwid = (String) session.getAttribute(MapleClient.CLIENT_NIBBLEHWID);   // thanks Paxum for noticing account stuck after PIC failure
            if (nibbleHwid == null) {
                return AntiMulticlientResult.REMOTE_NO_MATCH;
            }

            onlineRemoteHwids.remove(nibbleHwid);

            if (!remoteHwid.endsWith(nibbleHwid)) {
                return AntiMulticlientResult.REMOTE_NO_MATCH;
            } else if (onlineRemoteHwids.contains(remoteHwid)) {
                return AntiMulticlientResult.REMOTE_LOGGEDIN;
            }

            // assumption: after a SUCCESSFUL login attempt, the incoming client WILL receive a new IoSession from the game server

            // updated session CLIENT_HWID attribute will be set when the player log in the game
            onlineRemoteHwids.add(remoteHwid);
            hostHwidCache.addEntry(remoteHost, remoteHwid);
            hostHwidCache.addEntry(getSessionRemoteAddress(session), remoteHwid);
            associateHwidAccountIfAbsent(remoteHwid, accountId);

            return AntiMulticlientResult.SUCCESS;
        } finally {
            sessionInit.finalize(remoteHost);
        }
    }

    private static void associateHwidAccountIfAbsent(String remoteHwid, int accountId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            List<String> hwids = SessionDAO.getHwidsForAccount(con, accountId);

            boolean containsRemoteHwid = hwids.stream().anyMatch(hwid -> hwid.contentEquals(remoteHwid));
            if (containsRemoteHwid) {
                return;
            }

            if (hwids.size() < YamlConfig.config.server.MAX_ALLOWED_ACCOUNT_HWID) {
                Instant expiry = HwidAssociationExpiry.getHwidAccountExpiry(0);
                SessionDAO.registerAccountAccess(con, accountId, remoteHwid, expiry);
            }
        } catch (SQLException ex) {
            log.warn("Failed to associate hwid {} with account id {}", remoteHwid, accountId, ex);
        }
    }
    
    private static MapleClient fetchInTransitionSessionClient(IoSession session) {
        String remoteHwid = MapleSessionCoordinator.getInstance().getGameSessionHwid(session);

        if (remoteHwid == null) {   // maybe this session was currently in-transition?
            return null;
        }

        int hwidLen = remoteHwid.length();
        if (hwidLen <= 8) {
            session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, remoteHwid);
        } else {
            session.setAttribute(MapleClient.CLIENT_HWID, remoteHwid);
            session.setAttribute(MapleClient.CLIENT_NIBBLEHWID, remoteHwid.substring(hwidLen - 8, hwidLen));
        }

        MapleClient client = new MapleClient(null, null, session);
        Integer chrId = Server.getInstance().freeCharacteridInTransition(client);
        if (chrId != null) {
            try {
                client.setAccID(MapleCharacter.loadCharFromDB(chrId, client, false).getAccountID());
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }

        session.setAttribute(MapleClient.CLIENT_KEY, client);
        return client;
    }
    
    public void closeSession(IoSession session, Boolean immediately) {
        MapleClient client = getSessionClient(session);
        if (client == null) {
            client = fetchInTransitionSessionClient(session);
        }
        
        String hwid = (String) session.removeAttribute(MapleClient.CLIENT_NIBBLEHWID); // making sure to clean up calls to this function on login phase
        onlineRemoteHwids.remove(hwid);
        
        hwid = (String) session.removeAttribute(MapleClient.CLIENT_HWID);
        onlineRemoteHwids.remove(hwid);

        if (client != null) {
            final boolean isGameSession = hwid != null;
            if (isGameSession) {
                onlineClients.remove(client.getAccID());
            } else {
                MapleClient loggedClient = onlineClients.get(client.getAccID());
                
                // do not remove an online game session here, only login session
                if (loggedClient != null && loggedClient.getSessionId() == client.getSessionId()) {
                    onlineClients.remove(client.getAccID());
                }
            }
        }
        
        if (immediately != null) {
            session.close(immediately);
        }
    }
    
    public String pickLoginSessionHwid(IoSession session) {
        String remoteHost = getSessionRemoteAddress(session);
        // thanks BHB, resinate for noticing players from same network not being able to login
        return hostHwidCache.removeEntryAndGetItsHwid(remoteHost);
    }
    
    public String getGameSessionHwid(IoSession session) {
        String remoteHost = getSessionRemoteHost(session);
        return hostHwidCache.getEntryHwid(remoteHost);
    }
    
    public void clearExpiredHwidHistory() {
        hostHwidCache.clearExpired();
    }
    
    public void runUpdateLoginHistory() {
        loginStorage.clearExpiredAttempts();
    }
    
    public void printSessionTrace() {
        if (!onlineClients.isEmpty()) {
            List<Entry<Integer, MapleClient>> elist = new ArrayList<>(onlineClients.entrySet());
            String commaSeparatedClients = elist.stream()
                    .map(Entry::getKey)
                    .sorted(Integer::compareTo)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            
            System.out.println("Current online clients: " + commaSeparatedClients);
        }
        
        if (!onlineRemoteHwids.isEmpty()) {
            List<String> slist = new ArrayList<>(onlineRemoteHwids);
            Collections.sort(slist);
            
            System.out.println("Current online HWIDs: ");
            for (String s : slist) {
                System.out.println("  " + s);
            }
        }
        
        if (!loginRemoteHosts.isEmpty()) {
            List<Entry<String, Set<IoSession>>> elist = new ArrayList<>(loginRemoteHosts.entrySet());
            elist.sort(Entry.comparingByKey());
            
            System.out.println("Current login sessions: ");
            for (Entry<String, Set<IoSession>> e : elist) {
                System.out.println("  " + e.getKey() + ", size: " + e.getValue().size());
            }
        }
    }
    
    public void printSessionTrace(MapleClient c) {
        String str = "Opened server sessions:\r\n\r\n";
        
        if (!onlineClients.isEmpty()) {
            List<Entry<Integer, MapleClient>> elist = new ArrayList<>(onlineClients.entrySet());
            elist.sort(Entry.comparingByKey());
            
            str += ("Current online clients:\r\n");
            for (Entry<Integer, MapleClient> e : elist) {
                str += ("  " + e.getKey() + "\r\n");
            }
        }
        
        if (!onlineRemoteHwids.isEmpty()) {
            List<String> slist = new ArrayList<>(onlineRemoteHwids);
            Collections.sort(slist);
            
            str += ("Current online HWIDs:\r\n");
            for (String s : slist) {
                str += ("  " + s + "\r\n");
            }
        }
        
        if (!loginRemoteHosts.isEmpty()) {
            List<Entry<String, Set<IoSession>>> elist = new ArrayList<>(loginRemoteHosts.entrySet());
            
            elist.sort((e1, e2) -> e1.getKey().compareTo(e2.getKey()));
            
            str += ("Current login sessions:\r\n");
            for (Entry<String, Set<IoSession>> e : elist) {
                str += ("  " + e.getKey() + ", IP: " + e.getValue() + "\r\n");
            }
        }
        
        c.getAbstractPlayerInteraction().npcTalk(2140000, str);
    }
}
