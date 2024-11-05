package org.gms.net.server.task;

import org.gms.client.Character;
import org.gms.config.GameConfig;
import org.gms.net.server.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Shavit
 */
public class TimeoutTask extends BaseTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(TimeoutTask.class);

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        Collection<Character> chars = wserv.getPlayerStorage().getAllCharacters();
        for (Character chr : chars) {
            if (time - chr.getClient().getLastPacket() > GameConfig.getServerLong("timeout_duration")) {
                log.info("Chr {} auto-disconnected due to inactivity", chr.getName());
                chr.getClient().disconnect(true, chr.getCashShop().isOpened());
            }
        }
    }

    public TimeoutTask(World world) {
        super(world);
    }
}
