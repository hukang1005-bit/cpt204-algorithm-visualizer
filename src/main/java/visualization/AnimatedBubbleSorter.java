package visualization;

import model.CandidateLocation;

import sorting.LocationComparator;

import java.util.List;

public class AnimatedBubbleSorter implements AnimatedSorter {
    private final LocationComparator comparator = new LocationComparator();

    @Override
    public String getName() {
        return "Bubble Sort";
    }

    @Override
    public void sort(List<CandidateLocation> locations, SortStepListener listener) {
        for (int i = 0; i < locations.size() - 1 && !listener.isCancelled(); i++) {
            boolean swapped = false;

            for (int j = 0; j < locations.size() - 1 - i && !listener.isCancelled(); j++) {
                CandidateLocation left = locations.get(j);
                CandidateLocation right = locations.get(j + 1);

                if (comparator.compare(left, right) > 0) {
                    locations.set(j, right);
                    locations.set(j + 1, left);
                    swapped = true;
                    listener.onStep(
                            new SortStep(
                                    locations,
                                    j,
                                    j + 1,
                                    locations.size() - 1 - i,
                                    locations.size() - 1,
                                    "Swapped index " + j + " and " + (j + 1)));
                }
            }

            listener.onStep(
                    new SortStep(
                            locations,
                            -1,
                            -1,
                            locations.size() - 1 - i,
                            locations.size() - 1,
                            "Completed bubble pass " + (i + 1)));

            if (!swapped) {
                listener.onStep(
                        new SortStep(
                                locations,
                                -1,
                                -1,
                                0,
                                locations.size() - 1,
                                "Array is already sorted"));
                break;
            }
        }
    }
}
