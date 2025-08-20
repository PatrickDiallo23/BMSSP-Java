package org.bmssp.algo;

import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;

/**
 * The Duan et al. SSSP engine implementing O(m * log^(2/3) n) shortest paths.
 *
 * Implements the BMSSP recursion with pivoting as presented by
 * Duan, Jiayi Mao, Xiao Mao, Xinkai Shu, Longhui Yin (July 31, 2025).
 *
 * Paper citation: "Breaking the Sorting Barrier for Directed Single-Source Shortest Paths",
 * arXiv:2504.17033v2, July 31, 2025.
 */
public class BMSSP {

    public record Result(double BPrime, Set<Integer> U) {}

    /**
     * BMSSP recursive function
     *
     * @param graph The input graph
     * @param dist Current distance estimates (modified in-place)
     * @param edges List of all edges (for reference)
     * @param l Recursion depth
     * @param B Upper bound on distances
     * @param S Set of source nodes
     * @param n Number of nodes in graph
     * @param instr Instrumentation for tracking operations
     * @return Result containing B' and set of reached nodes
     */
    public static Result bmssp(Graph graph, Map<Integer, Double> dist, List<Graph.Edge> edges,
                               int l, double B, Set<Integer> S, int n, Instrument instr) {

        // Calculate heuristic parameters
        int tParam, kParam;
        if (n <= 2) {
            tParam = 1;
            kParam = 2;
        } else {
            // t ~ (log n)^{2/3}, k ~ (log n)^{1/3}
            double logN = Math.log(Math.max(3, n));
            tParam = Math.max(1, (int) Math.round(Math.pow(logN, 2.0 / 3.0)));
            kParam = Math.max(2, (int) Math.round(Math.pow(logN, 1.0 / 3.0)));
        }

        // Base case: l == 0
        if (l <= 0) {
            if (S.isEmpty()) {
                return new Result(B, Set.of());
            }
            var baseResult = BaseCase.baseCase(graph, dist, B, S, kParam, instr);
            return new Result(baseResult.BPrime(), baseResult.UoSet());
        }

        // FIND_PIVOTS: compute P, W
        int pLimit = Math.max(1, 1 << Math.min(10, tParam)); // 2^tParam, capped
        int kSteps = Math.max(1, kParam);
        var findPivotsResult = FindPivots.findPivots(graph, dist, S, B, n, kSteps, pLimit, instr);
        var P = findPivotsResult.P();
        var W = findPivotsResult.W();

        // Initialize DataStructure D
        int M = 1 << Math.max(0, (l - 1) * tParam); // 2^((l-1)*t)
        int blockSize = Math.max(1, Math.min(P.isEmpty() ? 1 : P.size(), 64));
        var D = new DataStructureD(M, B, blockSize);

        // Insert pivots into D
        for (int x : P) {
            D.insert(x, dist.getOrDefault(x, Double.POSITIVE_INFINITY));
        }

        double BPrimeInitial = P.stream()
                .mapToDouble(x -> dist.getOrDefault(x, Double.POSITIVE_INFINITY))
                .min()
                .orElse(B);

        var U = new HashSet<Integer>();
        var BPrimeSubValues = new ArrayList<Double>();

        // Main loop
        int loopGuard = 0;
        int limit = kParam * (1 << (l * Math.max(1, tParam)));

        while (U.size() < limit && !D.isEmpty()) {
            loopGuard++;
            if (loopGuard > 20000) {
                // Safety break for pathological cases
                break;
            }

            DataStructureD.PullResult pullResult;
            try {
                pullResult = D.pull();
            } catch (IllegalStateException e) {
                break;
            }

            double Bi = pullResult.Bi();
            var Si = pullResult.Si();

            // Recursive call
            var subResult = bmssp(graph, dist, edges, l - 1, Bi, Si, n, instr);
            double BPrimeSub = subResult.BPrime();
            var Ui = subResult.U();

            BPrimeSubValues.add(BPrimeSub);
            U.addAll(Ui);

            // Relax edges from Ui
            var KForBatch = new HashSet<DataStructureD.NodeKey>();

            for (int u : Ui) {
                double du = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
                if (!Double.isFinite(du)) {
                    continue;
                }

                for (var edge : graph.getNeighbors(u)) {
                    instr.incrementRelaxations();
                    int v = edge.to();
                    double wUV = edge.weight();
                    double newD = du + wUV;

                    // Accept equality per remark (<=) to allow reuse
                    if (newD <= dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                        dist.put(v, newD);
                        if (Bi <= newD && newD < B) {
                            D.insert(v, newD);
                        } else if (BPrimeSub <= newD && newD < Bi) {
                            KForBatch.add(new DataStructureD.NodeKey(v, newD));
                        }
                    }
                }
            }

            // Also include Si nodes whose distance falls into [BPrimeSub, Bi)
            for (int x : Si) {
                double dx = dist.getOrDefault(x, Double.POSITIVE_INFINITY);
                if (BPrimeSub <= dx && dx < Bi) {
                    KForBatch.add(new DataStructureD.NodeKey(x, dx));
                }
            }

            if (!KForBatch.isEmpty()) {
                D.batchPrepend(KForBatch);
            }
        }

        // Compute final B'
        double BPrimeFinal;
        if (!BPrimeSubValues.isEmpty()) {
            var allValues = new ArrayList<Double>();
            allValues.add(BPrimeInitial);
            allValues.addAll(BPrimeSubValues);
            BPrimeFinal = Collections.min(allValues);
        } else {
            BPrimeFinal = BPrimeInitial;
        }

        // Final U includes W nodes with distance < BPrimeFinal
        var UFinal = new HashSet<>(U);
        for (int x : W) {
            if (dist.getOrDefault(x, Double.POSITIVE_INFINITY) < BPrimeFinal) {
                UFinal.add(x);
            }
        }

        return new Result(BPrimeFinal, UFinal);
    }
}