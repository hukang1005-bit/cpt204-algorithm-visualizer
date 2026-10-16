package visualization;

import model.CandidateLocation;

import sorting.LocationComparator;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AnimatedQuickSorter implements AnimatedSorter {
    private final LocationComparator comparator = new LocationComparator();
    private final Random random = new Random(204);

    @Override
    public String getName() {
        return "Quick Sort";
    }

    @Override
    public void sort(List<CandidateLocation> locations, SortStepListener listener) {
        if (locations == null || locations.size() <= 1) {
            return;
        }

        Set<Integer> finalizedIndices = new HashSet<>();
        quickSort(locations, 0, locations.size() - 1, listener, finalizedIndices);
        if (!listener.isCancelled()) {
            for (int i = 0; i < locations.size(); i++) {
                finalizedIndices.add(i);
            }
            listener.onStep(
                    new SortStep(
                            locations, -1, -1, -1, -1, finalizedIndices, "Quick Sort completed"));
        }
    }

    private void quickSort(
            List<CandidateLocation> locations,
            int left,
            int right,
            SortStepListener listener,
            Set<Integer> finalizedIndices) {
        if (listener.isCancelled()) {
            return;
        }

        if (left == right) {
            finalizedIndices.add(left);
            listener.onStep(
                    new SortStep(
                            locations,
                            -1,
                            -1,
                            -1,
                            -1,
                            finalizedIndices,
                            "Single-item partition finalized at index " + left));
            return;
        }

        if (left > right) {
            return;
        }

        int pivotIndex = partition(locations, left, right, listener, finalizedIndices);
        if (listener.isCancelled()) {
            return;
        }

        finalizedIndices.add(pivotIndex);
        listener.onStep(
                new SortStep(
                        locations,
                        -1,
                        -1,
                        -1,
                        -1,
                        finalizedIndices,
                        "Pivot placed at index " + pivotIndex));

        quickSort(locations, left, pivotIndex - 1, listener, finalizedIndices);
        quickSort(locations, pivotIndex + 1, right, listener, finalizedIndices);
    }

    private int partition(
            List<CandidateLocation> locations,
            int left,
            int right,
            SortStepListener listener,
            Set<Integer> finalizedIndices) {
        int randomIndex = left + random.nextInt(right - left + 1);
        swap(locations, randomIndex, right);
        listener.onStep(
                new SortStep(
                        locations,
                        randomIndex,
                        right,
                        -1,
                        -1,
                        finalizedIndices,
                        "Moved pivot candidate to index " + right));

        CandidateLocation pivot = locations.get(right);
        int i = left - 1;

        for (int j = left; j < right && !listener.isCancelled(); j++) {
            if (comparator.compare(locations.get(j), pivot) <= 0) {
                i++;
                swap(locations, i, j);
                listener.onStep(
                        new SortStep(
                                locations,
                                i,
                                j,
                                -1,
                                -1,
                                finalizedIndices,
                                "Partition swap inside index " + left + "-" + right));
            }
        }

        swap(locations, i + 1, right);
        listener.onStep(
                new SortStep(
                        locations,
                        i + 1,
                        right,
                        -1,
                        -1,
                        finalizedIndices,
                        "Moved pivot to final position"));
        return i + 1;
    }

    private void swap(List<CandidateLocation> locations, int i, int j) {
        if (i == j) {
            return;
        }

        CandidateLocation temp = locations.get(i);
        locations.set(i, locations.get(j));
        locations.set(j, temp);
    }
}
