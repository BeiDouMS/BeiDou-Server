package org.gms.extension.runtime;

import org.gms.extension.api.HostMonsterDrops;
import org.gms.server.life.MonsterInformationProvider;

import java.util.List;

public final class BeiDouHostMonsterDrops implements HostMonsterDrops {

    @Override
    public List<DropSource> findSources(int itemId, int limit) {
        return MonsterInformationProvider.getInstance().retrieveDropSources(itemId, limit).stream()
                .map(source -> new DropSource(source.dropperId(), source.chance()))
                .toList();
    }
}
