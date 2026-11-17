
package io.github.ashan.hashdoslab.analysis;

import io.github.ashan.hashdoslab.payload.CollisionKeyGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CollisionAnalyzerTest {

    private final CollisionAnalyzer analyzer =
            new CollisionAnalyzer(new CollisionKeyGenerator());

    @Test
    void shouldAnalyzeCollisionInput() {

        var result = analyzer.analyzeCollisionInput(5);

        assertThat(result.inputType())
                .isEqualTo(CollisionAnalyzer.InputType.COLLISION);

        assertThat(result.keyCount()).isEqualTo(5);

        assertThat(result.traversalCount()).isEqualTo(10);
    }

    @Test
    void shouldAnalyzeNormalInput() {

        var result = analyzer.analyzeNormalInput(100);

        assertThat(result.inputType())
                .isEqualTo(CollisionAnalyzer.InputType.NORMAL);

        assertThat(result.keyCount()).isEqualTo(100);
    }

    @Test
    void collisionInputShouldRequireMoreTraversalsThanNormalInput() {

        var normalResult = analyzer.analyzeNormalInput(100);

        var collisionResult = analyzer.analyzeCollisionInput(100);

        assertThat(collisionResult.traversalCount())
                .isGreaterThan(normalResult.traversalCount());
    }

    @Test
    void collisionTraversalCountShouldGrowQuadratically() {

        var small = analyzer.analyzeCollisionInput(10);
        var large = analyzer.analyzeCollisionInput(20);

        assertThat(small.traversalCount()).isEqualTo(45);
        assertThat(large.traversalCount()).isEqualTo(190);
    }

    @Test
    void shouldRejectNegativeKeyCount() {

        assertThatThrownBy(
                () -> analyzer.analyzeNormalInput(-1)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(
                () -> analyzer.analyzeCollisionInput(-1)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
