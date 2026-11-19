package io.github.ashan.hashdoslab.api.dto.response;

public record ThreeEngineComparisonResponse(

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