
package io.github.ashan.hashdoslab.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaHashMapAdapterTest {

    private JavaHashMapAdapter hashTable;

    @BeforeEach
    void setUp() {
        hashTable = new JavaHashMapAdapter();
    }

    @Test
    void shouldInsertAndRetrieveValue() {
        hashTable.put("key-1", "value-1");

        assertEquals("value-1", hashTable.get("key-1"));
    }

    @Test
    void shouldReturnNullForNonExistingKey() {
        assertNull(hashTable.get("missing-key"));
    }

    @Test
    void shouldTrackNumberOfEntries() {
        hashTable.put("key-1", "value-1");
        hashTable.put("key-2", "value-2");

        assertEquals(2, hashTable.size());
    }

    @Test
    void shouldUpdateValueForDuplicateKey() {
        hashTable.put("key-1", "old-value");
        hashTable.put("key-1", "new-value");

        assertEquals("new-value", hashTable.get("key-1"));
        assertEquals(1, hashTable.size());
    }
}
