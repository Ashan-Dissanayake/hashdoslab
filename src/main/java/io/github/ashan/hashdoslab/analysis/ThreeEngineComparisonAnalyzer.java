package io.github.ashan.hashdoslab.analysis;

import io.github.ashan.hashdoslab.engine.HardenedHashTable;
import io.github.ashan.hashdoslab.engine.HashTable;
import io.github.ashan.hashdoslab.engine.JavaHashMapAdapter;
import io.github.ashan.hashdoslab.engine.VulnerableHashTable;
import io.github.ashan.hashdoslab.payload.CollisionKeyGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ThreeEngineComparisonAnalyzer {

    private final CollisionKeyGenerator collisionKeyGenerator;
    private static final int MAX_KEY_COUNT = 10_000;

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

        HashTable vulnerable = new VulnerableHashTable(16);
        HashTable javaHashMap = new JavaHashMapAdapter();
        HashTable hardened = new HardenedHashTable();

        // Vulnerable implementation
        long vulnerableStart = System.nanoTime();

        for (int i = 0; i < keyCount; i++) {
            vulnerable.put(collisionKeys.get(i), "value-" + i);
        }

        long vulnerableEnd = System.nanoTime();


// Java HashMap implementation
        long javaHashMapStart = System.nanoTime();

        for (int i = 0; i < keyCount; i++) {
            javaHashMap.put(collisionKeys.get(i), "value-" + i);
        }

        long javaHashMapEnd = System.nanoTime();


// Hardened implementation
        long hardenedStart = System.nanoTime();

        for (int i = 0; i < keyCount; i++) {
            hardened.put(collisionKeys.get(i), "value-" + i);
        }

        long hardenedEnd = System.nanoTime();

        long vulnerableTime = vulnerableEnd - vulnerableStart;
        long javaHashMapTime = javaHashMapEnd - javaHashMapStart;
        long hardenedTime = hardenedEnd - hardenedStart;

        // Verify entry counts
        if (vulnerable.size() != keyCount
                || javaHashMap.size() != keyCount
                || hardened.size() != keyCount) {

            throw new IllegalStateException(
                    "Entry count verification failed"
            );
        }


// Verify retrieved values
        for (int i = 0; i < keyCount; i++) {

            String key = collisionKeys.get(i);
            String expectedValue = "value-" + i;

            if (!expectedValue.equals(vulnerable.get(key))
                    || !expectedValue.equals(javaHashMap.get(key))
                    || !expectedValue.equals(hardened.get(key))) {

                throw new IllegalStateException(
                        "Data verification failed for key: " + key
                );
            }
        }

        long vulnerableTraversalCount = ((VulnerableHashTable) vulnerable).getTraversalCount();

        int vulnerableEntryCount = vulnerable.size();
        int javaHashMapEntryCount = javaHashMap.size();
        int hardenedEntryCount = hardened.size();


        return new ThreeEngineComparisonResult(
                keyCount,

                vulnerableTime,
                javaHashMapTime,
                hardenedTime,

                vulnerableTraversalCount,

                vulnerableEntryCount,
                javaHashMapEntryCount,
                hardenedEntryCount
        );

    }
}