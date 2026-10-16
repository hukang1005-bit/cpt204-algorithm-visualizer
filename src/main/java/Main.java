import service.ConsoleReportService;

import visualization.VisualizationFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--console".equals(args[0])) {
            printConsoleSummary();
            return;
        }

        SwingUtilities.invokeLater(
                () -> {
                    VisualizationFrame frame = new VisualizationFrame();
                    frame.setVisible(true);
                    frame.runStartupBenchmark();
                });
    }

    private static void printConsoleSummary() {
        System.out.print(new ConsoleReportService().buildConsoleReport().getText());
    }
}
