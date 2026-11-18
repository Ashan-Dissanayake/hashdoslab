package io.github.ashan.hashdoslab.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreeEngineComparisonReportFormatterTest {

    private ThreeEngineComparisonReportFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new ThreeEngineComparisonReportFormatter();
    }

    @Test
    void shouldFormatThreeEngineComparisonReport() {

        ThreeEngineComparisonResult result =
                new ThreeEngineComparisonResult(
                        100,
                        150000,
                        50000,
                        75000,
                        4950,
                        100,
                        100,
                        100
                );

        String report = formatter.format(result);

        assertTrue(report.contains("THREE-ENGINE HASHDOS COMPARISON"));
        assertTrue(report.contains("Collision Keys: 100"));

        assertTrue(report.contains("Vulnerable HashTable"));
        assertTrue(report.contains("Java HashMap"));
        assertTrue(report.contains("Hardened HashTable"));

        assertTrue(report.contains("Traversal Count"));
        assertTrue(report.contains("4,950"));

        assertTrue(report.contains("150,000"));
        assertTrue(report.contains("50,000"));
        assertTrue(report.contains("75,000"));
    }
}