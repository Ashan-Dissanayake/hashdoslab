
package io.github.ashan.hashdoslab.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HardenedHashTableTest {

    private HardenedHashTable hashTable;

    @BeforeEach
    void setUp() {
        hashTable = new HardenedHashTable();
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

    @Test
    void shouldRejectNullKey() {
        assertThrows(
                IllegalArgumentException.class,
                () -> hashTable.put(null, "value")
        );
    }

    @Test
    void shouldRejectNullValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> hashTable.put("key", null)
        );
    }

    @Test
    void shouldRejectNullKeyDuringRetrieval() {
        assertThrows(
                IllegalArgumentException.class,
                () -> hashTable.get(null)
        );
    }

    @Test
    void shouldHandleCollidingKeys() {
        String key1 = "Aa";
        String key2 = "BB";

        assertEquals(key1.hashCode(), key2.hashCode());

        hashTable.put(key1, "value-1");
        hashTable.put(key2, "value-2");

        assertEquals("value-1", hashTable.get(key1));
        assertEquals("value-2", hashTable.get(key2));
        assertEquals(2, hashTable.size());
    }

    @Test
    void shouldRejectEntriesBeyondMaximumCapacity() {

        for (int i = 0; i < 10_000; i++) {
            hashTable.put("key-" + i, "value-" + i);
        }

        assertEquals(10_000, hashTable.size());

        assertThrows(
                IllegalStateException.class,
                () -> hashTable.put("extra-key", "extra-value")
        );
    }
}
