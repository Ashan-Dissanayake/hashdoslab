
package io.github.ashan.hashdoslab.analysis;

import io.github.ashan.hashdoslab.engine.HardenedHashTable;
import io.github.ashan.hashdoslab.engine.HashTable;
import io.github.ashan.hashdoslab.engine.JavaHashMapAdapter;
import io.github.ashan.hashdoslab.engine.VulnerableHashTable;
import io.github.ashan.hashdoslab.payload.CollisionKeyGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ThreeEngineComparisonAnalyzer {

    private static final int MAX_KEY_COUNT = 10_000;
    private static final int WARMUP_ITERATIONS = 5;
    private static final int MEASUREMENT_ITERATIONS = 15;
    private static final int TABLE_CAPACITY = 16;

    private final CollisionKeyGenerator collisionKeyGenerator;

    public ThreeEngineComparisonAnalyzer(
            CollisionKeyGenerator collisionKeyGenerator
    ) {
        this.collisionKeyGenerator = collisionKeyGenerator;
    }

    public ThreeEngineComparisonResult compare(int keyCount) {

        if (keyCount <= 0) {
            throw new IllegalArgumentException(
                    "Key count must be greater than zero"
            );
        }

        if (keyCount > MAX_KEY_COUNT) {
            throw new IllegalArgumentException(
                    "Key count cannot exceed " + MAX_KEY_COUNT
            );
        }

        List<String> collisionKeys =
                collisionKeyGenerator.generateCollisionKeys(keyCount);

        List<Long> vulnerableSamples = new ArrayList<>();
        List<Long> javaHashMapSamples = new ArrayList<>();
        List<Long> hardenedSamples = new ArrayList<>();
        List<Long> vulnerableTraversalSamples = new ArrayList<>();

        // Warm-up phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            executeComparison(collisionKeys);
        }

        // Measurement phase
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {

            int order = i % 3;

            switch (order) {

                case 0 -> {
                    VulnerableMeasurement measurement =
                            measureVulnerable(collisionKeys);

                    vulnerableSamples.add(measurement.elapsedNanos());
                    vulnerableTraversalSamples.add(
                            measurement.traversalCount()
                    );

                    javaHashMapSamples.add(
                            measureInsertion(
                                    new JavaHashMapAdapter(),
                                    collisionKeys
                            )
                    );

                    hardenedSamples.add(
                            measureInsertion(
                                    new HardenedHashTable(),
                                    collisionKeys
                            )
                    );
                }

                case 1 -> {
                    javaHashMapSamples.add(
                            measureInsertion(
                                    new JavaHashMapAdapter(),
                                    collisionKeys
                            )
                    );

                    hardenedSamples.add(
                            measureInsertion(
                                    new HardenedHashTable(),
                                    collisionKeys
                            )
                    );

                    VulnerableMeasurement measurement =
                            measureVulnerable(collisionKeys);

                    vulnerableSamples.add(measurement.elapsedNanos());
                    vulnerableTraversalSamples.add(
                            measurement.traversalCount()
                    );
                }

                default -> {
                    hardenedSamples.add(
                            measureInsertion(
                                    new HardenedHashTable(),
                                    collisionKeys
                            )
                    );

                    VulnerableMeasurement measurement =
                            measureVulnerable(collisionKeys);

                    vulnerableSamples.add(measurement.elapsedNanos());
                    vulnerableTraversalSamples.add(
                            measurement.traversalCount()
                    );

                    javaHashMapSamples.add(
                            measureInsertion(
                                    new JavaHashMapAdapter(),
                                    collisionKeys
                            )
                    );
                }
            }
        }

        // Verify actual traversal count
        long expectedTraversalCount =
                ((long) keyCount * (keyCount - 1)) / 2;

        long actualTraversalCount =
                vulnerableTraversalSamples.get(0);

        if (actualTraversalCount != expectedTraversalCount) {
            throw new IllegalStateException(
                    "Vulnerable traversal count does not match expected count"
            );
        }

        if (vulnerableTraversalSamples.stream()
                .anyMatch(count -> count != actualTraversalCount)) {

            throw new IllegalStateException(
                    "Vulnerable traversal count is inconsistent across measurements"
            );
        }

        return new ThreeEngineComparisonResult(
                keyCount,

                WARMUP_ITERATIONS,
                MEASUREMENT_ITERATIONS,

                average(vulnerableSamples),
                median(vulnerableSamples),

                average(javaHashMapSamples),
                median(javaHashMapSamples),

                average(hardenedSamples),
                median(hardenedSamples),

                actualTraversalCount,

                keyCount,
                keyCount,
                keyCount
        );
    }

    private void executeComparison(List<String> collisionKeys) {

        measureInsertion(
                new VulnerableHashTable(TABLE_CAPACITY),
                collisionKeys
        );

        measureInsertion(
                new JavaHashMapAdapter(),
                collisionKeys
        );

        measureInsertion(
                new HardenedHashTable(),
                collisionKeys
        );
    }

    private long measureInsertion(
            HashTable table,
            List<String> collisionKeys
    ) {

        long start = System.nanoTime();

        for (int i = 0; i < collisionKeys.size(); i++) {
            table.put(collisionKeys.get(i), "value-" + i);
        }

        long end = System.nanoTime();

        // Correctness verification is outside the timed section
        verifyTable(table, collisionKeys);

        return end - start;
    }

    private void verifyTable(
            HashTable table,
            List<String> collisionKeys
    ) {

        if (table.size() != collisionKeys.size()) {
            throw new IllegalStateException(
                    "Entry count verification failed"
            );
        }

        for (int i = 0; i < collisionKeys.size(); i++) {

            String expectedValue = "value-" + i;

            if (!expectedValue.equals(table.get(collisionKeys.get(i)))) {
                throw new IllegalStateException(
                        "Data verification failed for key: "
                                + collisionKeys.get(i)
                );
            }
        }
    }

    private VulnerableMeasurement measureVulnerable(
            List<String> collisionKeys
    ) {

        VulnerableHashTable table =
                new VulnerableHashTable(TABLE_CAPACITY);

        long elapsedNanos = measureInsertion(table, collisionKeys);

        return new VulnerableMeasurement(
                elapsedNanos,
                table.getTraversalCount()
        );
    }

    private record VulnerableMeasurement(
            long elapsedNanos,
            long traversalCount
    ) {}

    private long average(List<Long> samples) {

        return Math.round(
                samples.stream()
                        .mapToLong(Long::longValue)
                        .average()
                        .orElseThrow()
        );
    }

    private long median(List<Long> samples) {

        List<Long> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);

        int middle = sorted.size() / 2;

        return sorted.get(middle);
    }
}
