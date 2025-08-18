package org.bmssp.algo;

import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;

public class BaseCase {
    public record Result(double Bprime, Set<Integer> Uo) {}

    private static class NodeDist implements Comparable<NodeDist> {
        final int node; final double dist;
        NodeDist(int node, double dist) { this.node = node; this.dist = dist; }
        public int compareTo(NodeDist o) { return Double.compare(this.dist, o.dist); }
    }

    public static Result run(Graph graph,
                             Map<Integer, Double> dist,
                             double B,
                             Set<Integer> S,
                             int k,
                             Instrument instr) {
        if (S.isEmpty()) return new Result(B, new HashSet<>());
        int x = S.stream().min(Comparator.comparingDouble(v -> dist.getOrDefault(v, Double.POSITIVE_INFINITY))).orElseThrow();
        PriorityQueue<NodeDist> pq = new PriorityQueue<>();
        double start = dist.getOrDefault(x, Double.POSITIVE_INFINITY);
        pq.add(new NodeDist(x, start));
        instr.incHeap();

        Set<Integer> Uo = new HashSet<>();
        while (!pq.isEmpty() && Uo.size() < (k + 1)) {
            NodeDist nd = pq.poll();
            instr.incHeap();
            if (nd.dist > dist.getOrDefault(nd.node, Double.POSITIVE_INFINITY)) continue;
            Uo.add(nd.node);
            for (Graph.Neighbor nb : graph.getAdj().get(nd.node)) {
                instr.incRelax();
                double newd = dist.getOrDefault(nd.node, Double.POSITIVE_INFINITY) + nb.w();
                if (newd < dist.getOrDefault(nb.v(), Double.POSITIVE_INFINITY) && newd < B) {
                    dist.put(nb.v(), newd);
                    pq.add(new NodeDist(nb.v(), newd));
                    instr.incHeap();
                }
            }
        }
        if (Uo.size() <= k) return new Result(B, Uo);
        double maxd = Uo.stream().mapToDouble(v -> dist.getOrDefault(v, Double.POSITIVE_INFINITY))
                .filter(Double::isFinite).max().orElse(B);
        Set<Integer> Ufiltered = new HashSet<>();
        for (int v : Uo) if (dist.getOrDefault(v, Double.POSITIVE_INFINITY) < maxd) Ufiltered.add(v);
        return new Result(maxd, Ufiltered);
    }
}
