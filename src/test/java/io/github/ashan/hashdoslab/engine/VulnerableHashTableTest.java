
package io.github.ashan.hashdoslab.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class VulnerableHashTableTest {

    private VulnerableHashTable hashTable;

    @BeforeEach
    void setUp() {
        hashTable = new VulnerableHashTable(16);
    }

    @Test
    void shouldInsertAndRetrieveValue() {

        hashTable.put("apple", "100");

        assertThat(hashTable.get("apple"))
                .isEqualTo("100");
    }

    @Test
    void shouldReturnNullWhenKeyDoesNotExist() {

        assertThat(hashTable.get("unknown"))
                .isNull();
    }

    @Test
    void shouldHandleHashCollisions() {

        // These two strings have identical hash codes.
        assertThat("Aa".hashCode())
                .isEqualTo("BB".hashCode());

        hashTable.put("Aa", "value-A");
        hashTable.put("BB", "value-B");

        assertThat(hashTable.get("Aa"))
                .isEqualTo("value-A");

        assertThat(hashTable.get("BB"))
                .isEqualTo("value-B");

        assertThat(hashTable.size())
                .isEqualTo(2);
    }

    @Test
    void shouldUpdateValueWhenDuplicateKeyIsInserted() {

        hashTable.put("apple", "100");
        hashTable.put("apple", "200");

        assertThat(hashTable.get("apple"))
                .isEqualTo("200");

        assertThat(hashTable.size())
                .isEqualTo(1);
    }

    @Test
    void shouldTrackNumberOfEntries() {

        hashTable.put("apple", "100");
        hashTable.put("banana", "200");
        hashTable.put("orange", "300");

        assertThat(hashTable.size())
                .isEqualTo(3);
    }

    @Test
    void shouldRejectZeroCapacity() {

        assertThatThrownBy(
                () -> new VulnerableHashTable(0)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeCapacity() {

        assertThatThrownBy(
                () -> new VulnerableHashTable(-1)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullKey() {

        assertThatNullPointerException()
                .isThrownBy(
                        () -> hashTable.put(null, "value")
                );
    }

    @Test
    void shouldRejectNullValue() {

        assertThatNullPointerException()
                .isThrownBy(
                        () -> hashTable.put("key", null)
                );
    }

    @Test
    void shouldRejectNullKeyWhenRetrieving() {

        assertThatNullPointerException()
                .isThrownBy(
                        () -> hashTable.get(null)
                );
    }
}
