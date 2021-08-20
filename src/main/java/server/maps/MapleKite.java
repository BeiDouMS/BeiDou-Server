package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import net.packet.Packet;
import tools.PacketCreator;

import java.awt.*;

public class MapleKite extends AbstractMapleMapObject {
    private final Point pos;
    private final MapleCharacter owner;
    private final String text;
    private final int ft;
    private final int itemid;

    public MapleKite(MapleCharacter owner, String text, int itemId) {
        this.owner = owner;
        this.pos = owner.getPosition();
        this.ft = owner.getFh();
        this.text = text;
        this.itemid = itemId;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.KITE;
    }

    @Override
    public Point getPosition() {
        return pos.getLocation();
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.sendPacket(makeDestroyData());
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.sendPacket(makeSpawnData());
    }

    public final Packet makeSpawnData() {
        return PacketCreator.spawnKite(getObjectId(), itemid, owner.getName(), text, pos, ft);
    }

    public final Packet makeDestroyData() {
        return PacketCreator.removeKite(getObjectId(), 0);
    }
}