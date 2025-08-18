package org.bmssp.algo;

import java.util.*;

/** Practical approximation of the paper's D. */
public class DataStructureD {
    private static class KeyNode implements Comparable<KeyNode> {
        final int v; final double key;
        KeyNode(int v, double key) { this.v = v; this.key = key; }
        public int compareTo(KeyNode o) { return Double.compare(this.key, o.key); }
    }

    public record PullResult(double Bi, Set<Integer> Si) {}

    private final PriorityQueue<KeyNode> heap = new PriorityQueue<>();
    private final Map<Integer, Double> best = new HashMap<>();
    private final int M;
    private final double Bupper;
    private final int blockSize;

    public DataStructureD(int M, double Bupper, Integer blockSize) {
        this.M = Math.max(1, M);
        this.Bupper = Bupper;
        this.blockSize = (blockSize != null) ? Math.max(1, blockSize) : Math.max(1, this.M / 8);
    }

    public void insert(int v, double key) {
        Double prev = best.get(v);
        if (prev == null || key < prev) {
            best.put(v, key);
            heap.add(new KeyNode(v, key));
        }
    }

    public void batchPrepend(Collection<Map.Entry<Integer, Double>> pairs) {
        for (Map.Entry<Integer, Double> e : pairs) insert(e.getKey(), e.getValue());
    }

    private void cleanup() {
        while (!heap.isEmpty()) {
            KeyNode top = heap.peek();
            Double b = best.get(top.v);
            if (b == null || b != top.key) heap.poll(); else break;
        }
    }

    public boolean isEmpty() {
        cleanup();
        return heap.isEmpty();
    }

    public PullResult pull() {
        cleanup();
        if (heap.isEmpty()) throw new NoSuchElementException("pull from empty D");
        double Bi = heap.peek().key;
        Set<Integer> Si = new HashSet<>();
        while (!heap.isEmpty() && Si.size() < blockSize) {
            KeyNode kn = heap.poll();
            Double b = best.get(kn.v);
            if (b != null && b == kn.key) {
                Si.add(kn.v);
                best.remove(kn.v);
            }
        }
        return new PullResult(Bi, Si);
    }
}