package org.bmssp.algo;

import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;

public class FindPivots {
    public record Result(Set<Integer> P, Set<Integer> W) {}

    public static Result run(Graph graph,
                             Map<Integer, Double> dist,
                             Set<Integer> S,
                             double B,
                             int n,
                             int kSteps,
                             int pLimit,
                             Instrument instr) {
        // Filter S to those with dist < B
        List<Integer> sFiltered = new ArrayList<>();
        for (int v : S) {
            if (dist.getOrDefault(v, Double.POSITIVE_INFINITY) < B) sFiltered.add(v);
        }
        Set<Integer> P;
        if (sFiltered.isEmpty()) {
            P = new HashSet<>();
            int take = Math.max(1, Math.min(S.size(), pLimit));
            int i = 0;
            for (int v : S) { P.add(v); if (++i >= take) break; }
        } else {
            sFiltered.sort(Comparator.comparingDouble(v -> dist.getOrDefault(v, Double.POSITIVE_INFINITY)));
            P = new HashSet<>(sFiltered.subList(0, Math.max(1, Math.min(sFiltered.size(), pLimit))));
        }

        Set<Integer> sourceFrontier = P.isEmpty() ? new HashSet<>(S) : new HashSet<>(P);
        Set<Integer> discovered = new HashSet<>(sourceFrontier);
        Set<Integer> frontier = new HashSet<>(sourceFrontier);

        for (int step = 0; step < Math.max(1, kSteps); step++) {
            if (frontier.isEmpty()) break;
            Set<Integer> next = new HashSet<>();
            for (int u : frontier) {
                double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
                if (du >= B) continue;
                for (Graph.Neighbor nb : graph.getAdj().get(u)) {
                    instr.incRelax();
                    double nd = du + nb.w();
                    if (nd < B && !discovered.contains(nb.v())) {
                        discovered.add(nb.v());
                        next.add(nb.v());
                    }
                }
            }
            frontier = next;
        }
        Set<Integer> W = new HashSet<>(discovered);
        if (P.isEmpty() && !S.isEmpty()) P = Set.of(S.iterator().next());
        return new Result(P, W);
    }
}
