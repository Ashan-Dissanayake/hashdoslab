
package io.github.ashan.hashdoslab.engine;

import java.util.Objects;

/**
 * An intentionally vulnerable hash table implementation.
 *
 * Uses separate chaining with singly linked lists.
 * Entries are appended to the end of each bucket's linked list.
 *
 * This implementation is designed for controlled HashDoS
 * and algorithmic complexity demonstrations.
 *
 * Do not use in production applications.
 */
public class VulnerableHashTable implements HashTable {

    private final Node[] buckets;
    private int size;
    private long traversalCount;

    public VulnerableHashTable(int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero"
            );
        }

        this.buckets = new Node[capacity];
        this.size = 0;
    }

    /**
     * Inserts a key-value pair into the hash table.
     *
     * If the key already exists, its value is updated.
     * Otherwise, a new node is appended to the bucket's linked list.
     */
    @Override
    public void put(String key, String value) {

        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");

        int index = getBucketIndex(key);

        Node newNode = new Node(key, value);

        // Empty bucket: insert directly.
        if (buckets[index] == null) {
            buckets[index] = newNode;
            size++;
            return;
        }

        // Traverse the linked list.
        Node current = buckets[index];


        while (true) {

            traversalCount++;

            if (current.key.equals(key)) {
                current.value = value;
                return;
            }

            if (current.next == null) {
                current.next = newNode;
                size++;
                return;
            }

            current = current.next;
        }

    }

    /**
     * Retrieves the value associated with a key.
     */
    @Override

    public String get(String key) {

        Objects.requireNonNull(key, "Key cannot be null");

        int index = getBucketIndex(key);

        Node current = buckets[index];

        while (current != null) {

            if (current.key.equals(key)) {
                return current.value;
            }

            current = current.next;
        }

        return null;
    }

    /**
     * Returns the number of traversals performed during hash table operations.
     */
    public long getTraversalCount() {
        return traversalCount;
    }

    /**
     * Returns the number of stored key-value pairs.
     */
    @Override

    public int size() {
        return size;
    }

    /**
     * Calculates the bucket index for a given key.
     */
    private int getBucketIndex(String key) {

        return Math.floorMod(
                key.hashCode(),
                buckets.length
        );
    }

    /**
     * Internal linked list node.
     */
    private static class Node {

        private final String key;
        private String value;
        private Node next;

        private Node(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
