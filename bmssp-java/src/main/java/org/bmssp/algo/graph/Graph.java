package org.bmssp.algo.graph;

import java.util.*;

public class Graph {
    public record Neighbor(int v, double w) {}

    private final int n;
    private final Map<Integer, List<Neighbor>> adj;
    private final List<Edge> edges;

    public Graph(int n) {
        this.n = n;
        this.adj = new HashMap<>();
        this.edges = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.put(i, new ArrayList<>());
    }

    public void addEdge(int u, int v, double w) {
        adj.get(u).add(new Neighbor(v, w));
        edges.add(new Edge(u, v, w));
    }

    public int size() { return n; }
    public Map<Integer, List<Neighbor>> getAdj() { return adj; }
    public List<Edge> getEdges() { return edges; }
}
