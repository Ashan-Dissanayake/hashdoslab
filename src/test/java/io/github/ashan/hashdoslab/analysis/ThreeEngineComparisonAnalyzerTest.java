package io.github.ashan.hashdoslab.analysis;

import io.github.ashan.hashdoslab.payload.CollisionKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreeEngineComparisonAnalyzerTest {

    private ThreeEngineComparisonAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new ThreeEngineComparisonAnalyzer(
                new CollisionKeyGenerator()
        );
    }

    @Test
    void shouldCompareAllThreeImplementations() {

        int keyCount = 100;

        ThreeEngineComparisonResult result =
                analyzer.compare(keyCount);

        assertEquals(keyCount, result.keyCount());

        assertEquals(100, result.vulnerableEntryCount());
        assertEquals(100, result.javaHashMapEntryCount());
        assertEquals(100, result.hardenedEntryCount());

        assertEquals(4950, result.vulnerableTraversalCount());

        assertTrue(result.vulnerableInsertionTimeNanos() >= 0);
        assertTrue(result.javaHashMapInsertionTimeNanos() >= 0);
        assertTrue(result.hardenedInsertionTimeNanos() >= 0);
    }

    @Test
    void shouldRejectZeroKeyCount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.compare(0)
        );
    }

    @Test
    void shouldRejectNegativeKeyCount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.compare(-1)
        );
    }

    @Test
    void shouldRejectKeyCountAboveMaximumLimit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.compare(10_001)
        );
    }
}