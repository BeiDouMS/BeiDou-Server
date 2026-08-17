package soloMapling.server;

import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;

public class SoloMaplingConstants {

    public static final Channel mainChannel = Server.getInstance().getChannel(0, 1);

    public static class GameConstants {
        public static final int WORLD_SCANIA = 0;
        public static final int CHANNEL_1 = 1;
        public static final int BOT_BASE_ID = 20000;
    }

}
