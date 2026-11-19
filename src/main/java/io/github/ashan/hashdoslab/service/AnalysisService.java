package io.github.ashan.hashdoslab.service;

import io.github.ashan.hashdoslab.analysis.CollisionAnalyzer;
import io.github.ashan.hashdoslab.analysis.ThreeEngineComparisonAnalyzer;
import io.github.ashan.hashdoslab.api.dto.response.AnalysisResponse;
import io.github.ashan.hashdoslab.api.dto.response.ThreeEngineComparisonResponse;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private final CollisionAnalyzer collisionAnalyzer;
    private final ThreeEngineComparisonAnalyzer threeEngineComparisonAnalyzer;

    public AnalysisService(
            CollisionAnalyzer collisionAnalyzer,
            ThreeEngineComparisonAnalyzer threeEngineComparisonAnalyzer
    ) {
        this.collisionAnalyzer = collisionAnalyzer;
        this.threeEngineComparisonAnalyzer = threeEngineComparisonAnalyzer;
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


    public ThreeEngineComparisonResponse compareEngines(int keyCount) {

        var result = threeEngineComparisonAnalyzer.compare(keyCount);

        return new ThreeEngineComparisonResponse(
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