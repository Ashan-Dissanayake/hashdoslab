package io.github.ashan.hashdoslab.api.dto.response;

public record ThreeEngineComparisonResponse(

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