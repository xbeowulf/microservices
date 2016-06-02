package com.github.xbeowulf.microservices.twitter;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.synchronizedMap;

public class LruCache<K, V> {

    private Map<K, V> cache;

    public LruCache(int cacheSize) {

        cache = synchronizedMap(new LinkedHashMap<K, V>(cacheSize, 0.75f, true) {

            private int maxSize = cacheSize;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
                return size() > this.maxSize;
            }
        });
    }

    public V get(K key) {
        return cache.get(key);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }
}