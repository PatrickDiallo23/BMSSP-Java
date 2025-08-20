package org.bmssp.algo.testing;

import org.bmssp.algo.BMSSP;
import org.bmssp.algo.Dijkstra;
import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;

import java.util.*;

/**
 * Test harness for comparing BMSSP and Dijkstra algorithms
 */
public class TestRunner {

    public record TestResult(
            int n, int m, int seed,
            double dijkstraTime, long dijkstraRelaxations,
            double bmsspTime, long bmsspRelaxations,
            long dijkstraReachable, long bmsspReachable,
            double maxDiff
    ) {}

    /**
     * Run a single test comparing BMSSP and Dijkstra
     */
    public static TestResult runSingleTest(int n, int m, int seed, int source) {
        System.out.printf("Generating graph: n=%d, m=%d, seed=%d%n", n, m, seed);

        var random = new Random(seed);
        var generated = Graph.generate(n, m, 100.0, random);
        var graph = generated.graph();
        var edges = generated.edges();

        double avgDeg = graph.getAverageOutDegree();
        System.out.printf("Graph generated. avg out-degree ≈ %.3f%n", avgDeg);

        // Run Dijkstra
        var instrDij = new Instrument();
        long start = System.nanoTime();
        var distDij = Dijkstra.shortestPaths(graph, source, instrDij);
        long end = System.nanoTime();
        double dijkstraTime = (end - start) / 1_000_000_000.0;

        long dijkstraReachable = distDij.values().stream()
                .mapToLong(d -> Double.isFinite(d) ? 1 : 0)
                .sum();

        System.out.printf("Dijkstra: time=%.6fs, relaxations=%d, heap_ops=%d, reachable=%d%n",
                dijkstraTime, instrDij.getRelaxations(), instrDij.getHeapOps(), dijkstraReachable);

        // Run BMSSP
        var distBM = new HashMap<Integer, Double>();
        for (int node : graph.getNodes()) {
            distBM.put(node, Double.POSITIVE_INFINITY);
        }
        distBM.put(source, 0.0);

        var instrBM = new Instrument();

        // Choose top-level recursion depth l heuristically
        int l;
        if (n <= 2) {
            l = 1;
        } else {
            double logN = Math.log(Math.max(3, n));
            int tGuess = Math.max(1, (int) Math.round(Math.pow(logN, 2.0 / 3.0)));
            l = Math.max(1, (int) Math.round(logN / tGuess));
        }

        System.out.printf("BMSSP params: top-level l=%d%n", l);

        start = System.nanoTime();
        var bmsspResult = BMSSP.bmssp(graph, distBM, edges, l, Double.POSITIVE_INFINITY,
                Set.of(source), n, instrBM);
        end = System.nanoTime();
        double bmsspTime = (end - start) / 1_000_000_000.0;

        double Bp = bmsspResult.BPrime();
        var UFinal = bmsspResult.U();

        long bmsspReachable = distBM.values().stream()
                .mapToLong(d -> Double.isFinite(d) ? 1 : 0)
                .sum();

        System.out.printf("BMSSP: time=%.6fs, relaxations=%d, reachable=%d, B'=%.6f, |U_final|=%d%n",
                bmsspTime, instrBM.getRelaxations(), bmsspReachable, Bp, UFinal.size());

        // Compare distances for commonly reachable nodes
        var diffs = new ArrayList<Double>();
        for (int node : graph.getNodes()) {
            double dv = distDij.getOrDefault(node, Double.POSITIVE_INFINITY);
            double db = distBM.getOrDefault(node, Double.POSITIVE_INFINITY);
            if (Double.isFinite(dv) && Double.isFinite(db)) {
                diffs.add(Math.abs(dv - db));
            }
        }

        double maxDiff = diffs.isEmpty() ? 0.0 : Collections.max(diffs);
        System.out.printf("Distance agreement (max abs diff on commonly reachable nodes): %.6e%n", maxDiff);

        return new TestResult(
                n, m, seed,
                dijkstraTime, instrDij.getRelaxations(),
                bmsspTime, instrBM.getRelaxations(),
                dijkstraReachable, bmsspReachable,
                maxDiff
        );
    }
}
