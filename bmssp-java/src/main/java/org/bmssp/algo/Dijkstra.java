package org.bmssp.algo;

import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;

/**
 * Standard Dijkstra's shortest path algorithm implementation
 */
public class Dijkstra {

    public record DistanceNode(double distance, int node) implements Comparable<DistanceNode> {
        @Override
        public int compareTo(DistanceNode other) {
            int cmp = Double.compare(this.distance, other.distance);
            return cmp != 0 ? cmp : Integer.compare(this.node, other.node);
        }
    }

    /**
     * Run Dijkstra's algorithm from source node
     */
    public static Map<Integer, Double> shortestPaths(Graph graph, int source, Instrument instr) {
        var dist = new HashMap<Integer, Double>();

        // Initialize distances
        for (int node : graph.getNodes()) {
            dist.put(node, Double.POSITIVE_INFINITY);
        }
        dist.put(source, 0.0);

        var heap = new PriorityQueue<DistanceNode>();
        heap.offer(new DistanceNode(0.0, source));
        instr.incrementHeapOps();

        while (!heap.isEmpty()) {
            var current = heap.poll();
            instr.incrementHeapOps();

            double dU = current.distance();
            int u = current.node();

            // Skip if we've already found a better path
            if (dU > dist.get(u)) {
                continue;
            }

            // Relax all neighbors
            for (var edge : graph.getNeighbors(u)) {
                instr.incrementRelaxations();
                int v = edge.to();
                double weight = edge.weight();
                double alt = dU + weight;

                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    heap.offer(new DistanceNode(alt, v));
                    instr.incrementHeapOps();
                }
            }
        }

        return dist;
    }
}