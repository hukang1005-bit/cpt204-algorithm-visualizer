package model;

public class CandidateLocation {
    private final String locationId;
    private final int priorityScore;

    public CandidateLocation(String locationId, int priorityScore) {
        this.locationId = locationId;
        this.priorityScore = priorityScore;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public String getLocationId() {
        return locationId;
    }

    @Override
    public String toString() {
        return "[" + this.locationId + ", " + this.priorityScore + "]";
    }
}
