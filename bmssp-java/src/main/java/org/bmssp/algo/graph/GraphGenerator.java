package org.bmssp.algo.graph;

import java.util.Random;

public class GraphGenerator {
    private final Random rand;
    public GraphGenerator(int seed) { this.rand = new Random(seed); }

    public Graph generateSparseDirectedGraph(int n, int m) { return generateSparseDirectedGraph(n, m, 100.0); }

    public Graph generateSparseDirectedGraph(int n, int m, double maxW) {
        Graph g = new Graph(n);
        // weak backbone to avoid isolated nodes
        for (int i = 1; i < n; i++) {
            int u = rand.nextInt(i);
            double w = 1.0 + rand.nextDouble() * (maxW - 1.0);
            g.addEdge(u, i, w);
        }
        int remaining = Math.max(0, m - (n - 1));
        for (int i = 0; i < remaining; i++) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);
            double w = 1.0 + rand.nextDouble() * (maxW - 1.0);
            g.addEdge(u, v, w);
        }
        return g;
    }
}
