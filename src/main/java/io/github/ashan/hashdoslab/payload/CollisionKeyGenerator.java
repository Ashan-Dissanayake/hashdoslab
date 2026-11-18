package io.github.ashan.hashdoslab.payload;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CollisionKeyGenerator {

    public List<String> generateCollisionKeys(int count) {

        if (count < 0) {
            throw new IllegalArgumentException(
                    "Count cannot be negative"
            );
        }

        List<String> keys = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            keys.add("Aa".repeat(i) + "BB".repeat(count - i));
        }

        return keys;
    }
}