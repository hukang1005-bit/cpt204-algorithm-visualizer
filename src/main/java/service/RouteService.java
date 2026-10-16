package service;

import graph.BidirectionalDijkstraAlgorithm;
import graph.DijkstraAlgorithm;
import graph.Graph;

import model.Path;
import model.RouteResult;

import java.util.ArrayList;
import java.util.List;

public class RouteService {
    public Graph buildGraph(List<Path> paths) {
        Graph graph = new Graph();
        for (Path path : paths) {
            graph.addEdge(path.getFromLocationId(), path.getToLocationId(), path.getWeight());
        }
        return graph;
    }

    public RouteResult findShortestPath(Graph graph, String start, String destination) {
        return DijkstraAlgorithm.findShortestPath(graph, start, destination);
    }

    public RouteResult findShortestPathBidirectional(
            Graph graph, String start, String destination) {
        return BidirectionalDijkstraAlgorithm.findShortestPath(graph, start, destination);
    }

    public RouteResult findShortestPathVia(
            Graph graph, String start, String destination, String... waypoints) {
        List<String> requiredStops = new ArrayList<>();
        requiredStops.add(start);
        for (String waypoint : waypoints) {
            requiredStops.add(waypoint);
        }
        requiredStops.add(destination);

        List<String> combinedPath = new ArrayList<>();
        double totalCost = 0;

        for (int i = 0; i < requiredStops.size() - 1; i++) {
            RouteResult segment =
                    findShortestPath(graph, requiredStops.get(i), requiredStops.get(i + 1));
            if (!segment.isReachable()) {
                return RouteResult.unreachable(start, destination);
            }

            if (combinedPath.isEmpty()) {
                combinedPath.addAll(segment.getPath());
            } else {
                combinedPath.addAll(segment.getPath().subList(1, segment.getPath().size()));
            }
            totalCost += segment.getTotalCost();
        }

        return new RouteResult(start, destination, combinedPath, totalCost);
    }

    public RouteResult findShortestPathViaBidirectional(
            Graph graph, String start, String destination, String... waypoints) {
        List<String> requiredStops = new ArrayList<>();
        requiredStops.add(start);
        for (String waypoint : waypoints) {
            requiredStops.add(waypoint);
        }
        requiredStops.add(destination);

        List<String> combinedPath = new ArrayList<>();
        double totalCost = 0;

        for (int i = 0; i < requiredStops.size() - 1; i++) {
            RouteResult segment =
                    findShortestPathBidirectional(
                            graph, requiredStops.get(i), requiredStops.get(i + 1));
            if (!segment.isReachable()) {
                return RouteResult.unreachable(start, destination);
            }

            if (combinedPath.isEmpty()) {
                combinedPath.addAll(segment.getPath());
            } else {
                combinedPath.addAll(segment.getPath().subList(1, segment.getPath().size()));
            }
            totalCost += segment.getTotalCost();
        }

        return new RouteResult(start, destination, combinedPath, totalCost);
    }

    public RouteBenchmarkResult benchmarkCase(
            String name,
            Graph graph,
            String start,
            String destination,
            String[] waypoints,
            int warmUpRuns,
            int measuredRuns) {
        if (warmUpRuns < 0 || measuredRuns < 1) {
            throw new IllegalArgumentException("Invalid benchmark run count.");
        }
        // Correctness verification
        RouteResult originalRes;
        RouteResult bidirectionalRes;
        if (waypoints == null || waypoints.length == 0) {
            originalRes = findShortestPath(graph, start, destination);
            bidirectionalRes = findShortestPathBidirectional(graph, start, destination);
        } else {
            originalRes = findShortestPathVia(graph, start, destination, waypoints);
            bidirectionalRes =
                    findShortestPathViaBidirectional(graph, start, destination, waypoints);
        }

        boolean correct = originalRes.isReachable() == bidirectionalRes.isReachable();
        if (correct && originalRes.isReachable()) {
            correct = Math.abs(originalRes.getTotalCost() - bidirectionalRes.getTotalCost()) < 1e-6;
        }

        // Timing Original
        for (int i = 0; i < warmUpRuns; i++) {
            if (waypoints == null || waypoints.length == 0) {
                findShortestPath(graph, start, destination);
            } else {
                findShortestPathVia(graph, start, destination, waypoints);
            }
        }
        long startTime = System.nanoTime();
        for (int i = 0; i < measuredRuns; i++) {
            if (waypoints == null || waypoints.length == 0) {
                findShortestPath(graph, start, destination);
            } else {
                findShortestPathVia(graph, start, destination, waypoints);
            }
        }
        long endTime = System.nanoTime();
        double originalAvgTimeUs = ((double) (endTime - startTime) / 1000.0) / measuredRuns;

        // Timing Bidirectional
        for (int i = 0; i < warmUpRuns; i++) {
            if (waypoints == null || waypoints.length == 0) {
                findShortestPathBidirectional(graph, start, destination);
            } else {
                findShortestPathViaBidirectional(graph, start, destination, waypoints);
            }
        }
        startTime = System.nanoTime();
        for (int i = 0; i < measuredRuns; i++) {
            if (waypoints == null || waypoints.length == 0) {
                findShortestPathBidirectional(graph, start, destination);
            } else {
                findShortestPathViaBidirectional(graph, start, destination, waypoints);
            }
        }
        endTime = System.nanoTime();
        double bidirectionalAvgTimeUs = ((double) (endTime - startTime) / 1000.0) / measuredRuns;

        return new RouteBenchmarkResult(name, originalAvgTimeUs, bidirectionalAvgTimeUs, correct);
    }

    public static class RouteBenchmarkResult {
        private final String caseName;
        private final double originalAvgTimeUs;
        private final double bidirectionalAvgTimeUs;
        private final boolean correct;

        public RouteBenchmarkResult(
                String caseName,
                double originalAvgTimeUs,
                double bidirectionalAvgTimeUs,
                boolean correct) {
            this.caseName = caseName;
            this.originalAvgTimeUs = originalAvgTimeUs;
            this.bidirectionalAvgTimeUs = bidirectionalAvgTimeUs;
            this.correct = correct;
        }

        public String getCaseName() {
            return caseName;
        }

        public double getOriginalAvgTimeUs() {
            return originalAvgTimeUs;
        }

        public double getBidirectionalAvgTimeUs() {
            return bidirectionalAvgTimeUs;
        }

        public boolean isCorrect() {
            return correct;
        }

        public double getSpeedup() {
            if (bidirectionalAvgTimeUs == 0) return 1.0;
            return originalAvgTimeUs / bidirectionalAvgTimeUs;
        }
    }
}
