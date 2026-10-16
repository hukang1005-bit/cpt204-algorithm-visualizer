package sorting;

import model.CandidateLocation;

import java.util.ArrayList;
import java.util.List;

public class MergeSorter implements Sorter {

    private final LocationComparator comparator = new LocationComparator();

    @Override
    public void sort(List<CandidateLocation> locations) {
        if (locations == null || locations.size() <= 1) {
            return;
        }

        List<CandidateLocation> temp = new ArrayList<>(locations);
        mergeSort(locations, temp, 0, locations.size() - 1);
    }

    private void mergeSort(
            List<CandidateLocation> locations, List<CandidateLocation> temp, int left, int right) {
        if (left >= right) {
            return;
        }

        int mid = left + (right - left) / 2;

        mergeSort(locations, temp, left, mid);
        mergeSort(locations, temp, mid + 1, right);

        merge(locations, temp, left, mid, right);
    }

    private void merge(
            List<CandidateLocation> locations,
            List<CandidateLocation> temp,
            int left,
            int mid,
            int right) {
        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            CandidateLocation c1 = locations.get(i);
            CandidateLocation c2 = locations.get(j);

            if (comparator.compare(c1, c2) > 0) {
                temp.set(k, c2);
                j++;
                k++;
            } else {
                temp.set(k, c1);
                i++;
                k++;
            }
        }

        while (i <= mid) {
            temp.set(k, locations.get(i));
            i++;
            k++;
        }

        while (j <= right) {
            temp.set(k, locations.get(j));
            j++;
            k++;
        }

        for (int p = left; p <= right; p++) {
            locations.set(p, temp.get(p));
        }
    }

    @Override
    public String getName() {
        return "Merge Sort";
    }
}
