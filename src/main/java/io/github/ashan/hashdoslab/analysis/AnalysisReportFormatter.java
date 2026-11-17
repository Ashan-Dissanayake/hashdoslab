
package io.github.ashan.hashdoslab.analysis;

import java.util.Locale;

public class AnalysisReportFormatter {

    public String format(
            CollisionAnalyzer.ComparisonResult result
    ) {

        String normalVisits = String.valueOf(
                result.normalResult().traversalCount()
        );

        String collisionVisits = String.valueOf(
                result.collisionResult().traversalCount()
        );

        String ratio = result.traversalRatio() == null
                ? "N/A"
                : String.format(
                Locale.ROOT,
                "%.2fx",
                result.traversalRatio()
        );

        return """
                ========== HASH DOS ANALYSIS ==========

                Input Size: %d

                [NORMAL INPUT]
                Node Visits: %s

                [COLLISION INPUT]
                Node Visits: %s

                [COMPARISON]
                Traversal Difference: %d
                Traversal Ratio: %s

                =======================================
                """.formatted(
                result.normalResult().keyCount(),
                normalVisits,
                collisionVisits,
                result.traversalDifference(),
                ratio
        );
    }
}
