
package io.github.ashan.hashdoslab.analysis;

import io.github.ashan.hashdoslab.engine.VulnerableHashTable;
import io.github.ashan.hashdoslab.payload.CollisionKeyGenerator;

import java.util.ArrayList;
import java.util.List;

public class CollisionAnalyzer {

    private static final int TABLE_CAPACITY = 16;

    private final CollisionKeyGenerator keyGenerator;

    public CollisionAnalyzer(CollisionKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public AnalysisResult analyzeNormalInput(int keyCount) {

        validateKeyCount(keyCount);

        List<String> keys = generateNormalKeys(keyCount);

        return analyze(keys, InputType.NORMAL);
    }

    public AnalysisResult analyzeCollisionInput(int keyCount) {

        validateKeyCount(keyCount);

        List<String> keys =
                keyGenerator.generateCollisionKeys(keyCount);

        return analyze(keys, InputType.COLLISION);
    }

    private AnalysisResult analyze(
            List<String> keys,
            InputType inputType
    ) {

        VulnerableHashTable hashTable =
                new VulnerableHashTable(TABLE_CAPACITY);

        for (String key : keys) {
            hashTable.put(key, "value");
        }

        return new AnalysisResult(
                inputType,
                keys.size(),
                hashTable.getTraversalCount()
        );
    }

    private List<String> generateNormalKeys(int keyCount) {

        List<String> keys = new ArrayList<>();

        for (int i = 0; i < keyCount; i++) {
            keys.add("normal-key-" + i);
        }

        return keys;
    }

    private void validateKeyCount(int keyCount) {

        if (keyCount < 0) {
            throw new IllegalArgumentException(
                    "Key count cannot be negative"
            );
        }
    }


    public ComparisonResult compareInputs(int keyCount) {

        AnalysisResult normalResult =
                analyzeNormalInput(keyCount);

        AnalysisResult collisionResult =
                analyzeCollisionInput(keyCount);

        long traversalDifference =
                collisionResult.traversalCount()
                        - normalResult.traversalCount();

        Double traversalRatio = null;

        if (normalResult.traversalCount() > 0) {
            traversalRatio =
                    (double) collisionResult.traversalCount()
                            / normalResult.traversalCount();
        }

        return new ComparisonResult(
                normalResult,
                collisionResult,
                traversalDifference,
                traversalRatio
        );
    }


    public enum InputType {
        NORMAL,
        COLLISION
    }

    public record AnalysisResult(
            InputType inputType,
            int keyCount,
            long traversalCount
    ) {}


    public record ComparisonResult(
            AnalysisResult normalResult,
            AnalysisResult collisionResult,
            long traversalDifference,
            Double traversalRatio
    ) {}

}
