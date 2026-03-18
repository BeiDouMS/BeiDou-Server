package org.gms.net.server.task;

import org.gms.client.Character;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;

public class OnlineTimeTask implements Runnable {
    @Override
    public void run() {
        if (!Server.getInstance().isOnline()) {
            return;
        }
        for (final Channel chan : Server.getInstance().getAllChannels()) {
            if (chan == null || chan.getPlayerStorage() == null) {
                continue;
            }
            for (final Character chr : chan.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                // 在线时长的初始化、跨日归零和递增都统一收口到角色对象内部，避免任务层重复散落判断。
                chr.tickOnlineTime();
            }
        }
    }
}
