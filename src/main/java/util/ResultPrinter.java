package util;

import model.CandidateLocation;

import java.util.List;

public class ResultPrinter {
    public static void printTop10(List<CandidateLocation> result) {
        int limit = Math.min(10, result.size());
        for (int i = 0; i < limit; i++) {
            System.out.println(result.get(i));
        }
    }
}
