package service;

import io.CandidateCSVReader;

import model.CandidateLocation;

import sorting.BubbleSorter;
import sorting.MergeSorter;
import sorting.QuickSorter;
import sorting.Sorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SortingService {
    public static final int DEFAULT_WARM_UP_RUNS = 10;
    public static final int DEFAULT_MEASURED_RUNS = 100;
    public static final int DEFAULT_SORTS_PER_MEASURED_RUN = 1;

    private final Sorter[] sorters = {new BubbleSorter(), new QuickSorter(), new MergeSorter()};

    public SortingResult processDataset(String datasetName, String filePath, int runs) {
        return processDataset(
                datasetName, filePath, DEFAULT_WARM_UP_RUNS, runs, DEFAULT_SORTS_PER_MEASURED_RUN);
    }

    public SortingResult processDataset(String datasetName, String filePath) {
        return processDataset(
                datasetName,
                filePath,
                DEFAULT_WARM_UP_RUNS,
                DEFAULT_MEASURED_RUNS,
                DEFAULT_SORTS_PER_MEASURED_RUN);
    }

    public SortingResult processDataset(
            String datasetName, String filePath, int warmUpRuns, int measuredRuns) {
        return processDataset(
                datasetName, filePath, warmUpRuns, measuredRuns, DEFAULT_SORTS_PER_MEASURED_RUN);
    }

    public SortingResult processDataset(
            String datasetName,
            String filePath,
            int warmUpRuns,
            int measuredRuns,
            int sortsPerMeasuredRun) {
        if (sortsPerMeasuredRun != 1) {
            throw new IllegalArgumentException("Only one sort per measured run is supported.");
        }
        if (warmUpRuns < 0) {
            throw new IllegalArgumentException("Warm-up runs cannot be negative.");
        }
        if (measuredRuns <= 0) {
            throw new IllegalArgumentException("Measured runs must be greater than zero.");
        }

        List<CandidateLocation> originalLocations = CandidateCSVReader.readCandidates(filePath);
        if (originalLocations.isEmpty()) {
            throw new IllegalArgumentException(
                    "No candidate locations were loaded from " + filePath);
        }

        Map<String, Double> averageTimes = new LinkedHashMap<>();
        List<CandidateLocation> selectedTop10 = Collections.emptyList();

        for (Sorter sorter : sorters) {
            long totalTime = 0;
            List<CandidateLocation> sortedLocations = null;

            for (int i = 0; i < warmUpRuns; i++) {
                sorter.sort(new ArrayList<>(originalLocations));
            }

            for (int i = 0; i < measuredRuns; i++) {
                sortedLocations = new ArrayList<>(originalLocations);
                long startTime = System.nanoTime();
                sorter.sort(sortedLocations);
                long endTime = System.nanoTime();
                totalTime += (endTime - startTime);
            }

            averageTimes.put(sorter.getName(), (totalTime / 1_000_000.0) / measuredRuns);

            if (selectedTop10.isEmpty() && sortedLocations != null) {
                selectedTop10 =
                        new ArrayList<>(
                                sortedLocations.subList(0, Math.min(10, sortedLocations.size())));
            } else if (sortedLocations != null) {
                List<CandidateLocation> currentTop10 =
                        sortedLocations.subList(0, Math.min(10, sortedLocations.size()));
                if (!hasSameLocationOrder(selectedTop10, currentTop10)) {
                    throw new IllegalStateException(
                            sorter.getName() + " produced a different Top 10 order.");
                }
            }
        }

        return new SortingResult(
                datasetName, warmUpRuns, measuredRuns, 1, averageTimes, selectedTop10);
    }

    private boolean hasSameLocationOrder(
            List<CandidateLocation> first, List<CandidateLocation> second) {
        if (first.size() != second.size()) {
            return false;
        }

        for (int i = 0; i < first.size(); i++) {
            if (!first.get(i).getLocationId().equals(second.get(i).getLocationId())) {
                return false;
            }
        }

        return true;
    }

    public static class SortingResult {
        private final String datasetName;
        private final int warmUpRuns;
        private final int measuredRuns;
        private final int sortsPerMeasuredRun;
        private final Map<String, Double> averageTimes;
        private final List<CandidateLocation> top10Locations;

        public SortingResult(
                String datasetName,
                int warmUpRuns,
                int measuredRuns,
                int sortsPerMeasuredRun,
                Map<String, Double> averageTimes,
                List<CandidateLocation> top10Locations) {
            this.datasetName = datasetName;
            this.warmUpRuns = warmUpRuns;
            this.measuredRuns = measuredRuns;
            this.sortsPerMeasuredRun = sortsPerMeasuredRun;
            this.averageTimes = new LinkedHashMap<>(averageTimes);
            this.top10Locations = new ArrayList<>(top10Locations);
        }

        public String getDatasetName() {
            return datasetName;
        }

        public int getWarmUpRuns() {
            return warmUpRuns;
        }

        public int getMeasuredRuns() {
            return measuredRuns;
        }

        public int getSortsPerMeasuredRun() {
            return sortsPerMeasuredRun;
        }

        public Map<String, Double> getAverageTimes() {
            return Collections.unmodifiableMap(averageTimes);
        }

        public List<CandidateLocation> getTop10Locations() {
            return Collections.unmodifiableList(top10Locations);
        }
    }
}
