
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

        // Benchmark configuration
        assertEquals(5, result.warmupIterations());
        assertEquals(15, result.measurementIterations());

        // Entry count verification
        assertEquals(keyCount, result.vulnerableEntryCount());
        assertEquals(keyCount, result.javaHashMapEntryCount());
        assertEquals(keyCount, result.hardenedEntryCount());

        // Vulnerable traversal count
        assertEquals(4950, result.vulnerableTraversalCount());

        // Vulnerable timing
        assertTrue(result.vulnerableAverageInsertionTimeNanos() >= 0);
        assertTrue(result.vulnerableMedianInsertionTimeNanos() >= 0);

        // Java HashMap timing
        assertTrue(result.javaHashMapAverageInsertionTimeNanos() >= 0);
        assertTrue(result.javaHashMapMedianInsertionTimeNanos() >= 0);

        // Hardened timing
        assertTrue(result.hardenedAverageInsertionTimeNanos() >= 0);
        assertTrue(result.hardenedMedianInsertionTimeNanos() >= 0);
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
