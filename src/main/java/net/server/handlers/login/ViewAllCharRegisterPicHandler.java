package net.server.handlers.login;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.coordinator.session.Hwid;
import net.server.coordinator.session.MapleSessionCoordinator;
import net.server.coordinator.session.MapleSessionCoordinator.AntiMulticlientResult;
import net.server.world.World;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ViewAllCharRegisterPicHandler extends AbstractMaplePacketHandler {

    private static int parseAntiMulticlientError(AntiMulticlientResult res) {
        switch (res) {
            case REMOTE_PROCESSING:
                return 10;

            case REMOTE_LOGGEDIN:
                return 7;

            case REMOTE_NO_MATCH:
                return 17;
                
            case COORDINATOR_ERROR:
                return 8;
                
            default:
                return 9;
        }
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        int charId = slea.readInt();
        slea.readInt(); // please don't let the client choose which world they should login
        
        String mac = slea.readMapleAsciiString();
        String hwid = slea.readMapleAsciiString();
        
        if (!Hwid.isValidRawHwid(hwid)) {
            c.announce(MaplePacketCreator.getAfterLoginError(17));
            return;
        }
        
        c.updateMacs(mac);
        c.updateHwid(hwid);
        
        if (c.hasBannedMac() || c.hasBannedHWID()) {
            MapleSessionCoordinator.getInstance().closeSession(c, true);
            return;
        }

        AntiMulticlientResult res = MapleSessionCoordinator.getInstance().attemptGameSession(c, c.getAccID(), hwid);
        if (res != AntiMulticlientResult.SUCCESS) {
            c.announce(MaplePacketCreator.getAfterLoginError(parseAntiMulticlientError(res)));
            return;
        }
        
        Server server = Server.getInstance();
        if(!server.haveCharacterEntry(c.getAccID(), charId)) {
            MapleSessionCoordinator.getInstance().closeSession(c, true);
            return;
        }
        
        c.setWorld(server.getCharacterWorld(charId));
        World wserv = c.getWorldServer();
        if(wserv == null || wserv.isWorldCapacityFull()) {
            c.announce(MaplePacketCreator.getAfterLoginError(10));
            return;
        }
        
        int channel = Randomizer.rand(1, server.getWorld(c.getWorld()).getChannelsSize());
        c.setChannel(channel);
        
        String pic = slea.readMapleAsciiString();
        c.setPic(pic);
        
        String[] socket = server.getInetSocket(c, c.getWorld(), channel);
        if (socket == null) {
            c.announce(MaplePacketCreator.getAfterLoginError(10));
            return;
        }
        
        server.unregisterLoginState(c);
        c.setCharacterOnSessionTransitionState(charId);
        
        try {
            c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
