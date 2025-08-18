package org.bmssp.algo;

import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.graph.GraphGenerator;
import org.bmssp.algo.util.Instrument;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        int n = 200000;
        int m = 800000;
        int seed = 0;
        int source = 0;

        // Allow CLI overrides
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-n", "--nodes" -> n = Integer.parseInt(args[++i]);
                case "-m", "--edges" -> m = Integer.parseInt(args[++i]);
                case "-s", "--seed" -> seed = Integer.parseInt(args[++i]);
                default -> {}
            }
        }

        System.out.printf("Generating graph: n=%d, m=%d, seed=%d%n", n, m, seed);
        GraphGenerator generator = new GraphGenerator(seed);
        Graph graph = generator.generateSparseDirectedGraph(n, m, 100.0);
        double avgOut = graph.getAdj().values().stream().mapToInt(List::size).average().orElse(0.0);
        System.out.printf("Graph generated. avg out-degree ≈ %.3f%n", avgOut);

        // Dijkstra timing
        Instrument instrDij = new Instrument();
        long t0 = System.nanoTime();
        Map<Integer, Double> distDij = Dijkstra.run(graph, source, instrDij);
        long t1 = System.nanoTime();
        long reachableDij = distDij.values().stream().filter(Double::isFinite).count();
        System.out.printf("Dijkstra: time=%.6fs, relaxations=%d, heap_ops=%d, reachable=%d%n",
                (t1 - t0) / 1e9,
                instrDij.getRelaxations(),
                instrDij.getHeapOps(),
                reachableDij);

        // BMSSP practical
        Map<Integer, Double> distBm = new HashMap<>();
        for (int v = 0; v < n; v++) distBm.put(v, Double.POSITIVE_INFINITY);
        distBm.put(source, 0.0);
        Instrument instrBm = new Instrument();
        int l;
        if (n <= 2) {
            l = 1;
        } else {
            int tGuess = Math.max(1, (int) Math.round(Math.pow(Math.log(Math.max(3, n)), 2.0 / 3.0)));
            l = Math.max(1, (int) Math.round(Math.log(Math.max(3, n)) / Math.max(1, tGuess)));
        }
        System.out.printf("BMSSP params: top-level l=%d%n", l);
        t0 = System.nanoTime();
        BMSSP.Result res = BMSSP.run(
                graph,
                distBm,
                graph.getEdges(),
                l,
                Double.POSITIVE_INFINITY,
                Set.of(source),
                n,
                instrBm
        );
        t1 = System.nanoTime();
        long reachableBm = distBm.values().stream().filter(Double::isFinite).count();
        System.out.printf("BMSSP: time=%.6fs, relaxations=%d, reachable=%d, B'=%s, |U_final|=%d%n",
                (t1 - t0) / 1e9,
                instrBm.getRelaxations(),
                reachableBm,
                Double.toString(res.Bprime()),
                res.Ufinal().size());

        double maxDiff = 0.0;
        for (int v : graph.getAdj().keySet()) {
            double dv = distDij.getOrDefault(v, Double.POSITIVE_INFINITY);
            double db = distBm.getOrDefault(v, Double.POSITIVE_INFINITY);
            if (Double.isFinite(dv) && Double.isFinite(db)) {
                maxDiff = Math.max(maxDiff, Math.abs(dv - db));
            }
        }
        System.out.printf("Distance agreement (max abs diff on commonly reachable nodes): %.6e%n", maxDiff);
    }
}
