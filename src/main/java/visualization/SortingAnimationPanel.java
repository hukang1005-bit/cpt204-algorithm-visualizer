package visualization;

import model.CandidateLocation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

public class SortingAnimationPanel extends JPanel {
    private static final Color BACKGROUND = new Color(250, 252, 255);
    private static final Color AXIS_COLOR = new Color(70, 78, 90);
    private static final Color BAR_COLOR = new Color(94, 126, 163);
    private static final Color ACTIVE_COLOR = new Color(236, 138, 56);
    private static final Color SORTED_COLOR = new Color(83, 159, 107);
    private static final Color MERGE_RANGE_COLOR = new Color(137, 190, 155);

    private List<CandidateLocation> originalData = Collections.emptyList();
    private List<CandidateLocation> currentData = Collections.emptyList();
    private int firstIndex = -1;
    private int secondIndex = -1;
    private int sortedStart = -1;
    private int sortedEnd = -1;
    private Set<Integer> sortedIndices = Collections.emptySet();
    private int minScore = 0;
    private int maxScore = 1;

    public SortingAnimationPanel() {
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(900, 560));
    }

    public void setOriginalData(List<CandidateLocation> locations) {
        this.originalData = new ArrayList<>(locations);
        this.currentData = new ArrayList<>(locations);
        this.firstIndex = -1;
        this.secondIndex = -1;
        this.sortedStart = -1;
        this.sortedEnd = -1;
        this.sortedIndices = Collections.emptySet();
        updateScoreRange(locations);
        repaint();
    }

    public void setStep(SortStep step) {
        this.currentData = new ArrayList<>(step.getSnapshot());
        this.firstIndex = step.getFirstIndex();
        this.secondIndex = step.getSecondIndex();
        this.sortedStart = step.getSortedStart();
        this.sortedEnd = step.getSortedEnd();
        this.sortedIndices = new HashSet<>(step.getSortedIndices());
        repaint();
    }

    public void resetView() {
        this.currentData = new ArrayList<>(originalData);
        this.firstIndex = -1;
        this.secondIndex = -1;
        this.sortedStart = -1;
        this.sortedEnd = -1;
        this.sortedIndices = Collections.emptySet();
        repaint();
    }

    private void updateScoreRange(List<CandidateLocation> locations) {
        if (locations.isEmpty()) {
            minScore = 0;
            maxScore = 1;
            return;
        }

        minScore = Integer.MAX_VALUE;
        maxScore = Integer.MIN_VALUE;
        for (CandidateLocation location : locations) {
            minScore = Math.min(minScore, location.getPriorityScore());
            maxScore = Math.max(maxScore, location.getPriorityScore());
        }

        if (minScore == maxScore) {
            minScore = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int left = 54;
        int right = 18;
        int top = 24;
        int bottom = 46;
        int plotWidth = Math.max(1, getWidth() - left - right);
        int plotHeight = Math.max(1, getHeight() - top - bottom);

        drawAxes(g, left, top, plotWidth, plotHeight);
        drawBars(g, left, top, plotWidth, plotHeight);

        g.dispose();
    }

    private void drawAxes(Graphics2D g, int left, int top, int plotWidth, int plotHeight) {
        g.setColor(AXIS_COLOR);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(left, top, left, top + plotHeight);
        g.drawLine(left, top + plotHeight, left + plotWidth, top + plotHeight);

        FontMetrics metrics = g.getFontMetrics();
        String yLabel = "priority_score";
        String xLabel = "array index";
        g.drawString(String.valueOf(maxScore), 8, top + metrics.getAscent());
        g.drawString(String.valueOf(minScore), 8, top + plotHeight);
        g.drawString(yLabel, 8, top + plotHeight / 2);
        g.drawString(
                xLabel, left + plotWidth / 2 - metrics.stringWidth(xLabel) / 2, getHeight() - 12);
        g.drawString("0", left, top + plotHeight + 18);
        if (!currentData.isEmpty()) {
            String lastIndex = String.valueOf(currentData.size() - 1);
            g.drawString(
                    lastIndex,
                    left + plotWidth - metrics.stringWidth(lastIndex),
                    top + plotHeight + 18);
        }
    }

    private void drawBars(Graphics2D g, int left, int top, int plotWidth, int plotHeight) {
        if (currentData.isEmpty()) {
            return;
        }

        int size = currentData.size();
        double barWidth = plotWidth / (double) size;
        double range = Math.max(1, maxScore - minScore);

        for (int i = 0; i < size; i++) {
            CandidateLocation location = currentData.get(i);
            double normalized = (location.getPriorityScore() - minScore) / range;
            int barHeight = Math.max(1, (int) Math.round(normalized * plotHeight));
            int x = left + (int) Math.floor(i * barWidth);
            int nextX = left + (int) Math.floor((i + 1) * barWidth);
            int width = Math.max(1, nextX - x);
            int y = top + plotHeight - barHeight;

            g.setColor(colorForIndex(i));
            g.fillRect(x, y, width, barHeight);
        }
    }

    private Color colorForIndex(int index) {
        if (index == firstIndex || index == secondIndex) {
            return ACTIVE_COLOR;
        }

        if (sortedIndices.contains(index)) {
            return SORTED_COLOR;
        }

        if (sortedStart >= 0
                && sortedEnd >= sortedStart
                && index >= sortedStart
                && index <= sortedEnd) {
            if (firstIndex >= 0 && secondIndex < 0 && sortedEnd > sortedStart) {
                return MERGE_RANGE_COLOR;
            }
            return SORTED_COLOR;
        }

        return BAR_COLOR;
    }
}
