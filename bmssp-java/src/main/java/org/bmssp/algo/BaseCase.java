package org.bmssp.algo;

import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BASECASE implementation - Dijkstra-like expansion with limits
 */
public class BaseCase {

    public record Result(double BPrime, Set<Integer> UoSet) {}

    public record DistanceNode(double distance, int node) implements Comparable<DistanceNode> {
        @Override
        public int compareTo(DistanceNode other) {
            int cmp = Double.compare(this.distance, other.distance);
            return cmp != 0 ? cmp : Integer.compare(this.node, other.node);
        }
    }

    /**
     * BASECASE: Run Dijkstra-like expansion from best node in S
     *
     * @param graph The input graph
     * @param dist Current distance estimates (modified in-place)
     * @param B Upper bound on distances
     * @param S Set of source nodes (should be singleton, but handles multiple)
     * @param k Limit on number of nodes to process
     * @param instr Instrumentation for tracking operations
     * @return Result containing B' and set of completed nodes
     */
    public static Result baseCase(Graph graph, Map<Integer, Double> dist, double B,
                                  Set<Integer> S, int k, Instrument instr) {
        if (S.isEmpty()) {
            return new Result(B, Set.of());
        }

        // Choose source x in S with smallest distance
        int x = S.stream()
                .min(Comparator.comparing(v -> dist.getOrDefault(v, Double.POSITIVE_INFINITY)))
                .orElseThrow();

        // Local heap for this base case
        var heap = new PriorityQueue<DistanceNode>();
        double startD = dist.getOrDefault(x, Double.POSITIVE_INFINITY);
        heap.offer(new DistanceNode(startD, x));
        instr.incrementHeapOps();

        var Uo = new HashSet<Integer>();

        while (!heap.isEmpty() && Uo.size() < (k + 1)) {
            var current = heap.poll();
            instr.incrementHeapOps();

            double dU = current.distance();
            int u = current.node();

            if (dU > dist.getOrDefault(u, Double.POSITIVE_INFINITY)) {
                continue;
            }

            // Mark 'u' as complete for this base case
            Uo.add(u);

            // Relax neighbors
            for (var edge : graph.getNeighbors(u)) {
                instr.incrementRelaxations();
                int v = edge.to();
                double weight = edge.weight();
                double newD = dist.getOrDefault(u, Double.POSITIVE_INFINITY) + weight;

                if (newD < dist.getOrDefault(v, Double.POSITIVE_INFINITY) && newD < B) {
                    dist.put(v, newD);
                    heap.offer(new DistanceNode(newD, v));
                    instr.incrementHeapOps();
                }
            }
        }

        if (Uo.size() <= k) {
            return new Result(B, Uo);
        } else {
            // Filter to nodes with distances less than the maximum finite distance
            var finiteDists = Uo.stream()
                    .mapToDouble(v -> dist.getOrDefault(v, Double.POSITIVE_INFINITY))
                    .filter(Double::isFinite)
                    .boxed()
                    .toList();

            if (finiteDists.isEmpty()) {
                return new Result(B, Set.of());
            }

            double maxD = Collections.max(finiteDists);
            var UFiltered = Uo.stream()
                    .filter(v -> dist.getOrDefault(v, Double.POSITIVE_INFINITY) < maxD)
                    .collect(Collectors.toSet());

            return new Result(maxD, UFiltered);
        }
    }
}