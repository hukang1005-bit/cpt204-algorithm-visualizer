package graph;

import model.Edge;
import model.RouteResult;

import java.util.*;

public class BidirectionalDijkstraAlgorithm {
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

        Map<String, Double> forwardDist = new HashMap<>();
        Map<String, Double> backwardDist = new HashMap<>();
        Map<String, String> forwardParents = new HashMap<>();
        Map<String, String> backwardParents = new HashMap<>();
        Set<String> forwardVisited = new HashSet<>();
        Set<String> backwardVisited = new HashSet<>();

        PriorityQueue<NodeDistance> forwardQueue =
                new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::getDistance));
        PriorityQueue<NodeDistance> backwardQueue =
                new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::getDistance));

        for (String node : graph.getAllNodes()) {
            forwardDist.put(node, Double.POSITIVE_INFINITY);
            backwardDist.put(node, Double.POSITIVE_INFINITY);
        }

        forwardDist.put(fromLocation, 0.0);
        backwardDist.put(toLocation, 0.0);

        forwardQueue.add(new NodeDistance(fromLocation, 0.0));
        backwardQueue.add(new NodeDistance(toLocation, 0.0));

        double bestPathCost = Double.POSITIVE_INFINITY;
        String intersectionNode = null;

        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {
            // Stopping condition: when the minimum distance in both queues meets or exceeds the
            // best path cost
            if (forwardQueue.peek().getDistance() + backwardQueue.peek().getDistance()
                    >= bestPathCost) {
                break;
            }

            // Expand the queue with the smaller top element to minimize search space
            if (forwardQueue.peek().getDistance() <= backwardQueue.peek().getDistance()) {
                NodeDistance current = forwardQueue.poll();
                String u = current.getLocation();

                if (forwardVisited.contains(u)) {
                    continue;
                }
                forwardVisited.add(u);

                for (Edge edge : graph.getNeighbors(u)) {
                    String v = edge.getToLocation();
                    double weight = edge.getWeight();
                    double newDist = forwardDist.get(u) + weight;

                    if (newDist < forwardDist.get(v)) {
                        forwardDist.put(v, newDist);
                        forwardParents.put(v, u);
                        forwardQueue.add(new NodeDistance(v, newDist));

                        // Check if v is already reached by the backward search
                        if (backwardDist.get(v) < Double.POSITIVE_INFINITY) {
                            double totalDist = newDist + backwardDist.get(v);
                            if (totalDist < bestPathCost) {
                                bestPathCost = totalDist;
                                intersectionNode = v;
                            }
                        }
                    }
                }
            } else {
                NodeDistance current = backwardQueue.poll();
                String u = current.getLocation();

                if (backwardVisited.contains(u)) {
                    continue;
                }
                backwardVisited.add(u);

                for (Edge edge : graph.getNeighbors(u)) {
                    String v = edge.getToLocation();
                    double weight = edge.getWeight();
                    double newDist = backwardDist.get(u) + weight;

                    if (newDist < backwardDist.get(v)) {
                        backwardDist.put(v, newDist);
                        backwardParents.put(v, u);
                        backwardQueue.add(new NodeDistance(v, newDist));

                        // Check if v is already reached by the forward search
                        if (forwardDist.get(v) < Double.POSITIVE_INFINITY) {
                            double totalDist = newDist + forwardDist.get(v);
                            if (totalDist < bestPathCost) {
                                bestPathCost = totalDist;
                                intersectionNode = v;
                            }
                        }
                    }
                }
            }
        }

        if (bestPathCost == Double.POSITIVE_INFINITY) {
            return RouteResult.unreachable(fromLocation, toLocation);
        }

        List<String> path =
                buildBidirectionalPath(
                        forwardParents,
                        backwardParents,
                        fromLocation,
                        toLocation,
                        intersectionNode);
        return new RouteResult(fromLocation, toLocation, path, bestPathCost);
    }

    private static List<String> buildBidirectionalPath(
            Map<String, String> forwardParents,
            Map<String, String> backwardParents,
            String fromLocation,
            String toLocation,
            String intersectionNode) {

        LinkedList<String> path = new LinkedList<>();
        if (intersectionNode == null) {
            return path;
        }

        // Trace forward from the intersection node back to fromLocation
        String current = intersectionNode;
        while (current != null) {
            path.addFirst(current);
            if (current.equals(fromLocation)) {
                break;
            }
            current = forwardParents.get(current);
        }

        // Trace backward from the intersection node's backward parent to toLocation
        current = backwardParents.get(intersectionNode);
        while (current != null) {
            path.addLast(current);
            if (current.equals(toLocation)) {
                break;
            }
            current = backwardParents.get(current);
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
