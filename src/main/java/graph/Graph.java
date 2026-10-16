package graph;

import model.Edge;

import java.util.*;

public class Graph {
    private final Set<String> locations;
    private final Map<String, List<Edge>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
        locations = new HashSet<>();
    }

    public List<Edge> getNeighbors(String location) {
        return Collections.unmodifiableList(
                adjacencyList.getOrDefault(location, Collections.emptyList()));
    }

    public Set<String> getAllNodes() {
        return Collections.unmodifiableSet(locations);
    }

    public void addEdge(String fromLocation, String toLocation, double weight) {
        if (!Double.isFinite(weight) || weight < 0) {
            throw new IllegalArgumentException(
                    "Dijkstra's algorithm requires non-negative edge weights.");
        }

        Edge edge = new Edge(fromLocation, toLocation, weight);
        Edge reversedEdge = new Edge(toLocation, fromLocation, weight);

        adjacencyList.computeIfAbsent(fromLocation, key -> new ArrayList<>()).add(edge);
        adjacencyList.computeIfAbsent(toLocation, key -> new ArrayList<>()).add(reversedEdge);

        locations.add(edge.getFromLocation());
        locations.add(edge.getToLocation());
    }
}
