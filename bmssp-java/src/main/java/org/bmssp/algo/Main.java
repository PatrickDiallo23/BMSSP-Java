package org.bmssp.algo;

import org.bmssp.algo.testing.TestRunner;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * Main CLI application for running BMSSP vs Dijkstra comparison
 */
@Command(name = "bmssp", description = "BMSSP (practical) vs Dijkstra - full implementation",
        mixinStandardHelpOptions = true, version = "1.0")
public class Main implements Callable<Integer> {

    @Option(names = {"-n", "--nodes"}, description = "Number of nodes (default: ${DEFAULT-VALUE})")
    private int nodes = 200000;

    @Option(names = {"-m", "--edges"}, description = "Number of edges (default: ${DEFAULT-VALUE})")
    private int edges = 800000;

    @Option(names = {"-s", "--seed"}, description = "Random seed (default: ${DEFAULT-VALUE})")
    private int seed = 0;

    @Option(names = {"--source"}, description = "Source node (default: ${DEFAULT-VALUE})")
    private int source = 0;

    @Override
    public Integer call() {
        try {
            System.out.println("BMSSP vs Dijkstra Comparison");
            System.out.println("============================");

            var result = TestRunner.runSingleTest(nodes, edges, seed, source);

            System.out.println();
            System.out.println("Test Summary:");
            System.out.println("=============");
            System.out.printf("Graph: n=%d, m=%d, seed=%d%n", result.n(), result.m(), result.seed());
            System.out.printf("Dijkstra  - Time: %.6fs, Relaxations: %d, Reachable: %d%n",
                    result.dijkstraTime(), result.dijkstraRelaxations(), result.dijkstraReachable());
            System.out.printf("BMSSP     - Time: %.6fs, Relaxations: %d, Reachable: %d%n",
                    result.bmsspTime(), result.bmsspRelaxations(), result.bmsspReachable());
            System.out.printf("Max Distance Difference: %.6e%n", result.maxDiff());

            double speedup = result.dijkstraTime() / result.bmsspTime();
            System.out.printf("Speedup: %.2fx %s%n", Math.abs(speedup),
                    speedup > 1 ? "(BMSSP faster)" : "(Dijkstra faster)");

            return 0;
        } catch (Exception e) {
            System.err.println("Error during execution:" + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}