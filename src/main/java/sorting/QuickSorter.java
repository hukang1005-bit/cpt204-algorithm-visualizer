package sorting;

import model.CandidateLocation;

import java.util.List;
import java.util.Random;

public class QuickSorter implements Sorter {

    private final Random random = new Random();
    private final LocationComparator comparator = new LocationComparator();

    @Override
    public void sort(List<CandidateLocation> locations) {
        if (locations == null || locations.size() <= 1) {
            return;
        }

        quickSort(locations, 0, locations.size() - 1);
    }

    private void quickSort(List<CandidateLocation> locations, int left, int right) {
        if (left >= right) {
            return;
        }

        int pivotIndex = partition(locations, left, right);

        quickSort(locations, left, pivotIndex - 1);
        quickSort(locations, pivotIndex + 1, right);
    }

    private int partition(List<CandidateLocation> locations, int left, int right) {
        int midIndex = left + (right - left) / 2;
        swap(locations, midIndex, right);

        CandidateLocation pivot = locations.get(right);

        int i = left - 1;

        for (int j = left; j < right; j++) {
            CandidateLocation current = locations.get(j);

            if (comparator.compare(current, pivot) <= 0) {
                i++;
                swap(locations, i, j);
            }
        }

        swap(locations, i + 1, right);
        return i + 1;
    }

    private void swap(List<CandidateLocation> locations, int i, int j) {
        CandidateLocation temp = locations.get(i);
        locations.set(i, locations.get(j));
        locations.set(j, temp);
    }

    @Override
    public String getName() {
        return "Quick Sort";
    }
}
