package visualization;

import model.CandidateLocation;

import service.ConsoleReportService;
import service.SortingService;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class SortingTimingPanel extends JPanel {
    private static final String BUBBLE = "Bubble Sort";
    private static final String QUICK = "Quick Sort";
    private static final String MERGE = "Merge Sort";

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new Object[] {
                        "Dataset",
                        "Bubble avg (ms)",
                        "Quick avg (ms)",
                        "Merge avg (ms)",
                        "Top 10 Selected Locations"
                    },
                    0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable timingTable = new JTable(tableModel);

    private final DefaultTableModel stabilityTableModel =
            new DefaultTableModel(
                    new Object[] {
                        "Algorithm",
                        "Mean Time (ms)",
                        "Population Variance (ms²)",
                        "Sample Variance (ms²)",
                        "Std Dev (ms)"
                    },
                    0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable stabilityTable = new JTable(stabilityTableModel);
    private final JButton runButton = new JButton("Run Timing & Stability Test");
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea consoleOutput = new JTextArea();
    private final BarChartPanel chartPanel = new BarChartPanel();
    private boolean hasRunTimingTest = false;

    public SortingTimingPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        runButton.setPreferredSize(new Dimension(240, 36));
        buildLayout();
        runButton.addActionListener(event -> runTimingTest());
    }

    public void runInitialTimingTestIfNeeded() {
        if (!hasRunTimingTest) {
            runTimingTest();
        }
    }

    private void buildLayout() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(runButton);
        top.add(statusLabel);

        timingTable.setRowHeight(36);
        timingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        timingTable.getTableHeader().setReorderingAllowed(false);
        timingTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        timingTable.getColumnModel().getColumn(1).setPreferredWidth(105);
        timingTable.getColumnModel().getColumn(2).setPreferredWidth(105);
        timingTable.getColumnModel().getColumn(3).setPreferredWidth(105);
        timingTable.getColumnModel().getColumn(4).setPreferredWidth(650);
        timingTable.getColumnModel().getColumn(4).setCellRenderer(new MultiLineCellRenderer());

        stabilityTable.setRowHeight(28);
        stabilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        stabilityTable.getTableHeader().setReorderingAllowed(false);
        stabilityTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        stabilityTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        stabilityTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        stabilityTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        stabilityTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        consoleOutput.setEditable(false);
        consoleOutput.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        consoleOutput.setLineWrap(false);

        JScrollPane tableScroll = new JScrollPane(timingTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Actual Task A Timing Results"));

        JScrollPane stabilityScroll = new JScrollPane(stabilityTable);
        stabilityScroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Algorithm Performance Stability (Variance Across Datasets)"));

        JPanel tablesPanel = new JPanel(new java.awt.GridLayout(2, 1, 10, 10));
        tablesPanel.add(tableScroll);
        tablesPanel.add(stabilityScroll);

        JSplitPane upperSplit =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablesPanel, chartPanel);
        upperSplit.setResizeWeight(0.60);
        upperSplit.setOneTouchExpandable(true);

        JScrollPane consoleScroll = new JScrollPane(consoleOutput);
        consoleScroll.setBorder(BorderFactory.createTitledBorder("Command Line Output"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperSplit, consoleScroll);
        splitPane.setResizeWeight(0.55);
        splitPane.setOneTouchExpandable(true);

        add(top, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    public void runTimingTest() {
        hasRunTimingTest = true;
        runButton.setEnabled(false);
        statusLabel.setText("Running actual timing and stability tests...");
        tableModel.setRowCount(0);
        stabilityTableModel.setRowCount(0);
        consoleOutput.setText("");

        SwingWorker<ConsoleReportService.ConsoleReport, Void> worker =
                new SwingWorker<ConsoleReportService.ConsoleReport, Void>() {
                    @Override
                    protected ConsoleReportService.ConsoleReport doInBackground() {
                        return new ConsoleReportService().buildConsoleReport();
                    }

                    @Override
                    protected void done() {
                        try {
                            ConsoleReportService.ConsoleReport report = get();
                            for (SortingService.SortingResult result : report.getSortingResults()) {
                                addResultRow(result);
                            }

                            for (ConsoleReportService.StabilityStats stats :
                                    report.getStabilityStats()) {
                                stabilityTableModel.addRow(
                                        new Object[] {
                                            stats.getAlgorithmName(),
                                            formatMillis(stats.getMean()),
                                            String.format("%.6f", stats.getPopulationVariance()),
                                            String.format("%.6f", stats.getSampleVariance()),
                                            formatMillis(stats.getStdDev())
                                        });
                            }

                            chartPanel.setResults(report.getSortingResults());

                            consoleOutput.setText(report.getText());
                            consoleOutput.setCaretPosition(0);
                            System.out.print(report.getText());

                            statusLabel.setText(" ");
                        } catch (Exception e) {
                            statusLabel.setText("Timing test failed: " + e.getMessage());
                        } finally {
                            runButton.setEnabled(true);
                        }
                    }
                };
        worker.execute();
    }

    private void addResultRow(SortingService.SortingResult result) {
        Map<String, Double> times = result.getAverageTimes();
        double bubble = getTime(times, BUBBLE);
        double quick = getTime(times, QUICK);
        double merge = getTime(times, MERGE);

        tableModel.addRow(
                new Object[] {
                    result.getDatasetName(),
                    formatMillis(bubble),
                    formatMillis(quick),
                    formatMillis(merge),
                    formatTopLocations(result.getTop10Locations())
                });
        updateRowHeights();
    }

    private double getTime(Map<String, Double> times, String sorterName) {
        Double value = times.get(sorterName);
        if (value == null) {
            return 0;
        }
        return value;
    }

    private String formatMillis(double millis) {
        return String.format("%.3f", millis);
    }

    private String formatTopLocations(List<CandidateLocation> locations) {
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(10, locations.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                if (i == 5) {
                    builder.append("\n");
                } else {
                    builder.append(", ");
                }
            }
            CandidateLocation location = locations.get(i);
            builder.append(location.getLocationId())
                    .append(" (")
                    .append(location.getPriorityScore())
                    .append(")");
        }
        return builder.toString();
    }

    private void updateRowHeights() {
        for (int row = 0; row < timingTable.getRowCount(); row++) {
            int preferredHeight = timingTable.getRowHeight();
            for (int column = 0; column < timingTable.getColumnCount(); column++) {
                TableCellRenderer renderer = timingTable.getCellRenderer(row, column);
                Component component = timingTable.prepareRenderer(renderer, row, column);
                preferredHeight =
                        Math.max(preferredHeight, component.getPreferredSize().height + 8);
            }
            timingTable.setRowHeight(row, preferredHeight);
        }
    }

    private static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
        MultiLineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            setText(value == null ? "" : value.toString());
            setFont(table.getFont());
            setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 6, 4, 6));
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
            return this;
        }
    }
}
