# Algorithm Visualizer

A Java Swing application for comparing sorting and shortest-path algorithms, developed from the CPT204 Team 154 coursework project.

The sorting view animates Bubble, Quick, and Merge Sort on three candidate-location datasets. The routing view compares Dijkstra and bidirectional Dijkstra on an undirected weighted graph, including routes through ordered waypoints. Console reports include the top ten candidates, route costs, and measured execution times.

## Requirements

JDK 17 or later. The application has no third-party Java dependencies.

## Run

macOS / Linux:

```sh
sh run.sh
sh run.sh --console
```

Windows:

```bat
run.bat
run.bat --console
```

The launchers build the sources and set the working directory so the CSV files can be found. To build manually:

```sh
sh build.sh
java -jar build/algorithm-visualizer.jar --console
```

An optional Maven build is available with `mvn package`; run `java -jar target/algorithm-visualizer-1.0.0.jar` from the repository root.

## Validation

```sh
sh test.sh
```

The checks compare both route algorithms over 100 seeded random graphs (32,400 source/destination pairs), compare every sorted dataset against Java's comparator-based sort, reject invalid graph weights, and check missing-file handling.

## Structure

- `src/main/java/graph/`: graph representation and route algorithms
- `src/main/java/sorting/`: sorting algorithms and ordering rules
- `src/main/java/service/`: application and benchmark services
- `src/main/java/visualization/`: Swing panels and animation
- `data/`: candidate lists and weighted graph edges
- `tests/`: dependency-free regression checks

## Benchmark interpretation

Each measured run sorts one fresh copy. Caller-specified warm-up and measurement counts are honored; no benchmark runs during class initialization. Results vary with JIT compilation, CPU load, and dataset size. Variance across different datasets describes workload differences and is not a repeated-run confidence interval. These timings are for demonstration rather than a JMH benchmark.

## Provenance

Developed as part of the CPT204 Team 154 coursework project. This repository retains the coursework algorithms and UI, with a standardized project layout, build and launch scripts, regression checks, and benchmark setup.
