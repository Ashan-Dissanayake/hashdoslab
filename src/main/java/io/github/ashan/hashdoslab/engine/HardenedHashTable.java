
package io.github.ashan.hashdoslab.engine;

import java.util.TreeMap;

public class HardenedHashTable implements HashTable {

    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAX_ENTRIES = 10_000;

    private final TreeMap<String, String>[] buckets;

    private int size;

    @SuppressWarnings("unchecked")
    public HardenedHashTable() {
        this.buckets = new TreeMap[DEFAULT_CAPACITY];

        for (int i = 0; i < DEFAULT_CAPACITY; i++) {
            buckets[i] = new TreeMap<>();
        }

        this.size = 0;
    }


    @Override
    public void put(String key, String value) {

        if (key == null || value == null) {
            throw new IllegalArgumentException(
                    "Key and value cannot be null"
            );
        }

        int index = Math.floorMod(
                key.hashCode(),
                buckets.length
        );

        TreeMap<String, String> bucket = buckets[index];

        // Update existing key without increasing size
        if (bucket.containsKey(key)) {
            bucket.put(key, value);
            return;
        }

        // Enforce maximum entry limit
        if (size >= MAX_ENTRIES) {
            throw new IllegalStateException(
                    "Maximum number of entries exceeded"
            );
        }

        bucket.put(key, value);
        size++;
    }


    @Override
    public String get(String key) {

        if (key == null) {
            throw new IllegalArgumentException(
                    "Key cannot be null"
            );
        }

        int index = Math.floorMod(
                key.hashCode(),
                buckets.length
        );

        TreeMap<String, String> bucket = buckets[index];

        return bucket.get(key);
    }

    @Override
    public int size() {
        return size;
    }
}
