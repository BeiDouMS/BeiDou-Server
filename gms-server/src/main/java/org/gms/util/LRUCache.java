package org.gms.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache() {
        this(1024);
    }

    public LRUCache(int capacity) {
        // 设置最大容量，扩容因子，排序规则
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    public boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // 当Map中数据量大于指定缓存个数的时候，返回true，自动删除最老的数据
        return size() > capacity;
    }
}
