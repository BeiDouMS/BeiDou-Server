package org.gms.extension.api;

import java.util.List;

/** Read-only, bounded lookup of monsters that have a real drop_data row for an item. */
public interface HostMonsterDrops {

    List<DropSource> findSources(int itemId, int limit);

    record DropSource(int dropperId, int chance) {
    }
}
