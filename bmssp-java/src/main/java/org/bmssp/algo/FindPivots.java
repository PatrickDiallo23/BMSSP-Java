package org.bmssp.algo;

import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FIND_PIVOTS implementation - bounded Bellman-Ford-like algorithm
 */
public class FindPivots {

    public record Result(Set<Integer> P, Set<Integer> W) {}

    /**
     * Heuristic approximation of FINDPIVOTS algorithm
     *
     * @param graph The input graph
     * @param dist Current distance estimates
     * @param S Set of source nodes
     * @param B Upper bound on distances
     * @param n Number of nodes in graph
     * @param kSteps Number of relaxation steps to perform
     * @param pLimit Maximum number of pivots to select
     * @param instr Instrumentation for tracking operations
     * @return Result containing pivot set P and discovered set W
     */
    public static Result findPivots(Graph graph, Map<Integer, Double> dist, Set<Integer> S,
                                    double B, int n, int kSteps, int pLimit, Instrument instr) {
        // Filter S to those with dist < B
        var SFiltered = S.stream()
                .filter(v -> dist.getOrDefault(v, Double.POSITIVE_INFINITY) < B)
                .collect(Collectors.toList());

        Set<Integer> P;
        if (SFiltered.isEmpty()) {
            // Fallback: choose up to pLimit arbitrary samples from S
            P = S.stream()
                    .limit(Math.max(1, Math.min(S.size(), pLimit)))
                    .collect(Collectors.toSet());
        } else {
            // Choose pivots with smallest distances
            SFiltered.sort(Comparator.comparing(v -> dist.getOrDefault(v, Double.POSITIVE_INFINITY)));
            P = SFiltered.stream()
                    .limit(Math.max(1, Math.min(SFiltered.size(), pLimit)))
                    .collect(Collectors.toSet());
        }

        // Bounded Bellman-Ford: start frontier from P (if P empty use S)
        var sourceFrontier = P.isEmpty() ? new HashSet<>(S) : new HashSet<>(P);
        var discovered = new HashSet<>(sourceFrontier);
        var frontier = new HashSet<>(sourceFrontier);

        // Perform bounded relaxations
        for (int step = 0; step < Math.max(1, kSteps); step++) {
            if (frontier.isEmpty()) {
                break;
            }

            var nextFront = new HashSet<Integer>();
            for (int u : frontier) {
                double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
                if (du >= B) {
                    continue;
                }

                for (var edge : graph.getNeighbors(u)) {
                    instr.incrementRelaxations();
                    int v = edge.to();
                    double weight = edge.weight();
                    double nd = du + weight;

                    // Consider only nodes with nd < B
                    if (nd < B && !discovered.contains(v)) {
                        discovered.add(v);
                        nextFront.add(v);
                    }
                }
            }
            frontier = nextFront;
        }

        var W = new HashSet<>(discovered);

        // P must be non-empty if S is non-empty
        if (P.isEmpty() && !S.isEmpty()) {
            P = Set.of(S.iterator().next());
        }

        return new Result(P, W);
    }
}