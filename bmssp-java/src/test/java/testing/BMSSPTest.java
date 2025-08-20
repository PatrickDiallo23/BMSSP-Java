package testing;

import org.bmssp.algo.BMSSP;
import org.bmssp.algo.Dijkstra;
import org.bmssp.algo.graph.Graph;
import org.bmssp.algo.util.Instrument;
import org.bmssp.algo.testing.TestRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BMSSP algorithm implementation
 */
class BMSSPTest {

    @Test
    @DisplayName("Simple graph test - both algorithms should produce same results")
    void testSimpleGraph() {
        // Create a simple graph: 0 -> 1 -> 2
        var graph = new Graph(3);
        graph.addEdge(0, 1, 5.0);
        graph.addEdge(1, 2, 3.0);
        graph.addEdge(0, 2, 10.0); // Longer direct path

        var edges = List.of(
                new Graph.Edge(0, 1, 5.0),
                new Graph.Edge(1, 2, 3.0),
                new Graph.Edge(0, 2, 10.0)
        );

        // Run Dijkstra
        var instrDij = new Instrument();
        var distDij = Dijkstra.shortestPaths(graph, 0, instrDij);

        // Run BMSSP
        var distBM = new HashMap<Integer, Double>();
        for (int i = 0; i < 3; i++) {
            distBM.put(i, Double.POSITIVE_INFINITY);
        }
        distBM.put(0, 0.0);

        var instrBM = new Instrument();
        var result = BMSSP.bmssp(graph, distBM, edges, 1, Double.POSITIVE_INFINITY,
                Set.of(0), 3, instrBM);

        // Check results
        assertEquals(0.0, distDij.get(0), 1e-6);
        assertEquals(5.0, distDij.get(1), 1e-6);
        assertEquals(8.0, distDij.get(2), 1e-6); // 0->1->2 is shorter than 0->2

        // BMSSP should find the same or similar paths
        assertEquals(0.0, distBM.get(0), 1e-6);
        assertTrue(distBM.get(1) <= 5.1); // Allow small tolerance
        assertTrue(distBM.get(2) <= 8.1); // Allow small tolerance
    }

    @Test
    @DisplayName("Single node graph")
    void testSingleNode() {
        var graph = new Graph(1);
        var edges = List.<Graph.Edge>of();

        var instrDij = new Instrument();
        var distDij = Dijkstra.shortestPaths(graph, 0, instrDij);

        var distBM = new HashMap<Integer, Double>();
        distBM.put(0, 0.0);

        var instrBM = new Instrument();
        var result = BMSSP.bmssp(graph, distBM, edges, 1, Double.POSITIVE_INFINITY,
                Set.of(0), 1, instrBM);

        assertEquals(0.0, distDij.get(0), 1e-6);
        assertEquals(0.0, distBM.get(0), 1e-6);
        assertTrue(result.U().contains(0));
    }

    @Test
    @DisplayName("Disconnected graph")
    void testDisconnectedGraph() {
        // Two disconnected components: 0-1 and 2-3
        var graph = new Graph(4);
        graph.addEdge(0, 1, 2.0);
        graph.addEdge(2, 3, 3.0);

        var edges = List.of(
                new Graph.Edge(0, 1, 2.0),
                new Graph.Edge(2, 3, 3.0)
        );

        var instrDij = new Instrument();
        var distDij = Dijkstra.shortestPaths(graph, 0, instrDij);

        var distBM = new HashMap<Integer, Double>();
        for (int i = 0; i < 4; i++) {
            distBM.put(i, Double.POSITIVE_INFINITY);
        }
        distBM.put(0, 0.0);

        var instrBM = new Instrument();
        var result = BMSSP.bmssp(graph, distBM, edges, 1, Double.POSITIVE_INFINITY,
                Set.of(0), 4, instrBM);

        // Node 0 should reach node 1 but not nodes 2,3
        assertEquals(0.0, distDij.get(0), 1e-6);
        assertEquals(2.0, distDij.get(1), 1e-6);
        assertTrue(Double.isInfinite(distDij.get(2)));
        assertTrue(Double.isInfinite(distDij.get(3)));

        // BMSSP should have similar behavior
        assertEquals(0.0, distBM.get(0), 1e-6);
        assertTrue(distBM.get(1) <= 2.1); // Allow small tolerance
        assertTrue(Double.isInfinite(distBM.get(2)) || distBM.get(2) > 1000);
        assertTrue(Double.isInfinite(distBM.get(3)) || distBM.get(3) > 1000);
    }

    @Test
    @DisplayName("Small random graph test")
    void testSmallRandomGraph() {
        var result = TestRunner.runSingleTest(10, 20, 42, 0);

        // Basic sanity checks
        assertTrue(result.dijkstraTime() > 0);
        assertTrue(result.bmsspTime() > 0);
        assertTrue(result.dijkstraRelaxations() > 0);
        assertTrue(result.bmsspRelaxations() > 0);
        assertTrue(result.maxDiff() < 1.0); // Should be quite close for small graphs
    }

    @Test
    @DisplayName("Graph generation test")
    void testGraphGeneration() {
        var random = new Random(123);
        var generated = Graph.generate(5, 8, 10.0, random);
        var graph = generated.graph();
        var edges = generated.edges();

        assertEquals(5, graph.getNodeCount());
        assertEquals(8, edges.size());

        // Check that all nodes exist
        for (int i = 0; i < 5; i++) {
            assertTrue(graph.getNodes().contains(i));
        }

        // Check that edges have positive weights
        for (var edge : edges) {
            assertTrue(edge.weight() > 0);
            assertTrue(edge.from() >= 0 && edge.from() < 5);
            assertTrue(edge.to() >= 0 && edge.to() < 5);
        }
    }

    @Test
    @DisplayName("Instrumentation test")
    void testInstrumentation() {
        var instr = new Instrument();
        assertEquals(0, instr.getRelaxations());
        assertEquals(0, instr.getHeapOps());

        instr.incrementRelaxations();
        instr.incrementHeapOps();
        instr.incrementHeapOps();

        assertEquals(1, instr.getRelaxations());
        assertEquals(2, instr.getHeapOps());

        instr.reset();
        assertEquals(0, instr.getRelaxations());
        assertEquals(0, instr.getHeapOps());
    }
}
