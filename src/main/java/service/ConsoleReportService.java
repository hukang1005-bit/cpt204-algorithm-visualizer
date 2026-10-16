package service;

import graph.Graph;

import io.PathReader;

import model.CandidateLocation;
import model.Path;
import model.RouteResult;

import util.ReportFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsoleReportService {
    private static final String[] DATASET_NAMES = {"Dataset A", "Dataset B", "Dataset C"};

    private static final String[] DATASET_FILES = {
        "data/candidates_A.csv", "data/candidates_B.csv", "data/candidates_C.csv"
    };

    public ConsoleReport buildConsoleReport() {
        SortingService sortingService = new SortingService();
        RouteService routeService = new RouteService();

        SortingService.SortingResult[] sortingResults =
                new SortingService.SortingResult[DATASET_FILES.length];
        List<SortingService.SortingResult> reportResults = new ArrayList<>();

        for (int i = 0; i < DATASET_FILES.length; i++) {
            sortingResults[i] = sortingService.processDataset(DATASET_NAMES[i], DATASET_FILES[i]);
            reportResults.add(sortingResults[i]);
        }

        List<Path> paths = PathReader.readPaths("data/paths.csv");
        Graph graph = routeService.buildGraph(paths);

        List<CandidateLocation> datasetATop10 = sortingResults[0].getTop10Locations();
        List<CandidateLocation> datasetBTop10 = sortingResults[1].getTop10Locations();
        List<CandidateLocation> datasetCTop10 = sortingResults[2].getTop10Locations();

        List<StabilityStats> stabilityStatsList = new ArrayList<>();
        String[] algorithms = {"Bubble Sort", "Quick Sort", "Merge Sort"};
        for (String alg : algorithms) {
            List<Double> times = new ArrayList<>();
            for (SortingService.SortingResult res : reportResults) {
                Double t = res.getAverageTimes().get(alg);
                if (t != null) {
                    times.add(t);
                }
            }
            if (!times.isEmpty()) {
                double sum = 0;
                for (double t : times) {
                    sum += t;
                }
                double mean = sum / times.size();

                double popVarSum = 0;
                for (double t : times) {
                    popVarSum += Math.pow(t - mean, 2);
                }
                double popVar = popVarSum / times.size();

                double sampleVar = 0;
                if (times.size() > 1) {
                    sampleVar = popVarSum / (times.size() - 1);
                }

                double stdDev = Math.sqrt(popVar);
                stabilityStatsList.add(new StabilityStats(alg, mean, popVar, sampleVar, stdDev));
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("================================\n");
        builder.append("Task A sorting report table\n");
        builder.append("================================\n");
        builder.append(ReportFormatter.formatSortingReportTable(reportResults)).append('\n');

        builder.append("================================\n");
        builder.append("Sorting Algorithm Stability Analysis (Variance across datasets)\n");
        builder.append("================================\n");
        builder.append(ReportFormatter.formatStabilityReportTable(stabilityStatsList)).append('\n');

        builder.append("================================\n");
        builder.append("Task B shortest-path cases\n");
        builder.append("================================\n");
        appendRoute(
                builder,
                "Case 1",
                routeService.findShortestPath(
                        graph,
                        datasetATop10.get(0).getLocationId(),
                        datasetATop10.get(0).getLocationId()));
        appendRoute(
                builder,
                "Case 2",
                routeService.findShortestPath(
                        graph,
                        datasetATop10.get(0).getLocationId(),
                        datasetATop10.get(9).getLocationId()));
        appendRoute(
                builder,
                "Case 3",
                routeService.findShortestPathVia(
                        graph,
                        datasetATop10.get(0).getLocationId(),
                        datasetBTop10.get(0).getLocationId(),
                        datasetBTop10.get(4).getLocationId()));
        appendRoute(
                builder,
                "Case 4",
                routeService.findShortestPathVia(
                        graph,
                        datasetATop10.get(0).getLocationId(),
                        datasetCTop10.get(0).getLocationId(),
                        datasetBTop10.get(4).getLocationId(),
                        datasetCTop10.get(4).getLocationId()));

        // Routing performance benchmark
        List<RouteService.RouteBenchmarkResult> routeBenchmarkResults = new ArrayList<>();
        int warmUp = 50;
        int measured = 2000;

        routeBenchmarkResults.add(
                routeService.benchmarkCase(
                        "Case 1: A1 to A1",
                        graph,
                        datasetATop10.get(0).getLocationId(),
                        datasetATop10.get(0).getLocationId(),
                        new String[0],
                        warmUp,
                        measured));

        routeBenchmarkResults.add(
                routeService.benchmarkCase(
                        "Case 2: A1 to A10",
                        graph,
                        datasetATop10.get(0).getLocationId(),
                        datasetATop10.get(9).getLocationId(),
                        new String[0],
                        warmUp,
                        measured));

        routeBenchmarkResults.add(
                routeService.benchmarkCase(
                        "Case 3: A1 to B1 via B5",
                        graph,
                        datasetATop10.get(0).getLocationId(),
                        datasetBTop10.get(0).getLocationId(),
                        new String[] {datasetBTop10.get(4).getLocationId()},
                        warmUp,
                        measured));

        routeBenchmarkResults.add(
                routeService.benchmarkCase(
                        "Case 4: A1 to C1 via B5 and C5",
                        graph,
                        datasetATop10.get(0).getLocationId(),
                        datasetCTop10.get(0).getLocationId(),
                        new String[] {
                            datasetBTop10.get(4).getLocationId(),
                            datasetCTop10.get(4).getLocationId()
                        },
                        warmUp,
                        measured));

        builder.append("================================\n");
        builder.append("Task B routing benchmark table (microseconds)\n");
        builder.append("================================\n");
        builder.append(ReportFormatter.formatRouteReportTable(routeBenchmarkResults)).append('\n');

        return new ConsoleReport(
                builder.toString(), reportResults, stabilityStatsList, routeBenchmarkResults);
    }

    private void appendRoute(StringBuilder builder, String caseName, RouteResult route) {
        builder.append(caseName).append('\n');
        builder.append("Start: ").append(route.getStartLocation()).append('\n');
        builder.append("Destination: ").append(route.getDestinationLocation()).append('\n');
        builder.append("Path: ").append(route.formatPath()).append('\n');
        builder.append("Total cost: ").append(route.getTotalCost()).append("\n\n");
    }

    public static class StabilityStats {
        private final String algorithmName;
        private final double mean;
        private final double populationVariance;
        private final double sampleVariance;
        private final double stdDev;

        public StabilityStats(
                String algorithmName,
                double mean,
                double populationVariance,
                double sampleVariance,
                double stdDev) {
            this.algorithmName = algorithmName;
            this.mean = mean;
            this.populationVariance = populationVariance;
            this.sampleVariance = sampleVariance;
            this.stdDev = stdDev;
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public double getMean() {
            return mean;
        }

        public double getPopulationVariance() {
            return populationVariance;
        }

        public double getSampleVariance() {
            return sampleVariance;
        }

        public double getStdDev() {
            return stdDev;
        }
    }

    public static class ConsoleReport {
        private final String text;
        private final List<SortingService.SortingResult> sortingResults;
        private final List<StabilityStats> stabilityStats;
        private final List<RouteService.RouteBenchmarkResult> routeResults;

        public ConsoleReport(
                String text,
                List<SortingService.SortingResult> sortingResults,
                List<StabilityStats> stabilityStats,
                List<RouteService.RouteBenchmarkResult> routeResults) {
            this.text = text;
            this.sortingResults = new ArrayList<>(sortingResults);
            this.stabilityStats = new ArrayList<>(stabilityStats);
            this.routeResults = new ArrayList<>(routeResults);
        }

        public String getText() {
            return text;
        }

        public List<SortingService.SortingResult> getSortingResults() {
            return Collections.unmodifiableList(sortingResults);
        }

        public List<StabilityStats> getStabilityStats() {
            return Collections.unmodifiableList(stabilityStats);
        }

        public List<RouteService.RouteBenchmarkResult> getRouteResults() {
            return Collections.unmodifiableList(routeResults);
        }
    }
}
