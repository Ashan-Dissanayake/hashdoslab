
package io.github.ashan.hashdoslab.service;

import io.github.ashan.hashdoslab.analysis.CollisionAnalyzer;
import io.github.ashan.hashdoslab.api.dto.response.AnalysisResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private CollisionAnalyzer collisionAnalyzer;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void shouldMapComparisonResultToAnalysisResponse() {

        // Arrange
        int keyCount = 100;

        CollisionAnalyzer.AnalysisResult normalResult =
                new CollisionAnalyzer.AnalysisResult(
                        CollisionAnalyzer.InputType.NORMAL,
                        keyCount,
                        250
                );

        CollisionAnalyzer.AnalysisResult collisionResult =
                new CollisionAnalyzer.AnalysisResult(
                        CollisionAnalyzer.InputType.COLLISION,
                        keyCount,
                        4950
                );

        CollisionAnalyzer.ComparisonResult comparisonResult =
                new CollisionAnalyzer.ComparisonResult(
                        normalResult,
                        collisionResult,
                        4700,
                        19.8
                );

        when(collisionAnalyzer.compareInputs(keyCount))
                .thenReturn(comparisonResult);

        // Act
        AnalysisResponse response =
                analysisService.compare(keyCount);

        // Assert
        assertNotNull(response);
        assertEquals(100, response.keyCount());
        assertEquals(250, response.normal().traversalCount());
        assertEquals(4950, response.collision().traversalCount());
        assertEquals(4700, response.traversalDifference());
        assertEquals(19.8, response.traversalRatio());

        verify(collisionAnalyzer, times(1))
                .compareInputs(keyCount);
    }

    @Test
    void shouldPreserveZeroTraversalDifference() {

        // Arrange
        int keyCount = 1;

        CollisionAnalyzer.AnalysisResult normalResult =
                new CollisionAnalyzer.AnalysisResult(
                        CollisionAnalyzer.InputType.NORMAL,
                        keyCount,
                        0
                );

        CollisionAnalyzer.AnalysisResult collisionResult =
                new CollisionAnalyzer.AnalysisResult(
                        CollisionAnalyzer.InputType.COLLISION,
                        keyCount,
                        0
                );

        CollisionAnalyzer.ComparisonResult comparisonResult =
                new CollisionAnalyzer.ComparisonResult(
                        normalResult,
                        collisionResult,
                        0,
                        null
                );

        when(collisionAnalyzer.compareInputs(keyCount))
                .thenReturn(comparisonResult);

        // Act
        AnalysisResponse response =
                analysisService.compare(keyCount);

        // Assert
        assertEquals(0, response.traversalDifference());
        assertEquals(0, response.normal().traversalCount());
        assertEquals(0, response.collision().traversalCount());
        assertNull(response.traversalRatio());
    }

    @Test
    void shouldPreserveNullTraversalRatio() {

        // Arrange
        int keyCount = 1;

        CollisionAnalyzer.AnalysisResult normalResult =
                new CollisionAnalyzer.AnalysisResult(
                        CollisionAnalyzer.InputType.NORMAL,
                        keyCount,
                        0
                );

        CollisionAnalyzer.AnalysisResult collisionResult =
                new CollisionAnalyzer.AnalysisResult(
                        CollisionAnalyzer.InputType.COLLISION,
                        keyCount,
                        0
                );

        CollisionAnalyzer.ComparisonResult comparisonResult =
                new CollisionAnalyzer.ComparisonResult(
                        normalResult,
                        collisionResult,
                        0,
                        null
                );

        when(collisionAnalyzer.compareInputs(keyCount))
                .thenReturn(comparisonResult);

        // Act
        AnalysisResponse response =
                analysisService.compare(keyCount);

        // Assert
        assertNull(response.traversalRatio());
    }

    @Test
    void shouldCallCollisionAnalyzerOnlyOnce() {

        // Arrange
        int keyCount = 10;

        CollisionAnalyzer.AnalysisResult normalResult =
                new CollisionAnalyzer.AnalysisResult(
                        CollisionAnalyzer.InputType.NORMAL,
                        keyCount,
                        5
                );

        CollisionAnalyzer.AnalysisResult collisionResult =
                new CollisionAnalyzer.AnalysisResult(
                        CollisionAnalyzer.InputType.COLLISION,
                        keyCount,
                        45
                );

        CollisionAnalyzer.ComparisonResult comparisonResult =
                new CollisionAnalyzer.ComparisonResult(
                        normalResult,
                        collisionResult,
                        40,
                        9.0
                );

        when(collisionAnalyzer.compareInputs(keyCount))
                .thenReturn(comparisonResult);

        // Act
        analysisService.compare(keyCount);

        // Assert
        verify(collisionAnalyzer, times(1))
                .compareInputs(keyCount);

        verifyNoMoreInteractions(collisionAnalyzer);
    }
}
