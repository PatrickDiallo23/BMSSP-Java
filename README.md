# BMSSP Java Implementation

This project is a **Java 21 Maven implementation** of the [BMSSP (Bounded Multi-Source Shortest Path)](https://papers-pdfs.assets.alphaxiv.org/2504.17033v2.pdf) algorithm, based on the provided Python reference code.

It includes:
- Graph generation utilities (sparse directed graphs with random edges).
- Standard **Dijkstra's algorithm** for benchmarking.
- Full **BMSSP recursive algorithm** with base cases, pivot selection, and data structure `D`.
- Instrumentation counters for relaxations and heap operations.

---

## Requirements
- **Java 21** (ensure `java --version` reports 21)
- **Maven 3.8+**

---

## Build

Clone the repository and run:

```bash
mvn clean package -DskipTests
```

This will produce a runnable JAR in `target/`.

---

## Run

The program accepts the following arguments:
- `-n <int>` : number of vertices (default: 2000)
- `-m <int>` : number of edges (default: 8000)
- `-s <int>` : random seed (default: 0)
- `-src <int>` : source vertex (default: 0)

### Example: small test graph
```bash
java -jar target/bmssp-java-1.0.0.jar -n 2000 -m 8000 -s 0 -src 0
```

### Example: large benchmark
```bash
java -jar target/bmssp-java-1.0.0.jar -n 200000 -m 800000 -s 42 -src 0
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

## Notes
- This is inspired from the existing [Python implementation of the algorithm](https://github.com/sidharthpunathil/fastest-shortest-path-algo-poc/tree/main), [Rust implementation](https://github.com/lucas-montes/bmssp), and [a Medium article](https://medium.com/@adnanmasood/breaking-the-sorting-barrier-why-a-new-shortest-path-algorithm-matters-even-if-you-still-love-7bad07c71b88).
- Designed for **research/educational use**, not optimized for production-scale shortest paths (yet).
- BMSSP recursion parameters (`l`) are automatically chosen based on `n`.
- You may need to adjust JVM memory settings (`-Xmx4g` or higher) for very large graphs.
- You are free to contribute to this repository.

---

## License
MIT License (free to use, modify, and distribute).