package io.github.ashan.hashdoslab.service;

import io.github.ashan.hashdoslab.analysis.CollisionAnalyzer;
import io.github.ashan.hashdoslab.api.dto.response.AnalysisResponse;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private final CollisionAnalyzer collisionAnalyzer;

    public AnalysisService(CollisionAnalyzer collisionAnalyzer) {
        this.collisionAnalyzer = collisionAnalyzer;
    }

    public AnalysisResponse compare(int keyCount) {

        CollisionAnalyzer.ComparisonResult result =
                collisionAnalyzer.compareInputs(keyCount);

        return new AnalysisResponse(
                keyCount,
                new AnalysisResponse.InputMetrics(
                        result.normalResult().traversalCount()
                ),
                new AnalysisResponse.InputMetrics(
                        result.collisionResult().traversalCount()
                ),
                result.traversalDifference(),
                result.traversalRatio()
        );
    }
}