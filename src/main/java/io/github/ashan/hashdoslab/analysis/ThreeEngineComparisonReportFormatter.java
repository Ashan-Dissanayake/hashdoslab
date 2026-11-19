
package io.github.ashan.hashdoslab.analysis;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ThreeEngineComparisonReportFormatter {

    public String format(ThreeEngineComparisonResult result) {

        return String.format(
                Locale.ROOT,

                """
                ==========================================
                    THREE-ENGINE HASHDOS COMPARISON
                ==========================================

                Collision Keys: %d

                BENCHMARK CONFIGURATION
                ------------------------------------------
                Warm-up Iterations     : %d
                Measurement Iterations : %d

                INSERTION TIME
                ------------------------------------------
                Vulnerable HashTable
                  Average : %,d ns
                  Median  : %,d ns

                Java HashMap
                  Average : %,d ns
                  Median  : %,d ns

                Hardened HashTable
                  Average : %,d ns
                  Median  : %,d ns

                VULNERABLE ENGINE ANALYSIS
                ------------------------------------------
                Traversal Count : %,d

                ENTRY COUNT VERIFICATION
                ------------------------------------------
                Vulnerable HashTable : %d
                Java HashMap         : %d
                Hardened HashTable   : %d

                ==========================================
                """,

                result.keyCount(),

                result.warmupIterations(),
                result.measurementIterations(),

                result.vulnerableAverageInsertionTimeNanos(),
                result.vulnerableMedianInsertionTimeNanos(),

                result.javaHashMapAverageInsertionTimeNanos(),
                result.javaHashMapMedianInsertionTimeNanos(),

                result.hardenedAverageInsertionTimeNanos(),
                result.hardenedMedianInsertionTimeNanos(),

                result.vulnerableTraversalCount(),

                result.vulnerableEntryCount(),
                result.javaHashMapEntryCount(),
                result.hardenedEntryCount()
        );
    }
}
