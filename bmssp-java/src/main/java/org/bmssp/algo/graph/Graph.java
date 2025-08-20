package org.bmssp.algo.graph;

import java.util.*;

/**
 * Core graph data structures and utilities
 */
public class Graph {

    public record Edge(int from, int to, double weight) {}

    public record WeightedEdge(int to, double weight) {}

    private final Map<Integer, List<WeightedEdge>> adjacencyList;
    private final int nodeCount;

    /**
     * Simple adjacency-list graph for directed graphs with non-negative edge weights.
     */
    public Graph(int nodeCount) {
        this.nodeCount = nodeCount;
        this.adjacencyList = new HashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            adjacencyList.put(i, new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, double weight) {
        adjacencyList.get(from).add(new WeightedEdge(to, weight));
    }

    public List<WeightedEdge> getNeighbors(int node) {
        return adjacencyList.getOrDefault(node, List.of());
    }

    public Set<Integer> getNodes() {
        return adjacencyList.keySet();
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public double getAverageOutDegree() {
        return adjacencyList.values().stream()
                .mapToInt(List::size)
                .average()
                .orElse(0.0);
    }

    /**
     * Generate a sparse directed graph with weak connectivity backbone
     */
    public static GeneratedGraph generate(int n, int m, double maxWeight, Random random) {
        var graph = new Graph(n);
        var edges = new ArrayList<Edge>();

        // Create weak backbone to avoid isolated nodes
        for (int i = 1; i < n; i++) {
            int u = random.nextInt(i);
            double w = random.nextDouble() * (maxWeight - 1.0) + 1.0;
            graph.addEdge(u, i, w);
            edges.add(new Edge(u, i, w));
        }

        // Add remaining edges
        int remaining = Math.max(0, m - (n - 1));
        for (int i = 0; i < remaining; i++) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            double w = random.nextDouble() * (maxWeight - 1.0) + 1.0;
            graph.addEdge(u, v, w);
            edges.add(new Edge(u, v, w));
        }

        return new GeneratedGraph(graph, edges);
    }

    public record GeneratedGraph(Graph graph, List<Edge> edges) {}
}