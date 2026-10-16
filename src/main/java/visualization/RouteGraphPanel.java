package visualization;

import graph.Graph;

import io.PathReader;

import model.CandidateLocation;
import model.Path;
import model.RouteResult;

import service.RouteService;
import service.SortingService;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

public class RouteGraphPanel extends JPanel {
    private final JComboBox<String> caseBox = new JComboBox<>();
    private final JTextArea detailArea = new JTextArea(6, 80);
    private final PathDiagramPanel diagramPanel = new PathDiagramPanel();
    private final List<RouteCase> routeCases = new ArrayList<>();

    public RouteGraphPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildLayout();
        loadRouteCases();
        wireActions();
        showSelectedCase();
    }

    private void buildLayout() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(new JLabel("Route case:"));
        top.add(caseBox);

        detailArea.setEditable(false);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);

        add(top, BorderLayout.NORTH);
        add(diagramPanel, BorderLayout.CENTER);
        add(new JScrollPane(detailArea), BorderLayout.SOUTH);
    }

    private void wireActions() {
        caseBox.addActionListener(event -> showSelectedCase());
    }

    private void loadRouteCases() {
        SortingService sortingService = new SortingService();
        RouteService routeService = new RouteService();

        SortingService.SortingResult datasetA =
                sortingService.processDataset("Dataset A", "data/candidates_A.csv", 3);
        SortingService.SortingResult datasetB =
                sortingService.processDataset("Dataset B", "data/candidates_B.csv", 3);
        SortingService.SortingResult datasetC =
                sortingService.processDataset("Dataset C", "data/candidates_C.csv", 3);

        List<Path> paths = PathReader.readPaths("data/paths.csv");
        Graph graph = routeService.buildGraph(paths);

        List<CandidateLocation> topA = datasetA.getTop10Locations();
        List<CandidateLocation> topB = datasetB.getTop10Locations();
        List<CandidateLocation> topC = datasetC.getTop10Locations();

        addRouteCase(
                "Case 1: A1 to A1",
                "",
                routeService.findShortestPath(
                        graph, topA.get(0).getLocationId(), topA.get(0).getLocationId()));
        addRouteCase(
                "Case 2: A1 to A10",
                "",
                routeService.findShortestPath(
                        graph, topA.get(0).getLocationId(), topA.get(9).getLocationId()));
        addRouteCase(
                "Case 3: A1 to B1 via B5",
                topB.get(4).getLocationId(),
                routeService.findShortestPathVia(
                        graph,
                        topA.get(0).getLocationId(),
                        topB.get(0).getLocationId(),
                        topB.get(4).getLocationId()));
        addRouteCase(
                "Case 4: A1 to C1 via B5 and C5",
                topB.get(4).getLocationId() + ", " + topC.get(4).getLocationId(),
                routeService.findShortestPathVia(
                        graph,
                        topA.get(0).getLocationId(),
                        topC.get(0).getLocationId(),
                        topB.get(4).getLocationId(),
                        topC.get(4).getLocationId()));
    }

    private void addRouteCase(String name, String waypoints, RouteResult routeResult) {
        RouteCase routeCase = new RouteCase(name, waypoints, routeResult);
        routeCases.add(routeCase);
        caseBox.addItem(name);
    }

    private void showSelectedCase() {
        if (routeCases.isEmpty() || caseBox.getSelectedIndex() < 0) {
            return;
        }

        RouteCase routeCase = routeCases.get(caseBox.getSelectedIndex());
        RouteResult route = routeCase.getRouteResult();
        StringBuilder details = new StringBuilder();
        details.append(routeCase.getName()).append('\n');
        details.append("Start: ").append(route.getStartLocation()).append('\n');
        details.append("Destination: ").append(route.getDestinationLocation()).append('\n');
        details.append("Waypoints: ")
                .append(routeCase.getWaypoints().isEmpty() ? "None" : routeCase.getWaypoints())
                .append('\n');
        details.append("Total cost: ").append(route.getTotalCost()).append('\n');
        details.append("Path: ").append(route.formatPath());

        detailArea.setText(details.toString());
        detailArea.setCaretPosition(0);

        List<String> waypointList = new ArrayList<>();
        if (!routeCase.getWaypoints().isEmpty()) {
            for (String wp : routeCase.getWaypoints().split(",")) {
                waypointList.add(wp.trim());
            }
        }
        diagramPanel.setRoute(route, waypointList);
    }

    private static class RouteCase {
        private final String name;
        private final String waypoints;
        private final RouteResult routeResult;

        RouteCase(String name, String waypoints, RouteResult routeResult) {
            this.name = name;
            this.waypoints = waypoints;
            this.routeResult = routeResult;
        }

        String getName() {
            return name;
        }

        String getWaypoints() {
            return waypoints;
        }

        RouteResult getRouteResult() {
            return routeResult;
        }
    }

    private static class PathDiagramPanel extends JPanel {
        private RouteResult route;
        private List<String> waypoints = new ArrayList<>();
        private double currentProgress = 0.0;
        private Timer animationTimer;

        PathDiagramPanel() {
            setPreferredSize(new Dimension(900, 420));
            setBackground(new Color(250, 252, 255));
            setBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                            "Route Graph Network Visualizer"));
        }

        void setRoute(RouteResult route, List<String> waypoints) {
            this.route = route;
            this.waypoints = waypoints != null ? waypoints : new ArrayList<>();
            startAnimation();
        }

        private void startAnimation() {
            if (animationTimer != null) {
                animationTimer.stop();
            }
            currentProgress = 0.0;

            if (route == null || route.getPath().isEmpty()) {
                repaint();
                return;
            }

            int n = route.getPath().size();
            // 60 FPS animation (roughly 16ms per frame)
            animationTimer =
                    new Timer(
                            16,
                            event -> {
                                // Animate at a steady speed: 1 full segment every 150ms (increment
                                // 0.08 per 16ms frame)
                                currentProgress += 0.08;
                                if (currentProgress >= n) {
                                    currentProgress = n;
                                    animationTimer.stop();
                                }
                                repaint();
                            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (route == null || !route.isReachable() || route.getPath().isEmpty()) {
                g.setColor(new Color(120, 130, 140));
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                g.drawString("No route selected.", 30, 35);
                g.dispose();
                return;
            }

            List<String> path = route.getPath();
            int n = path.size();
            int width = getWidth();
            int height = getHeight();

            int cx = width / 2;
            int cy = height / 2;
            int r = Math.min(width, height) / 2 - 80;
            if (r < 50) {
                r = 50;
            }

            // Calculate vertex coordinates
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                if (n <= 1) {
                    xs[i] = cx;
                    ys[i] = cy;
                } else {
                    double angle = -Math.PI / 2 + i * 2 * Math.PI / n;
                    xs[i] = cx + (int) Math.round(r * Math.cos(angle));
                    ys[i] = cy + (int) Math.round(r * Math.sin(angle));
                }
            }

            // 1. Draw dashed background polygon with subtle transparency
            if (n >= 3) {
                g.setStroke(
                        new BasicStroke(
                                1.5f,
                                BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_MITER,
                                10f,
                                new float[] {5f, 5f},
                                0f));
                g.setColor(new Color(180, 200, 220, 120));
                for (int i = 0; i < n; i++) {
                    int next = (i + 1) % n;
                    g.drawLine(xs[i], ys[i], xs[next], ys[next]);
                }
            }

            // 2. Draw actual path with bold lines and directional arrowheads progressively
            int nodeRadius = 16;
            g.setStroke(new BasicStroke(3.0f));
            for (int i = 0; i < n - 1; i++) {
                if (currentProgress < i) {
                    break;
                }

                int x1 = xs[i];
                int y1 = ys[i];
                int x2 = xs[i + 1];
                int y2 = ys[i + 1];

                double segmentProgress = currentProgress - i;
                if (segmentProgress >= 1.0) {
                    // This segment is fully animated, draw a solid bold path line with standard
                    // arrow
                    g.setColor(new Color(86, 128, 178));
                    drawArrow(g, x1, y1, x2, y2, nodeRadius);
                } else if (segmentProgress > 0) {
                    // Segment is partially animated (growing)
                    int targetX = x1 + (int) Math.round(segmentProgress * (x2 - x1));
                    int targetY = y1 + (int) Math.round(segmentProgress * (y2 - y1));
                    g.setColor(new Color(86, 128, 178, 180));
                    g.drawLine(x1, y1, targetX, targetY);
                    drawArrowhead(g, x1, y1, targetX, targetY);
                }
            }

            // 3. Draw nodes dynamically with progressive fade-in transition
            FontMetrics metrics = g.getFontMetrics();
            for (int i = 0; i < n; i++) {
                double f_i;
                if (currentProgress >= i + 1) {
                    f_i = 1.0;
                } else if (currentProgress <= i) {
                    f_i = 0.0;
                } else {
                    f_i = currentProgress - i;
                }

                if (f_i <= 0) {
                    continue;
                }

                String label = path.get(i);
                boolean isStart = i == 0;
                boolean isDest = i == n - 1;
                boolean isWaypoint = waypoints.contains(label);

                Color nodeColor;
                if (isStart || isDest) {
                    nodeColor = new Color(83, 159, 107); // Endpoint: green
                } else if (isWaypoint) {
                    nodeColor = new Color(236, 138, 56); // Waypoint: orange
                } else {
                    nodeColor = new Color(94, 126, 163); // Intermediate: standard slate blue
                }

                int animatedRadius = (int) Math.round(nodeRadius * f_i);
                if (animatedRadius < 2) {
                    animatedRadius = 2;
                }

                int alphaVal = (int) Math.round(f_i * 255);
                g.setColor(
                        new Color(
                                nodeColor.getRed(),
                                nodeColor.getGreen(),
                                nodeColor.getBlue(),
                                alphaVal));
                g.fillOval(
                        xs[i] - animatedRadius,
                        ys[i] - animatedRadius,
                        animatedRadius * 2,
                        animatedRadius * 2);

                g.setStroke(new BasicStroke(1.5f));
                g.setColor(new Color(255, 255, 255, alphaVal));
                g.drawOval(
                        xs[i] - animatedRadius,
                        ys[i] - animatedRadius,
                        animatedRadius * 2,
                        animatedRadius * 2);

                // 4. Draw radial, center-aligned labels with fade-in transition
                String labelText = label + (isWaypoint ? " (WP)" : "");
                double angle = (n <= 1) ? -Math.PI / 2 : -Math.PI / 2 + i * 2 * Math.PI / n;
                int labelDist = r + 24;
                int lx = cx + (int) Math.round(labelDist * Math.cos(angle));
                int ly = cy + (int) Math.round(labelDist * Math.sin(angle));

                int labelX = lx - metrics.stringWidth(labelText) / 2;
                int labelY = ly + (metrics.getAscent() - metrics.getDescent()) / 2;

                g.setColor(new Color(40, 48, 60, alphaVal));
                g.drawString(labelText, labelX, labelY);
            }

            g.dispose();
        }

        private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, int nodeRadius) {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.hypot(dx, dy);
            if (length < 1) return;

            double shrinkFactor = (length - nodeRadius) / length;
            if (shrinkFactor < 0) shrinkFactor = 0;
            int targetX = x1 + (int) Math.round(dx * shrinkFactor);
            int targetY = y1 + (int) Math.round(dy * shrinkFactor);

            g.drawLine(x1, y1, targetX, targetY);
            drawArrowhead(g, x1, y1, targetX, targetY);
        }

        private void drawArrowhead(Graphics2D g, int x1, int y1, int targetX, int targetY) {
            double dx = targetX - x1;
            double dy = targetY - y1;
            double angle = Math.atan2(dy, dx);
            double arrowSize = 8;
            int ax1 = targetX - (int) Math.round(arrowSize * Math.cos(angle - Math.PI / 6));
            int ay1 = targetY - (int) Math.round(arrowSize * Math.sin(angle - Math.PI / 6));
            int ax2 = targetX - (int) Math.round(arrowSize * Math.cos(angle + Math.PI / 6));
            int ay2 = targetY - (int) Math.round(arrowSize * Math.sin(angle + Math.PI / 6));

            g.fillPolygon(new int[] {targetX, ax1, ax2}, new int[] {targetY, ay1, ay2}, 3);
        }
    }
}
