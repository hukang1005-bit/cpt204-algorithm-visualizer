package graph;

import model.Edge;
import model.RouteResult;

import java.util.*;

public class DijkstraAlgorithm {
    public static RouteResult findShortestPath(
            Graph graph, String fromLocation, String toLocation) {
        if (!graph.getAllNodes().contains(fromLocation)
                || !graph.getAllNodes().contains(toLocation)) {
            return RouteResult.unreachable(fromLocation, toLocation);
        }

        if (fromLocation.equals(toLocation)) {
            return new RouteResult(
                    fromLocation, toLocation, Collections.singletonList(fromLocation), 0.0);
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> parents = new HashMap<>();
        PriorityQueue<NodeDistance> queue =
                new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::getDistance));

        for (String node : graph.getAllNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }

        distances.put(fromLocation, 0.0);
        queue.add(new NodeDistance(fromLocation, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            if (current.getDistance() > distances.get(current.getLocation())) {
                continue;
            }

            if (current.getLocation().equals(toLocation)) {
                break;
            }

            for (Edge edge : graph.getNeighbors(current.getLocation())) {
                double newDistance = current.getDistance() + edge.getWeight();
                if (newDistance < distances.get(edge.getToLocation())) {
                    distances.put(edge.getToLocation(), newDistance);
                    parents.put(edge.getToLocation(), current.getLocation());
                    queue.add(new NodeDistance(edge.getToLocation(), newDistance));
                }
            }
        }

        double totalCost = distances.get(toLocation);
        if (totalCost == Double.POSITIVE_INFINITY) {
            return RouteResult.unreachable(fromLocation, toLocation);
        }

        return new RouteResult(
                fromLocation, toLocation, buildPath(parents, fromLocation, toLocation), totalCost);
    }

    private static List<String> buildPath(
            Map<String, String> parents, String fromLocation, String toLocation) {
        LinkedList<String> path = new LinkedList<>();
        String current = toLocation;
        path.addFirst(current);

        while (!current.equals(fromLocation)) {
            current = parents.get(current);
            if (current == null) {
                return Collections.emptyList();
            }
            path.addFirst(current);
        }

        return path;
    }

    private static class NodeDistance {
        private final String location;
        private final double distance;

        NodeDistance(String location, double distance) {
            this.location = location;
            this.distance = distance;
        }

        String getLocation() {
            return location;
        }

        double getDistance() {
            return distance;
        }
    }
}
