package org.gms.server.life;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonsterInformationProviderDropSourceTest {

    @Test
    void dropSourceLookupIsCachedBoundedAndReadOnly() {
        AtomicInteger loads = new AtomicInteger();
        MonsterInformationProvider provider = new MonsterInformationProvider(false) {
            @Override
            protected List<DropSource> loadDropSources(int itemId) {
                loads.incrementAndGet();
                List<DropSource> sources = new ArrayList<>();
                for (int i = 0; i < MAX_DROP_SOURCE_RESULTS; i++) {
                    sources.add(new DropSource(100_000 + i, 1_000 - i));
                }
                return List.copyOf(sources);
            }
        };

        assertEquals(3, provider.retrieveDropSources(2000000, 3).size());
        assertEquals(MonsterInformationProvider.MAX_DROP_SOURCE_RESULTS,
                provider.retrieveDropSources(2000000, Integer.MAX_VALUE).size());
        assertEquals(1, loads.get());
        assertThrows(UnsupportedOperationException.class,
                () -> provider.retrieveDropSources(2000000, 3)
                        .add(new MonsterInformationProvider.DropSource(1, 1)));
    }

    @Test
    void invalidLookupDoesNotLoad() {
        AtomicInteger loads = new AtomicInteger();
        MonsterInformationProvider provider = new MonsterInformationProvider(false) {
            @Override
            protected List<DropSource> loadDropSources(int itemId) {
                loads.incrementAndGet();
                return List.of();
            }
        };

        assertEquals(List.of(), provider.retrieveDropSources(0, 10));
        assertEquals(List.of(), provider.retrieveDropSources(2000000, 0));
        assertEquals(0, loads.get());
    }
}
