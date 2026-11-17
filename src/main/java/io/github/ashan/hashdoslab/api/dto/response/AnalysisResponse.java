package io.github.ashan.hashdoslab.api.dto.response;

public record AnalysisResponse(
        int keyCount,
        InputMetrics normal,
        InputMetrics collision,
        long traversalDifference,
        Double traversalRatio
) {

    public record InputMetrics(
            long traversalCount
    ) {
    }
}