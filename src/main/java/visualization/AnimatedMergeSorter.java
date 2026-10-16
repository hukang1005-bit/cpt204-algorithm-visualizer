package visualization;

import model.CandidateLocation;

import sorting.LocationComparator;

import java.util.ArrayList;
import java.util.List;

public class AnimatedMergeSorter implements AnimatedSorter {
    private final LocationComparator comparator = new LocationComparator();

    @Override
    public String getName() {
        return "Merge Sort";
    }

    @Override
    public void sort(List<CandidateLocation> locations, SortStepListener listener) {
        if (locations == null || locations.size() <= 1) {
            return;
        }

        List<CandidateLocation> temp = new ArrayList<>(locations);
        mergeSort(locations, temp, 0, locations.size() - 1, listener);
    }

    private void mergeSort(
            List<CandidateLocation> locations,
            List<CandidateLocation> temp,
            int left,
            int right,
            SortStepListener listener) {
        if (left >= right || listener.isCancelled()) {
            return;
        }

        int mid = left + (right - left) / 2;
        mergeSort(locations, temp, left, mid, listener);
        mergeSort(locations, temp, mid + 1, right, listener);
        merge(locations, temp, left, mid, right, listener);
    }

    private void merge(
            List<CandidateLocation> locations,
            List<CandidateLocation> temp,
            int left,
            int mid,
            int right,
            SortStepListener listener) {
        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right && !listener.isCancelled()) {
            CandidateLocation leftItem = locations.get(i);
            CandidateLocation rightItem = locations.get(j);

            if (comparator.compare(leftItem, rightItem) > 0) {
                temp.set(k, rightItem);
                j++;
            } else {
                temp.set(k, leftItem);
                i++;
            }
            k++;
        }

        while (i <= mid && !listener.isCancelled()) {
            temp.set(k, locations.get(i));
            i++;
            k++;
        }

        while (j <= right && !listener.isCancelled()) {
            temp.set(k, locations.get(j));
            j++;
            k++;
        }

        for (int p = left; p <= right && !listener.isCancelled(); p++) {
            locations.set(p, temp.get(p));
            listener.onStep(
                    new SortStep(
                            locations,
                            p,
                            -1,
                            left,
                            right,
                            "Merged index " + p + " in range " + left + "-" + right));
        }
    }
}
