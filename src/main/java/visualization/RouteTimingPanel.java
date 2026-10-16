package visualization;

import graph.Graph;

import io.PathReader;

import model.CandidateLocation;
import model.Path;
import model.RouteResult;

import service.RouteService;
import service.SortingService;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RouteTimingPanel extends JPanel {
    private final RouteService routeService = new RouteService();
    private final SortingService sortingService = new SortingService();
    private Graph graph;
    private List<CandidateLocation> topA;
    private List<CandidateLocation> topB;
    private List<CandidateLocation> topC;

    private final DefaultTableModel caseTableModel =
            new DefaultTableModel(
                    new Object[] {
                        "Route Case",
                        "Start Node",
                        "Target Node",
                        "Original (μs)",
                        "Bidirectional (μs)",
                        "Speedup",
                        "Verification"
                    },
                    0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable caseTable = new JTable(caseTableModel);

    private final DefaultTableModel bulkTableModel =
            new DefaultTableModel(
                    new Object[] {
                        "Benchmark Test",
                        "Query Count",
                        "Original Dijkstra Total (ms)",
                        "Bidirectional Total (ms)",
                        "Average Speedup",
                        "Verification"
                    },
                    0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable bulkTable = new JTable(bulkTableModel);

    private final JButton runButton = new JButton("Run Route Timing Test");
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea logArea = new JTextArea();
    private final RouteBarChartPanel chartPanel = new RouteBarChartPanel();
    private boolean hasRunTimingTest = false;

    public RouteTimingPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        runButton.setPreferredSize(new Dimension(220, 36));
        buildLayout();
        loadData();
        runButton.addActionListener(event -> runRouteBenchmark());
    }

    public void runInitialTimingTestIfNeeded() {
        if (!hasRunTimingTest) {
            runRouteBenchmark();
        }
    }

    private void loadData() {
        try {
            List<Path> paths = PathReader.readPaths("data/paths.csv");
            graph = routeService.buildGraph(paths);

            SortingService.SortingResult datasetA =
                    sortingService.processDataset("Dataset A", "data/candidates_A.csv", 3);
            SortingService.SortingResult datasetB =
                    sortingService.processDataset("Dataset B", "data/candidates_B.csv", 3);
            SortingService.SortingResult datasetC =
                    sortingService.processDataset("Dataset C", "data/candidates_C.csv", 3);

            topA = datasetA.getTop10Locations();
            topB = datasetB.getTop10Locations();
            topC = datasetC.getTop10Locations();
        } catch (Exception e) {
            logArea.setText("Error loading datasets: " + e.getMessage());
        }
    }

    private void buildLayout() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(runButton);
        top.add(statusLabel);

        caseTable.setRowHeight(28);
        caseTable.getTableHeader().setReorderingAllowed(false);
        caseTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        bulkTable.setRowHeight(28);
        bulkTable.getTableHeader().setReorderingAllowed(false);
        bulkTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane caseScroll = new JScrollPane(caseTable);
        caseScroll.setBorder(
                BorderFactory.createTitledBorder("Coursework Predefined Route Benchmarks"));

        JScrollPane bulkScroll = new JScrollPane(bulkTable);
        bulkScroll.setBorder(
                BorderFactory.createTitledBorder("Network-Wide Randomized Bulk Benchmarks"));

        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        tablesPanel.add(caseScroll);
        tablesPanel.add(bulkScroll);

        JSplitPane upperSplit =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablesPanel, chartPanel);
        upperSplit.setResizeWeight(0.55);
        upperSplit.setOneTouchExpandable(true);

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Benchmark Console Logs"));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperSplit, logScroll);
        mainSplit.setResizeWeight(0.60);
        mainSplit.setOneTouchExpandable(true);

        add(top, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
    }

    private void runRouteBenchmark() {
        hasRunTimingTest = true;
        runButton.setEnabled(false);
        statusLabel.setText("Running route benchmark tests...");
        caseTableModel.setRowCount(0);
        bulkTableModel.setRowCount(0);
        logArea.setText("");

        SwingWorker<BenchmarkRunResult, Void> worker =
                new SwingWorker<BenchmarkRunResult, Void>() {
                    @Override
                    protected BenchmarkRunResult doInBackground() throws Exception {
                        StringBuilder logBuilder = new StringBuilder();
                        logBuilder.append(
                                "========================================================================\n");
                        logBuilder.append(
                                "         DIJKSTRA VS. BIDIRECTIONAL DIJKSTRA PERFORMANCE BENCHMARK"
                                    + "       \n");
                        logBuilder.append(
                                "========================================================================\n\n");

                        List<RouteService.RouteBenchmarkResult> caseResults = new ArrayList<>();
                        int warmUp = 50;
                        int measured = 5000;

                        logBuilder.append("Starting coursework routing cases benchmark...\n");
                        logBuilder
                                .append("Benchmark configuration: ")
                                .append(warmUp)
                                .append(" warm-up runs, ")
                                .append(measured)
                                .append(" measured runs averaged.\n\n");

                        // Case 1
                        logBuilder.append("Benchmarking Case 1: A1 to A1...\n");
                        RouteService.RouteBenchmarkResult r1 =
                                routeService.benchmarkCase(
                                        "Case 1: A1 to A1",
                                        graph,
                                        topA.get(0).getLocationId(),
                                        topA.get(0).getLocationId(),
                                        new String[0],
                                        warmUp,
                                        measured);
                        caseResults.add(r1);
                        logBuilder.append(formatCaseLog(r1));

                        // Case 2
                        logBuilder.append("Benchmarking Case 2: A1 to A10...\n");
                        RouteService.RouteBenchmarkResult r2 =
                                routeService.benchmarkCase(
                                        "Case 2: A1 to A10",
                                        graph,
                                        topA.get(0).getLocationId(),
                                        topA.get(9).getLocationId(),
                                        new String[0],
                                        warmUp,
                                        measured);
                        caseResults.add(r2);
                        logBuilder.append(formatCaseLog(r2));

                        // Case 3
                        logBuilder.append("Benchmarking Case 3: A1 to B1 via B5...\n");
                        RouteService.RouteBenchmarkResult r3 =
                                routeService.benchmarkCase(
                                        "Case 3: A1 to B1 via B5",
                                        graph,
                                        topA.get(0).getLocationId(),
                                        topB.get(0).getLocationId(),
                                        new String[] {topB.get(4).getLocationId()},
                                        warmUp,
                                        measured);
                        caseResults.add(r3);
                        logBuilder.append(formatCaseLog(r3));

                        // Case 4
                        logBuilder.append("Benchmarking Case 4: A1 to C1 via B5 and C5...\n");
                        RouteService.RouteBenchmarkResult r4 =
                                routeService.benchmarkCase(
                                        "Case 4: A1 to C1 via B5 and C5",
                                        graph,
                                        topA.get(0).getLocationId(),
                                        topC.get(0).getLocationId(),
                                        new String[] {
                                            topB.get(4).getLocationId(), topC.get(4).getLocationId()
                                        },
                                        warmUp,
                                        measured);
                        caseResults.add(r4);
                        logBuilder.append(formatCaseLog(r4));

                        // Randomized Bulk Test
                        logBuilder.append(
                                "========================================================================\n");
                        logBuilder.append(
                                "Starting 1,000-Query Randomized network-wide Bulk Benchmark...\n");
                        logBuilder.append(
                                "========================================================================\n\n");

                        List<String> allNodes = new ArrayList<>(graph.getAllNodes());
                        int queryCount = 1000;
                        Random rand = new Random(42); // Seeded for deterministic stability
                        long originalTotalNs = 0;
                        long bidirectionalTotalNs = 0;
                        int failedVerifications = 0;

                        // Warm up first
                        for (int q = 0; q < 100; q++) {
                            String u = allNodes.get(rand.nextInt(allNodes.size()));
                            String v = allNodes.get(rand.nextInt(allNodes.size()));
                            routeService.findShortestPath(graph, u, v);
                            routeService.findShortestPathBidirectional(graph, u, v);
                        }

                        for (int q = 0; q < queryCount; q++) {
                            String u = allNodes.get(rand.nextInt(allNodes.size()));
                            String v = allNodes.get(rand.nextInt(allNodes.size()));

                            long start = System.nanoTime();
                            RouteResult res1 = routeService.findShortestPath(graph, u, v);
                            long end = System.nanoTime();
                            originalTotalNs += (end - start);

                            start = System.nanoTime();
                            RouteResult res2 =
                                    routeService.findShortestPathBidirectional(graph, u, v);
                            end = System.nanoTime();
                            bidirectionalTotalNs += (end - start);

                            // Verification
                            boolean correct = res1.isReachable() == res2.isReachable();
                            if (correct && res1.isReachable()) {
                                correct =
                                        Math.abs(res1.getTotalCost() - res2.getTotalCost()) < 1e-6;
                            }
                            if (!correct) {
                                failedVerifications++;
                            }
                        }

                        double originalMs = originalTotalNs / 1_000_000.0;
                        double bidirectionalMs = bidirectionalTotalNs / 1_000_000.0;
                        double avgSpeedup = originalMs / bidirectionalMs;

                        logBuilder.append("Random Bulk Test Results:\n");
                        logBuilder
                                .append("  - Total Queries Run: ")
                                .append(queryCount)
                                .append("\n");
                        logBuilder
                                .append("  - Original Dijkstra Total Time: ")
                                .append(String.format("%.2f ms", originalMs))
                                .append(" (Avg ")
                                .append(
                                        String.format(
                                                "%.3f μs/query",
                                                (originalTotalNs / 1000.0) / queryCount))
                                .append(")\n");
                        logBuilder
                                .append("  - Bidirectional Total Time: ")
                                .append(String.format("%.2f ms", bidirectionalMs))
                                .append(" (Avg ")
                                .append(
                                        String.format(
                                                "%.3f μs/query",
                                                (bidirectionalTotalNs / 1000.0) / queryCount))
                                .append(")\n");
                        logBuilder
                                .append("  - Network-wide Speedup Factor: ")
                                .append(String.format("%.2fx", avgSpeedup))
                                .append("\n");
                        logBuilder
                                .append("  - Verification Failures: ")
                                .append(failedVerifications)
                                .append(" / ")
                                .append(queryCount)
                                .append("\n\n");

                        logBuilder
                                .append("Verification completed successfully: ")
                                .append(failedVerifications == 0 ? "ALL PASSED" : "FAILED")
                                .append("\n\nBenchmark finished.");

                        return new BenchmarkRunResult(
                                caseResults,
                                logBuilder.toString(),
                                queryCount,
                                originalMs,
                                bidirectionalMs,
                                avgSpeedup,
                                failedVerifications == 0);
                    }

                    @Override
                    protected void done() {
                        try {
                            BenchmarkRunResult result = get();
                            for (RouteService.RouteBenchmarkResult r : result.caseResults) {
                                caseTableModel.addRow(
                                        new Object[] {
                                            r.getCaseName(),
                                            getStartNode(r.getCaseName()),
                                            getTargetNode(r.getCaseName()),
                                            String.format("%.3f", r.getOriginalAvgTimeUs()),
                                            String.format("%.3f", r.getBidirectionalAvgTimeUs()),
                                            String.format("%.2fx", r.getSpeedup()),
                                            r.isCorrect() ? "PASSED" : "FAILED"
                                        });
                            }

                            bulkTableModel.addRow(
                                    new Object[] {
                                        "1,000 Random Route Queries",
                                        result.queryCount,
                                        String.format("%.2f ms", result.originalMs),
                                        String.format("%.2f ms", result.bidirectionalMs),
                                        String.format("%.2fx", result.avgSpeedup),
                                        result.verified ? "PASSED" : "FAILED (Check logs)"
                                    });

                            chartPanel.setResults(result.caseResults);
                            logArea.setText(result.logText);
                            logArea.setCaretPosition(0);

                            statusLabel.setText("Benchmark tests completed successfully.");
                        } catch (Exception e) {
                            statusLabel.setText("Benchmark failed: " + e.getMessage());
                            logArea.setText("Error during benchmark execution:\n" + e.toString());
                        } finally {
                            runButton.setEnabled(true);
                        }
                    }
                };
        worker.execute();
    }

    private String formatCaseLog(RouteService.RouteBenchmarkResult r) {
        return String.format(
                "  * Original: %.3f μs | Bidirectional: %.3f μs | Speedup: %.2fx | Verification:"
                    + " %s\n\n",
                r.getOriginalAvgTimeUs(),
                r.getBidirectionalAvgTimeUs(),
                r.getSpeedup(),
                r.isCorrect() ? "PASSED" : "FAILED");
    }

    private String getStartNode(String caseName) {
        if (caseName.contains("Case 1")) return topA.get(0).getLocationId();
        if (caseName.contains("Case 2")) return topA.get(0).getLocationId();
        if (caseName.contains("Case 3")) return topA.get(0).getLocationId();
        if (caseName.contains("Case 4")) return topA.get(0).getLocationId();
        return "";
    }

    private String getTargetNode(String caseName) {
        if (caseName.contains("Case 1")) return topA.get(0).getLocationId();
        if (caseName.contains("Case 2")) return topA.get(9).getLocationId();
        if (caseName.contains("Case 3"))
            return topB.get(0).getLocationId() + " (via " + topB.get(4).getLocationId() + ")";
        if (caseName.contains("Case 4"))
            return topC.get(0).getLocationId()
                    + " (via "
                    + topB.get(4).getLocationId()
                    + ", "
                    + topC.get(4).getLocationId()
                    + ")";
        return "";
    }

    private static class BenchmarkRunResult {
        final List<RouteService.RouteBenchmarkResult> caseResults;
        final String logText;
        final int queryCount;
        final double originalMs;
        final double bidirectionalMs;
        final double avgSpeedup;
        final boolean verified;

        BenchmarkRunResult(
                List<RouteService.RouteBenchmarkResult> caseResults,
                String logText,
                int queryCount,
                double originalMs,
                double bidirectionalMs,
                double avgSpeedup,
                boolean verified) {
            this.caseResults = caseResults;
            this.logText = logText;
            this.queryCount = queryCount;
            this.originalMs = originalMs;
            this.bidirectionalMs = bidirectionalMs;
            this.avgSpeedup = avgSpeedup;
            this.verified = verified;
        }
    }
}
