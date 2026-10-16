package sorting;

import model.CandidateLocation;

import java.util.List;

public interface Sorter {
    void sort(List<CandidateLocation> locations);

    String getName();
}
