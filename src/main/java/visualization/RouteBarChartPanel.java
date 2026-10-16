package visualization;

import service.RouteService;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class RouteBarChartPanel extends JPanel {
    private static final String ORIGINAL = "Original Dijkstra";
    private static final String BIDIRECTIONAL = "Bidirectional Dijkstra";

    private List<RouteService.RouteBenchmarkResult> results = new ArrayList<>();
    private int hoveredGroupIndex = -1;
    private int hoveredBarIndex = -1;

    private static class BarRect {
        int groupIndex;
        int barIndex;
        Shape shape;
        String caseName;
        String algorithmName;
        double value;

        BarRect(
                int groupIndex,
                int barIndex,
                Shape shape,
                String caseName,
                String algorithmName,
                double value) {
            this.groupIndex = groupIndex;
            this.barIndex = barIndex;
            this.shape = shape;
            this.caseName = caseName;
            this.algorithmName = algorithmName;
            this.value = value;
        }
    }

    private final List<BarRect> barRects = new ArrayList<>();

    public RouteBarChartPanel() {
        setBackground(new Color(250, 252, 255));
        setOpaque(true);
        setupMouseListeners();
        setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(210, 215, 220), 1, true),
                        "Routing Runtime Comparison (μs)"));
    }

    public void setResults(List<RouteService.RouteBenchmarkResult> results) {
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
                                                    + " #2980b9;'>%.3f μs</b></body></html>",
                                                found.caseName, found.algorithmName, found.value));
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

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (results == null || results.isEmpty()) {
            g2d.setColor(new Color(110, 115, 125));
            g2d.setFont(new Font("SansSerif", Font.ITALIC, 14));
            String msg = "No benchmark results available. Click run button to generate.";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
            return;
        }

        int topMargin = 50;
        int bottomMargin = 60;
        int leftMargin = 75;
        int rightMargin = 30;

        int chartWidth = width - leftMargin - rightMargin;
        int chartHeight = height - topMargin - bottomMargin;

        if (chartWidth <= 0 || chartHeight <= 0) return;

        double maxVal = 0.001;
        for (RouteService.RouteBenchmarkResult res : results) {
            if (res.getOriginalAvgTimeUs() > maxVal) {
                maxVal = res.getOriginalAvgTimeUs();
            }
            if (res.getBidirectionalAvgTimeUs() > maxVal) {
                maxVal = res.getBidirectionalAvgTimeUs();
            }
        }
        double maxTime = maxVal * 1.15;

        barRects.clear();

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

            if (k > 0) {
                g2d.setColor(new Color(210, 215, 220));
                g2d.setStroke(dashedStroke);
                g2d.drawLine(leftMargin, y, leftMargin + chartWidth, y);
            }

            g2d.setColor(new Color(110, 115, 125));
            String label = String.format("%.1f μs", val);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(label, leftMargin - fm.stringWidth(label) - 10, y + 4);
        }

        g2d.setStroke(originalStroke);
        g2d.setColor(new Color(210, 215, 220));
        g2d.drawLine(
                leftMargin - 5,
                topMargin + chartHeight,
                leftMargin + chartWidth,
                topMargin + chartHeight);
        g2d.drawLine(leftMargin, topMargin, leftMargin, topMargin + chartHeight + 5);

        int numGroups = results.size();
        double groupWidth = (double) chartWidth / numGroups;
        double groupSpacing = groupWidth * 0.22;
        double usableWidth = groupWidth - groupSpacing;

        String[] algorithms = {ORIGINAL, BIDIRECTIONAL};
        Color[][] gradientColors = {
            {new Color(94, 126, 163), new Color(140, 170, 200)}, // Original: Slate Blue
            {new Color(83, 159, 107), new Color(130, 200, 150)} // Bidirectional: Emerald Green
        };

        double barWidth = usableWidth / algorithms.length;
        double barSpacing = 3.0;

        for (int i = 0; i < numGroups; i++) {
            RouteService.RouteBenchmarkResult res = results.get(i);
            double groupX = leftMargin + i * groupWidth + groupSpacing / 2;

            g2d.setColor(new Color(40, 48, 60));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));

            // Clean up the group labels
            String groupLabel = res.getCaseName();
            if (groupLabel.contains(":")) {
                groupLabel = groupLabel.substring(0, groupLabel.indexOf(":")).trim();
            }
            FontMetrics fmGroup = g2d.getFontMetrics();
            int labelX = (int) (groupX + usableWidth / 2 - fmGroup.stringWidth(groupLabel) / 2);
            g2d.drawString(groupLabel, labelX, topMargin + chartHeight + 22);

            for (int j = 0; j < algorithms.length; j++) {
                double val =
                        (j == 0) ? res.getOriginalAvgTimeUs() : res.getBidirectionalAvgTimeUs();

                double barHeight = (val / maxTime) * chartHeight;
                double barX = groupX + j * barWidth;
                double barY = topMargin + chartHeight - barHeight;

                if (barHeight > 0) {
                    double finalBarWidth = barWidth - barSpacing;
                    double cornerRadius = Math.min(8.0, finalBarWidth / 2.0);

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

                    barRects.add(
                            new BarRect(i, j, roundBar, res.getCaseName(), algorithms[j], val));

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
                        g2d.fill(flatBottom);
                    }

                    if (hoveredGroupIndex == i && hoveredBarIndex == j) {
                        g2d.setColor(new Color(255, 255, 255, 80));
                        g2d.fill(roundBar);
                        if (barHeight > cornerRadius) {
                            g2d.fill(flatBottom);
                        }

                        g2d.setColor(new Color(51, 102, 204));
                        g2d.setStroke(new BasicStroke(2.0f));
                        g2d.draw(roundBar);
                        g2d.setStroke(originalStroke);
                    }

                    g2d.setColor(new Color(110, 115, 125));
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    String valLabel = String.format("%.2f", val);
                    FontMetrics fmVal = g2d.getFontMetrics();
                    int valX = (int) (barX + (finalBarWidth - fmVal.stringWidth(valLabel)) / 2);
                    g2d.drawString(valLabel, valX, (int) barY - 4);
                }
            }
        }

        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        int legendItemSpacing = 160;
        int totalLegendWidth = algorithms.length * legendItemSpacing;
        int startLegendX = (width - totalLegendWidth) / 2;
        int legendY = height - 15;

        for (int k = 0; k < algorithms.length; k++) {
            int currentX = startLegendX + k * legendItemSpacing;
            g2d.setColor(gradientColors[k][0]);
            g2d.fillRoundRect(currentX, legendY - 8, 12, 12, 4, 4);

            g2d.setColor(new Color(110, 115, 125));
            g2d.drawString(algorithms[k], currentX + 18, legendY + 2);
        }
    }
}
