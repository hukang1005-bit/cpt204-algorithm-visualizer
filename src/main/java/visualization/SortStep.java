package visualization;

import model.CandidateLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SortStep {
    private final List<CandidateLocation> snapshot;
    private final int firstIndex;
    private final int secondIndex;
    private final int sortedStart;
    private final int sortedEnd;
    private final Set<Integer> sortedIndices;
    private final String description;

    public SortStep(
            List<CandidateLocation> snapshot,
            int firstIndex,
            int secondIndex,
            int sortedStart,
            int sortedEnd,
            String description) {
        this(
                snapshot,
                firstIndex,
                secondIndex,
                sortedStart,
                sortedEnd,
                Collections.emptySet(),
                description);
    }

    public SortStep(
            List<CandidateLocation> snapshot,
            int firstIndex,
            int secondIndex,
            int sortedStart,
            int sortedEnd,
            Set<Integer> sortedIndices,
            String description) {
        this.snapshot = new ArrayList<>(snapshot);
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        this.sortedStart = sortedStart;
        this.sortedEnd = sortedEnd;
        this.sortedIndices = new HashSet<>(sortedIndices);
        this.description = description;
    }

    public List<CandidateLocation> getSnapshot() {
        return Collections.unmodifiableList(snapshot);
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public int getSecondIndex() {
        return secondIndex;
    }

    public int getSortedStart() {
        return sortedStart;
    }

    public int getSortedEnd() {
        return sortedEnd;
    }

    public Set<Integer> getSortedIndices() {
        return Collections.unmodifiableSet(sortedIndices);
    }

    public String getDescription() {
        return description;
    }
}
