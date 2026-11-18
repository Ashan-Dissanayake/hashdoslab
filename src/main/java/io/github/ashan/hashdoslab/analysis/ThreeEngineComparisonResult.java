package io.github.ashan.hashdoslab.analysis;

public record ThreeEngineComparisonResult(

        int keyCount,

        long vulnerableInsertionTimeNanos,
        long javaHashMapInsertionTimeNanos,
        long hardenedInsertionTimeNanos,

        long vulnerableTraversalCount,

        int vulnerableEntryCount,
        int javaHashMapEntryCount,
        int hardenedEntryCount

) {
}