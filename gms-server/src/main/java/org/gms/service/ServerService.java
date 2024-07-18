package org.gms.service;

import org.gms.model.dto.ChannelListRtnDTO;
import org.gms.model.dto.WorldListRtnDTO;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.gms.net.server.world.World;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerService {

    public List<WorldListRtnDTO> worldList() {
        List<World> worlds = Server.getInstance().getWorlds();
        return worlds.stream()
                .map(w -> WorldListRtnDTO.builder()
                        .id(w.getId())
                        .expRate(w.getExpRate())
                        .dropRate(w.getDropRate())
                        .mesoRate(w.getMesoRate())
                        .bossDropRate(w.getBossDropRate())
                        .questRate(w.getQuestRate())
                        .travelRate(w.getTravelRate())
                        .fishingRate(w.getFishingRate())
                        .build())
                .toList();
    }

    public List<ChannelListRtnDTO> channelList(int worldId) {
        List<Channel> channels = Server.getInstance().getWorld(worldId).getChannels();
        return channels.stream()
                .map(c -> ChannelListRtnDTO.builder().id(c.getId()).worldId(c.getWorld()).build())
                .toList();
    }
}
