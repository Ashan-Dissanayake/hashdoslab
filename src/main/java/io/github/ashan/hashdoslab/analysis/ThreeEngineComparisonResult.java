package io.github.ashan.hashdoslab.analysis;

public record ThreeEngineComparisonResult(

        int keyCount,

        int warmupIterations,
        int measurementIterations,

        long vulnerableAverageInsertionTimeNanos,
        long vulnerableMedianInsertionTimeNanos,

        long javaHashMapAverageInsertionTimeNanos,
        long javaHashMapMedianInsertionTimeNanos,

        long hardenedAverageInsertionTimeNanos,
        long hardenedMedianInsertionTimeNanos,

        long vulnerableTraversalCount,

        int vulnerableEntryCount,
        int javaHashMapEntryCount,
        int hardenedEntryCount

) {
}