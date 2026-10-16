package util;

import model.CandidateLocation;

import service.ConsoleReportService.StabilityStats;
import service.SortingService;

import java.util.List;
import java.util.Map;

public class ReportFormatter {
    private static final String BUBBLE = "Bubble Sort";
    private static final String QUICK = "Quick Sort";
    private static final String MERGE = "Merge Sort";

    public static String formatSortingReportTable(List<SortingService.SortingResult> results) {
        StringBuilder builder = new StringBuilder();
        if (!results.isEmpty()) {
            SortingService.SortingResult first = results.get(0);
            builder.append("Timing method: ")
                    .append(first.getWarmUpRuns())
                    .append(" warm-up runs discarded, ")
                    .append(first.getMeasuredRuns())
                    .append(" measured runs averaged.\n\n");
        }

        String[] headers = {
            "Dataset",
            "Bubble avg (ms)",
            "Quick avg (ms)",
            "Merge avg (ms)",
            "Top 10 Selected Locations"
        };
        String[][] rows = new String[results.size()][headers.length];

        for (int i = 0; i < results.size(); i++) {
            SortingService.SortingResult result = results.get(i);
            Map<String, Double> times = result.getAverageTimes();
            double bubble = getTime(times, BUBBLE);
            double quick = getTime(times, QUICK);
            double merge = getTime(times, MERGE);

            rows[i][0] = result.getDatasetName();
            rows[i][1] = formatMillis(bubble);
            rows[i][2] = formatMillis(quick);
            rows[i][3] = formatMillis(merge);
            rows[i][4] = formatTopLocations(result.getTop10Locations());
        }

        builder.append(formatAlignedTable(headers, rows));
        return builder.toString();
    }

    private static String formatAlignedTable(String[] headers, String[][] rows) {
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        StringBuilder builder = new StringBuilder();
        appendSeparator(builder, widths);
        appendRow(builder, headers, widths);
        appendSeparator(builder, widths);
        for (String[] row : rows) {
            appendRow(builder, row, widths);
        }
        appendSeparator(builder, widths);
        return builder.toString();
    }

    private static void appendSeparator(StringBuilder builder, int[] widths) {
        builder.append('+');
        for (int width : widths) {
            for (int i = 0; i < width + 2; i++) {
                builder.append('-');
            }
            builder.append('+');
        }
        builder.append('\n');
    }

    private static void appendRow(StringBuilder builder, String[] cells, int[] widths) {
        builder.append('|');
        for (int i = 0; i < cells.length; i++) {
            builder.append(' ');
            builder.append(padRight(cells[i], widths[i]));
            builder.append(' ');
            builder.append('|');
        }
        builder.append('\n');
    }

    private static String padRight(String value, int width) {
        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < width) {
            builder.append(' ');
        }
        return builder.toString();
    }

    private static double getTime(Map<String, Double> times, String key) {
        Double time = times.get(key);
        if (time == null) {
            return 0;
        }
        return time;
    }

    private static String formatMillis(double millis) {
        return String.format("%.3f", millis);
    }

    private static String formatTopLocations(List<CandidateLocation> locations) {
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(10, locations.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            CandidateLocation location = locations.get(i);
            builder.append(location.getLocationId())
                    .append(" (")
                    .append(location.getPriorityScore())
                    .append(")");
        }
        return builder.toString();
    }

    public static String formatStabilityReportTable(List<StabilityStats> statsList) {
        String[] headers = {
            "Algorithm",
            "Mean Time (ms)",
            "Population Var (ms²)",
            "Sample Variance (ms²)",
            "Std Dev (ms)"
        };
        String[][] rows = new String[statsList.size()][headers.length];

        for (int i = 0; i < statsList.size(); i++) {
            StabilityStats stats = statsList.get(i);
            rows[i][0] = stats.getAlgorithmName();
            rows[i][1] = formatMillis(stats.getMean());
            rows[i][2] = String.format("%.6f", stats.getPopulationVariance());
            rows[i][3] = String.format("%.6f", stats.getSampleVariance());
            rows[i][4] = formatMillis(stats.getStdDev());
        }

        return formatAlignedTable(headers, rows);
    }

    public static String formatRouteReportTable(
            List<service.RouteService.RouteBenchmarkResult> results) {
        String[] headers = {
            "Route Case",
            "Original Dijkstra (μs)",
            "Bidirectional Dijkstra (μs)",
            "Speedup Factor",
            "Correctness"
        };
        String[][] rows = new String[results.size()][headers.length];

        for (int i = 0; i < results.size(); i++) {
            service.RouteService.RouteBenchmarkResult result = results.get(i);
            rows[i][0] = result.getCaseName();
            rows[i][1] = String.format("%.3f", result.getOriginalAvgTimeUs());
            rows[i][2] = String.format("%.3f", result.getBidirectionalAvgTimeUs());
            rows[i][3] = String.format("%.2fx", result.getSpeedup());
            rows[i][4] = result.isCorrect() ? "PASSED" : "FAILED";
        }

        return formatAlignedTable(headers, rows);
    }
}
