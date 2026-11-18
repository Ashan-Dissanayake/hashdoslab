package io.github.ashan.hashdoslab.payload;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CollisionKeyGeneratorTest {

    private final CollisionKeyGenerator generator =
            new CollisionKeyGenerator();

    @Test
    void shouldGenerateRequestedNumberOfKeys() {
        List<String> keys = generator.generateCollisionKeys(5);

        assertThat(keys).hasSize(5);
    }

    @Test
    void generatedKeysShouldHaveSameHashCode() {
        List<String> keys = generator.generateCollisionKeys(5);

        assertThat(keys)
                .extracting(String::hashCode)
                .containsOnly(keys.get(0).hashCode());
    }

    @Test
    void generatedKeysShouldBeUnique() {
        List<String> keys = generator.generateCollisionKeys(5);

        assertThat(keys).doesNotHaveDuplicates();
    }

    @Test
    void shouldRejectNegativeCount() {
        assertThatThrownBy(() -> generator.generateCollisionKeys(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}