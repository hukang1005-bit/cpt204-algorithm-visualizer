import graph.*;

import io.CandidateCSVReader;

import model.*;

import sorting.*;

import java.util.*;

public class AlgorithmChecks {
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Random rng = new Random(42);
        for (int trial = 0; trial < 100; trial++) {
            Graph g = new Graph();
            for (int i = 0; i < 18; i++) g.addEdge("n" + i, "n" + i, 0);
            for (int i = 0; i < 50; i++)
                g.addEdge("n" + rng.nextInt(18), "n" + rng.nextInt(18), rng.nextInt(30));
            for (int from = 0; from < 18; from++)
                for (int to = 0; to < 18; to++) {
                    RouteResult a = DijkstraAlgorithm.findShortestPath(g, "n" + from, "n" + to);
                    RouteResult b =
                            BidirectionalDijkstraAlgorithm.findShortestPath(
                                    g, "n" + from, "n" + to);
                    check(a.isReachable() == b.isReachable(), "reachability mismatch");
                    if (a.isReachable())
                        check(
                                Math.abs(a.getTotalCost() - b.getTotalCost()) < 1e-9,
                                "distance mismatch");
                }
        }
        for (String file :
                new String[] {
                    "data/candidates_A.csv", "data/candidates_B.csv", "data/candidates_C.csv"
                }) {
            List<CandidateLocation> data = CandidateCSVReader.readCandidates(file);
            List<CandidateLocation> expected = new ArrayList<>(data);
            expected.sort(new LocationComparator());
            for (Sorter sorter :
                    new Sorter[] {new BubbleSorter(), new MergeSorter(), new QuickSorter()}) {
                List<CandidateLocation> actual = new ArrayList<>(data);
                sorter.sort(actual);
                for (int i = 0; i < actual.size(); i++)
                    check(
                            actual.get(i).getLocationId().equals(expected.get(i).getLocationId()),
                            "sort mismatch at " + i);
            }
        }
        for (double weight : new double[] {-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            try {
                new Graph().addEdge("a", "b", weight);
                throw new AssertionError("invalid weight accepted");
            } catch (IllegalArgumentException expected) {
            }
        }
        try {
            CandidateCSVReader.readCandidates("missing.csv");
            throw new AssertionError("missing dataset accepted");
        } catch (IllegalArgumentException expected) {
        }
        System.out.println(
                "Algorithm checks passed: 32,400 route comparisons and all dataset sorting"
                    + " orders.");
    }
}
