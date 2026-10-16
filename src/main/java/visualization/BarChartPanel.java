package visualization;

import service.SortingService;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

public class BarChartPanel extends JPanel {
    private static final String BUBBLE = "Bubble Sort";
    private static final String QUICK = "Quick Sort";
    private static final String MERGE = "Merge Sort";

    private List<SortingService.SortingResult> results = new ArrayList<>();
    private int hoveredGroupIndex = -1;
    private int hoveredBarIndex = -1;

    private static class BarRect {
        int groupIndex;
        int barIndex;
        Shape shape;
        String datasetName;
        String algorithmName;
        double value;

        BarRect(
                int groupIndex,
                int barIndex,
                Shape shape,
                String datasetName,
                String algorithmName,
                double value) {
            this.groupIndex = groupIndex;
            this.barIndex = barIndex;
            this.shape = shape;
            this.datasetName = datasetName;
            this.algorithmName = algorithmName;
            this.value = value;
        }
    }

    private final List<BarRect> barRects = new ArrayList<>();

    public BarChartPanel() {
        setBackground(new Color(250, 252, 255));
        setOpaque(true);
        setupMouseListeners();
        setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(210, 215, 220), 1, true),
                        "Sorting Runtime Comparison (ms)"));
    }

    public void setResults(List<SortingService.SortingResult> results) {
        this.results = results != null ? results : new ArrayList<>();
        repaint();
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter =
                new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent event) {
                        if (barRects.isEmpty()) return;

                        int nextHoveredGroup = -1;
                        int nextHoveredBar = -1;
                        BarRect found = null;

                        for (BarRect r : barRects) {
                            if (r.shape.contains(event.getPoint())) {
                                nextHoveredGroup = r.groupIndex;
                                nextHoveredBar = r.barIndex;
                                found = r;
                                break;
                            }
                        }

                        if (nextHoveredGroup != hoveredGroupIndex
                                || nextHoveredBar != hoveredBarIndex) {
                            hoveredGroupIndex = nextHoveredGroup;
                            hoveredBarIndex = nextHoveredBar;
                            if (found != null) {
                                setToolTipText(
                                        String.format(
                                                "<html><body style='font-family: sans-serif;"
                                                    + " padding: 4px;'><b style='color:"
                                                    + " #2c3e50;'>%s</b><br><span style='color:"
                                                    + " #7f8c8d;'>%s:</span> <b style='color:"
                                                    + " #2980b9;'>%.3f ms</b></body></html>",
                                                found.datasetName,
                                                found.algorithmName,
                                                found.value));
                            } else {
                                setToolTipText(null);
                            }
                            repaint();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent event) {
                        if (hoveredGroupIndex != -1 || hoveredBarIndex != -1) {
                            hoveredGroupIndex = -1;
                            hoveredBarIndex = -1;
                            setToolTipText(null);
                            repaint();
                        }
                    }
                };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable Anti-Aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Check if there is data
        if (results == null || results.isEmpty()) {
            g2d.setColor(new Color(110, 115, 125));
            g2d.setFont(new Font("SansSerif", Font.ITALIC, 14));
            String msg = "No benchmark results available. Click run button to generate.";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
            return;
        }

        // Margin & usable drawing space
        int topMargin = 60;
        int bottomMargin = 60;
        int leftMargin = 75;
        int rightMargin = 30;

        int chartWidth = width - leftMargin - rightMargin;
        int chartHeight = height - topMargin - bottomMargin;

        if (chartWidth <= 0 || chartHeight <= 0) return;

        // Find Maximum average time for Y-axis scaling
        double maxVal = 0.001; // Avoid divide-by-zero
        for (SortingService.SortingResult res : results) {
            for (double time : res.getAverageTimes().values()) {
                if (time > maxVal) {
                    maxVal = time;
                }
            }
        }
        // Round up maximum for clean axis labels
        double maxTime = Math.max(0.1, maxVal * 1.15); // Add 15% head room

        // Clear previous shapes for hover detection
        barRects.clear();

        // 1. Draw Grid Lines and Y-Axis Labels
        int numTicks = 5;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        Stroke originalStroke = g2d.getStroke();
        Stroke dashedStroke =
                new BasicStroke(
                        1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {5}, 0);

        for (int k = 0; k < numTicks; k++) {
            double ratio = (double) k / (numTicks - 1);
            double val = ratio * maxTime;
            int y = topMargin + chartHeight - (int) (ratio * chartHeight);

            // Gridline
            if (k > 0) {
                g2d.setColor(new Color(210, 215, 220)); // Light gray grid
                g2d.setStroke(dashedStroke);
                g2d.drawLine(leftMargin, y, leftMargin + chartWidth, y);
            }

            // Y-axis label
            g2d.setColor(new Color(110, 115, 125));
            String label = String.format("%.3f ms", val);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(label, leftMargin - fm.stringWidth(label) - 10, y + 4);
        }

        // Draw solid X-axis line at 0ms
        g2d.setStroke(originalStroke);
        g2d.setColor(new Color(210, 215, 220));
        g2d.drawLine(
                leftMargin - 5,
                topMargin + chartHeight,
                leftMargin + chartWidth,
                topMargin + chartHeight);
        // Draw solid Y-axis line
        g2d.drawLine(leftMargin, topMargin, leftMargin, topMargin + chartHeight + 5);

        // 2. Draw Grouped Bars
        int numGroups = results.size();
        double groupWidth = (double) chartWidth / numGroups;
        double groupSpacing = groupWidth * 0.18;
        double usableWidth = groupWidth - groupSpacing;

        String[] algorithms = {BUBBLE, QUICK, MERGE};
        Color[][] gradientColors = {
            {new Color(255, 95, 109), new Color(255, 195, 113)}, // Bubble: Warm Red/Orange
            {new Color(111, 66, 193), new Color(155, 103, 233)}, // Quick: Deep Purple/Indigo
            {new Color(32, 201, 151), new Color(40, 167, 69)} // Merge: Fresh Teal/Green
        };

        double barWidth = usableWidth / algorithms.length;
        double barSpacing = 2.0;

        for (int i = 0; i < numGroups; i++) {
            SortingService.SortingResult res = results.get(i);
            double groupX = leftMargin + i * groupWidth + groupSpacing / 2;

            // Draw group label (Dataset name)
            g2d.setColor(new Color(40, 48, 60));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            String groupLabel = res.getDatasetName();
            FontMetrics fmGroup = g2d.getFontMetrics();
            int labelX = (int) (groupX + usableWidth / 2 - fmGroup.stringWidth(groupLabel) / 2);
            g2d.drawString(groupLabel, labelX, topMargin + chartHeight + 22);

            Map<String, Double> times = res.getAverageTimes();

            for (int j = 0; j < algorithms.length; j++) {
                String alg = algorithms[j];
                Double timeVal = times.get(alg);
                double val = (timeVal != null) ? timeVal : 0.0;

                double barHeight = (val / maxTime) * chartHeight;
                double barX = groupX + j * barWidth;
                double barY = topMargin + chartHeight - barHeight;

                if (barHeight > 0) {
                    double finalBarWidth = barWidth - barSpacing;
                    double cornerRadius = Math.min(8.0, finalBarWidth / 2.0);

                    // Create top-rounded bar shape
                    RoundRectangle2D roundBar =
                            new RoundRectangle2D.Double(
                                    barX,
                                    barY,
                                    finalBarWidth,
                                    barHeight,
                                    cornerRadius,
                                    cornerRadius);
                    Rectangle2D flatBottom =
                            new Rectangle2D.Double(
                                    barX,
                                    barY + barHeight - cornerRadius,
                                    finalBarWidth,
                                    cornerRadius);

                    // Store shapes for hit-testing
                    barRects.add(new BarRect(i, j, roundBar, res.getDatasetName(), alg, val));

                    // Fill gradient
                    GradientPaint gp =
                            new GradientPaint(
                                    (float) barX,
                                    (float) barY,
                                    gradientColors[j][0],
                                    (float) barX,
                                    (float) (barY + barHeight),
                                    gradientColors[j][1]);
                    g2d.setPaint(gp);
                    g2d.fill(roundBar);
                    if (barHeight > cornerRadius) {
                        g2d.fill(flatBottom); // Flat bottom corner replacement
                    }

                    // Hover effect overlay
                    if (hoveredGroupIndex == i && hoveredBarIndex == j) {
                        g2d.setColor(new Color(255, 255, 255, 80));
                        g2d.fill(roundBar);
                        if (barHeight > cornerRadius) {
                            g2d.fill(flatBottom);
                        }

                        g2d.setColor(new Color(51, 102, 204)); // Safe classic blue highlight border
                        g2d.setStroke(new BasicStroke(2.0f));
                        g2d.draw(roundBar);
                        g2d.setStroke(originalStroke);
                    }

                    // Value label on top of the bar
                    g2d.setColor(new Color(110, 115, 125));
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    String valLabel = String.format("%.3f", val);
                    FontMetrics fmVal = g2d.getFontMetrics();
                    int valX = (int) (barX + (finalBarWidth - fmVal.stringWidth(valLabel)) / 2);
                    g2d.drawString(valLabel, valX, (int) barY - 4);
                }
            }
        }

        // 3. Draw Legend at the Bottom Centered
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        int legendItemSpacing = 110;
        int totalLegendWidth = algorithms.length * legendItemSpacing;
        int startLegendX = (width - totalLegendWidth) / 2;
        int legendY = height - 15;

        for (int k = 0; k < algorithms.length; k++) {
            int currentX = startLegendX + k * legendItemSpacing;
            // Legend box color
            g2d.setColor(gradientColors[k][0]);
            g2d.fillRoundRect(currentX, legendY - 8, 12, 12, 4, 4);

            // Legend text
            g2d.setColor(new Color(110, 115, 125));
            g2d.drawString(algorithms[k], currentX + 18, legendY + 2);
        }
    }
}
