package visualization;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VisualizationFrame extends JFrame {
    private final SortingTimingPanel sortingTimingPanel;
    private final RouteTimingPanel routeTimingPanel;

    public VisualizationFrame() {
        super("Urban Infrastructure Inspection System Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        this.sortingTimingPanel = new SortingTimingPanel();
        this.routeTimingPanel = new RouteTimingPanel();

        tabs.addTab("Sorting Animation", new SortingControlPanel());
        tabs.addTab("Sorting Timing", sortingTimingPanel);
        tabs.addTab("Route Graph", new RouteGraphPanel());
        tabs.addTab("Route Timing", routeTimingPanel);

        tabs.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent event) {
                        if (tabs.getSelectedComponent() == sortingTimingPanel) {
                            sortingTimingPanel.runInitialTimingTestIfNeeded();
                        } else if (tabs.getSelectedComponent() == routeTimingPanel) {
                            routeTimingPanel.runInitialTimingTestIfNeeded();
                        }
                    }
                });

        add(tabs, BorderLayout.CENTER);
        setSize(1300, 850);
        setLocationRelativeTo(null);
    }

    public void runStartupBenchmark() {
        sortingTimingPanel.runTimingTest();
    }
}
