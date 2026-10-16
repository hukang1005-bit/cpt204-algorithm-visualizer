package visualization;

import model.CandidateLocation;

import java.util.List;

public interface AnimatedSorter {
    String getName();

    void sort(List<CandidateLocation> locations, SortStepListener listener);
}
