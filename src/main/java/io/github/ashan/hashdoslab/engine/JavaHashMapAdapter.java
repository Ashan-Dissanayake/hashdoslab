
package io.github.ashan.hashdoslab.engine;

import java.util.HashMap;
import java.util.Map;

public class JavaHashMapAdapter implements HashTable {

    private final Map<String, String> map;

    public JavaHashMapAdapter() {
        this.map = new HashMap<>();
    }

    @Override
    public void put(String key, String value) {
        map.put(key, value);
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }
}
