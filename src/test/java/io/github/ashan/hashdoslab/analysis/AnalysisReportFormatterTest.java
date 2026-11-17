
package io.github.ashan.hashdoslab.analysis;

import io.github.ashan.hashdoslab.payload.CollisionKeyGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportFormatterTest {

    private final CollisionAnalyzer analyzer =
            new CollisionAnalyzer(new CollisionKeyGenerator());

    private final AnalysisReportFormatter formatter =
            new AnalysisReportFormatter();

    @Test
    void shouldFormatComparisonReport() {

        var result = analyzer.compareInputs(100);

        String report = formatter.format(result);

        assertThat(report)
                .contains("HASH DOS ANALYSIS")
                .contains("Input Size: 100")
                .contains("[NORMAL INPUT]")
                .contains("[COLLISION INPUT]")
                .contains("[COMPARISON]")
                .contains("Node Visits:")
                .contains("Traversal Difference:")
                .contains("Traversal Ratio:");
    }

    @Test
    void shouldDisplayNotAvailableWhenRatioIsNull() {

        var result = analyzer.compareInputs(1);

        String report = formatter.format(result);

        assertThat(report)
                .contains("Traversal Ratio: N/A");
    }

    @Test
    void shouldDisplayCollisionTraversalCount() {

        var result = analyzer.compareInputs(100);

        String report = formatter.format(result);

        assertThat(report)
                .contains("Node Visits: 4950");
    }
}
