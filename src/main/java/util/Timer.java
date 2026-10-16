package util;

import model.CandidateLocation;

import sorting.Sorter;

import java.util.List;

public class Timer {
    public static double measureSortTimeMillis(Sorter sorter, List<CandidateLocation> locations) {
        long startTime = System.nanoTime();

        sorter.sort(locations);

        long endTime = System.nanoTime();

        return (endTime - startTime) / 1_000_000.0;
    }
}
