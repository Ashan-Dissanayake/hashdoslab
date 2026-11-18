
package io.github.ashan.hashdoslab.engine;

public interface HashTable {

    void put(String key, String value);

    String get(String key);

    int size();
}
