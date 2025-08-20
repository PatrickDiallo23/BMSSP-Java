package org.bmssp.algo;

import java.util.*;

/**
 * Practical approximation of the paper's DataStructure D (Partial Queue)
 * Supports insert, batch_prepend, and pull operations.
 * This reference implementation uses a priority queue to maintain the smallest keys
 * and a map to track the best keys for each node.
 */
public class DataStructureD {

    public record NodeKey(int node, double key) implements Comparable<NodeKey> {
        @Override
        public int compareTo(NodeKey other) {
            int cmp = Double.compare(this.key, other.key);
            return cmp != 0 ? cmp : Integer.compare(this.node, other.node);
        }
    }

    public record PullResult(double Bi, Set<Integer> Si) {}

    private final PriorityQueue<NodeKey> heap;
    private final Map<Integer, Double> best;
    private final int M;
    private final double BUpper;
    private final int blockSize;

    public DataStructureD(int M, double BUpper, Integer blockSize) {
        this.heap = new PriorityQueue<>();
        this.best = new HashMap<>();
        this.M = Math.max(1, M);
        this.BUpper = BUpper;
        this.blockSize = blockSize != null ? blockSize : Math.max(1, this.M / 8);
    }

    /**
     * Insert a node with given key
     */
    public void insert(int node, double key) {
        Double prev = best.get(node);
        if (prev == null || key < prev) {
            best.put(node, key);
            heap.offer(new NodeKey(node, key));
        }
    }

    /**
     * Batch insert nodes with small keys
     */
    public void batchPrepend(Collection<NodeKey> pairs) {
        for (var pair : pairs) {
            insert(pair.node(), pair.key());
        }
    }

    /**
     * Remove stale heap entries
     */
    private void cleanup() {
        while (!heap.isEmpty()) {
            var top = heap.peek();
            Double currentBest = best.get(top.node());
            if (currentBest == null || !currentBest.equals(top.key())) {
                heap.poll();
            } else {
                break;
            }
        }
    }

    /**
     * Check if the data structure is empty
     */
    public boolean isEmpty() {
        cleanup();
        return heap.isEmpty();
    }

    /**
     * Pull the smallest key (Bi) and a set of nodes with smallest keys (Si)
     */
    public PullResult pull() {
        cleanup();
        if (heap.isEmpty()) {
            throw new IllegalStateException("Cannot pull from empty DataStructureD");
        }

        // Get the smallest key
        double Bi = heap.peek().key();
        var Si = new HashSet<Integer>();

        // Pop up to blockSize best current entries
        while (!heap.isEmpty() && Si.size() < blockSize) {
            var current = heap.poll();
            int node = current.node();
            double key = current.key();

            Double currentBest = best.get(node);
            if (currentBest != null && currentBest.equals(key)) {
                Si.add(node);
                // Remove from best to mark as "pulled"
                best.remove(node);
            }
        }

        return new PullResult(Bi, Si);
    }
}