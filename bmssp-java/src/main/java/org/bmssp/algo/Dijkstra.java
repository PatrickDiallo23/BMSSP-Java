package org.bmssp.algo;

import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;

public class Dijkstra {
    private static class NodeDist implements Comparable<NodeDist> {
        final int node; final double dist;
        NodeDist(int node, double dist) { this.node = node; this.dist = dist; }
        public int compareTo(NodeDist o) { return Double.compare(this.dist, o.dist); }
    }

    public static Map<Integer, Double> run(Graph graph, int source, Instrument instr) {
        Map<Integer, Double> dist = new HashMap<>();
        for (int v = 0; v < graph.size(); v++) dist.put(v, Double.POSITIVE_INFINITY);
        dist.put(source, 0.0);
        PriorityQueue<NodeDist> pq = new PriorityQueue<>();
        pq.add(new NodeDist(source, 0.0));
        instr.incHeap();
        while (!pq.isEmpty()) {
            NodeDist nd = pq.poll();
            instr.incHeap();
            if (nd.dist > dist.get(nd.node)) continue;
            for (Graph.Neighbor nb : graph.getAdj().get(nd.node)) {
                instr.incRelax();
                double alt = nd.dist + nb.w();
                if (alt < dist.getOrDefault(nb.v(), Double.POSITIVE_INFINITY)) {
                    dist.put(nb.v(), alt);
                    pq.add(new NodeDist(nb.v(), alt));
                    instr.incHeap();
                }
            }
        }
        return dist;
    }
}
