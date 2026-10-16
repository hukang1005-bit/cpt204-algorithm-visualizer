package model;

public class Path {
    private final String fromLocationId;
    private final String toLocationId;
    private final double weight;

    public Path(String fromLocationId, String toLocationId, double weight) {
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.weight = weight;
    }

    public String getFromLocationId() {
        return fromLocationId;
    }

    public String getToLocationId() {
        return toLocationId;
    }

    public double getWeight() {
        return weight;
    }
}
