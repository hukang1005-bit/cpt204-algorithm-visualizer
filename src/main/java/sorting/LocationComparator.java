package sorting;

import model.CandidateLocation;

import java.util.Comparator;

public class LocationComparator implements Comparator<CandidateLocation> {
    @Override
    public int compare(CandidateLocation c1, CandidateLocation c2) {
        if (c1.getPriorityScore() == c2.getPriorityScore()) {
            return c1.getLocationId().compareTo(c2.getLocationId());
        }
        return Integer.compare(c2.getPriorityScore(), c1.getPriorityScore());
    }
}
