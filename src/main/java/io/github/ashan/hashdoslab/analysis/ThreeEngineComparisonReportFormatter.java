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

                INSERTION TIME
                ------------------------------------------
                Vulnerable HashTable : %,d ns
                Java HashMap         : %,d ns
                Hardened HashTable   : %,d ns

                VULNERABLE ENGINE ANALYSIS
                ------------------------------------------
                Traversal Count     : %,d

                ENTRY COUNT VERIFICATION
                ------------------------------------------
                Vulnerable HashTable : %d
                Java HashMap         : %d
                Hardened HashTable   : %d

                ==========================================
                """,

                result.keyCount(),

                result.vulnerableInsertionTimeNanos(),
                result.javaHashMapInsertionTimeNanos(),
                result.hardenedInsertionTimeNanos(),

                result.vulnerableTraversalCount(),

                result.vulnerableEntryCount(),
                result.javaHashMapEntryCount(),
                result.hardenedEntryCount()
        );
    }
}