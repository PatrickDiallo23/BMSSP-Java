# BMSSP Java Implementation

This project is a **Java 21 Maven implementation** of the [BMSSP (Bounded Multi-Source Shortest Path)](https://papers-pdfs.assets.alphaxiv.org/2504.17033v2.pdf) algorithm, based on the provided Python/Rust reference code. The project provides both the BMSSP algorithm and a standard Dijkstra's algorithm for comparison.


---

## Features

- Graph generation utilities (sparse directed graphs with random edges).
- Full **BMSSP recursive algorithm** with base cases, pivot selection, and data structure `D`.
- **Standard Dijkstra's Algorithm**: For performance comparison
- **Instrumentation**: Performance tracking for relaxations and heap operations
- **Randomized Test Harness**: Generate and test on various graph sizes
- **Maven Integration**: Proper project structure with dependencies
- **Java 21 Features**: Modern Java syntax and features where appropriate
- **Comprehensive Testing**: Unit tests covering various scenarios

---

## Requirements
- **Java 21** (ensure `java --version` reports 21)
- **Maven 3.6+**

---

## Build

Clone the repository and change directory:

```
git clone https://github.com/PatrickDiallo23/BMSSP-Java.git
cd BMSSP-Java/bmssp-java
```

Then run:

```bash
mvn clean compile
```

---

## Run the application

### Command Line Options

- `n, --nodes <NUM>`: Number of nodes (default: 200000)
- `m, --edges <NUM>`: Number of edges (default: 800000)
- `s, --seed <NUM>`: Random seed (default: 0)
- `-source <NUM>`: Source node (default: 0)
- `h, --help`: Show help message

### Example
```bash
# Run with default parameters (200k nodes, 800k edges)
mvn exec:java

# Run with custom parameters
mvn exec:java -Dexec.args="-n 50000 -m 200000 -s 42"

# See help
mvn exec:java -Dexec.args="--help"
```


---

## Output
The program reports timing and statistics for both algorithms, for example:

```
Generating graph: n=200000, m=800000, seed=0
Graph generated. avg out-degree ≈ 4.000
Dijkstra: time=0.453019s, relaxations=800000, heap_ops=532376, reachable=200000
BMSSP params: top-level l=2
BMSSP: time=0.161213s, relaxations=25545, reachable=8363, B'=0.0, |U_final|=2048
Distance agreement (max abs diff on commonly reachable nodes): 6.111340e+01
```

- **Dijkstra stats**: runtime, relaxations, heap operations, reachable nodes
- **BMSSP stats**: runtime, relaxations, reachable nodes, computed `B'`, and final set size
- **Agreement**: maximum absolute difference in distances between Dijkstra and BMSSP

---

## Project Structure
```
src/main/java/org/bmssp/algo/
 ├── Main.java                 # Entry point
 ├── Dijkstra.java             # Dijkstra implementation
 ├── BMSSP.java                # Recursive BMSSP implementation
 ├── FindPivots.java           # Pivot selection logic
 ├── BaseCase.java             # Base case solver
 ├── DataStructureD.java       # Specialized data structure for BMSSP
 ├── graph/
 │    ├── Graph.java           # Graph representation
 │    ├── GraphGenerator.java  # Random sparse graph generator
 │    └── Edge.java            # Immutable edge class
 └── util/
      └── Instrument.java      # Counters for relaxations & heap ops
```
---

## Algorithm Details

### BMSSP Algorithm

The BMSSP algorithm is a sophisticated shortest path algorithm that uses:

1. **Recursive Structure**: Divides the problem into smaller subproblems
2. **Pivot Selection**: Uses FIND_PIVOTS to select strategic nodes for exploration
3. **Bounded Exploration**: Limits search within distance bounds
4. **Specialized Data Structure**: Custom data structure D for efficient operations

### Key Components

- **BMSSP**: Main recursive function with depth parameter l
- **FIND_PIVOTS**: Bounded Bellman-Ford-like algorithm for pivot selection
- **BASECASE**: Dijkstra-like expansion for small problems
- **DataStructure D (Partial Queue)**: Supports insert, batch_prepend, and pull operations

### Parameters

The algorithm automatically chooses parameters based on graph size:

- `t ≈ (log n)^(2/3)`: Controls recursion branching
- `k ≈ (log n)^(1/3)`: Limits exploration depth
- `l`: Recursion depth, chosen heuristically

### Performance

The implementation includes comprehensive instrumentation to track:

- Number of edge relaxations
- Heap operations
- Execution time
- Distance accuracy compared to Dijkstra

---

## Testing

The project includes comprehensive tests:

- Unit tests for individual components
- Integration tests comparing BMSSP vs Dijkstra
- Random graph generation and testing
- Edge cases (single node, disconnected graphs)

Run specific tests:
```bash
# Run all tests
mvn test

# Run a specific test
mvn test -Dtest=BMSSPTest#testSimpleGraph

```
bash

### Performance Notes

- For small graphs (n < 1000), Dijkstra is typically faster due to overhead
- For large sparse graphs, BMSSP may show theoretical advantages
- Performance depends heavily on graph structure and parameter tuning
- The implementation prioritizes correctness and clarity over micro-optimizations

---

## Notes
- This is inspired from the existing [Python implementation of the algorithm](https://github.com/sidharthpunathil/fastest-shortest-path-algo-poc/tree/main), [Rust implementation](https://github.com/lucas-montes/bmssp), and [a Medium article](https://medium.com/@adnanmasood/breaking-the-sorting-barrier-why-a-new-shortest-path-algorithm-matters-even-if-you-still-love-7bad07c71b88).
- Designed for **research/educational use**, not optimized for production-scale shortest paths (yet).
- BMSSP recursion parameters (`l`) are automatically chosen based on `n`.
- You may need to adjust JVM memory settings (`-Xmx4g` or higher) for very large graphs.
- You are free to contribute to this repository.

---

## License
MIT License (free to use, modify, and distribute).
