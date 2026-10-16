package sorting;

import model.CandidateLocation;

import java.util.List;

public class BubbleSorter implements Sorter {

    private final LocationComparator comparator = new LocationComparator();

    @Override
    public void sort(List<CandidateLocation> locations) {
        for (int i = 0; i < locations.size() - 1; i++) {

            boolean swapped = false;

            for (int j = 0; j < locations.size() - 1 - i; j++) {
                CandidateLocation c1 = locations.get(j);
                CandidateLocation c2 = locations.get(j + 1);
                int result = comparator.compare(c1, c2);
                if (result > 0) {
                    locations.set(j, c2);
                    locations.set(j + 1, c1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    @Override
    public String getName() {
        return "Bubble Sort";
    }
}
