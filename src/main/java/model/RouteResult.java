package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteResult {
    private final String startLocation;
    private final String destinationLocation;
    private final List<String> path;
    private final double totalCost;

    public RouteResult(
            String startLocation, String destinationLocation, List<String> path, double totalCost) {
        this.startLocation = startLocation;
        this.destinationLocation = destinationLocation;
        this.path = new ArrayList<>(path);
        this.totalCost = totalCost;
    }

    public static RouteResult unreachable(String startLocation, String destinationLocation) {
        return new RouteResult(
                startLocation,
                destinationLocation,
                Collections.emptyList(),
                Double.POSITIVE_INFINITY);
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public List<String> getPath() {
        return Collections.unmodifiableList(path);
    }

    public double getTotalCost() {
        return totalCost;
    }

    public boolean isReachable() {
        return !path.isEmpty() && totalCost < Double.POSITIVE_INFINITY;
    }

    public String formatPath() {
        if (!isReachable()) {
            return "No path";
        }
        return String.join(" -> ", path);
    }

    @Override
    public String toString() {
        return formatPath() + " | cost: " + totalCost;
    }
}
