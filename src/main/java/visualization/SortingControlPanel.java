package visualization;

import io.CandidateCSVReader;

import model.CandidateLocation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SortingControlPanel extends JPanel {
    private static final String[] DATASET_NAMES = {"Dataset A", "Dataset B", "Dataset C"};
    private static final String[] DATASET_FILES = {
        "data/candidates_A.csv", "data/candidates_B.csv", "data/candidates_C.csv"
    };

    private final AnimatedSorter[] sorters = {
        new AnimatedBubbleSorter(), new AnimatedQuickSorter(), new AnimatedMergeSorter()
    };

    private final SortingAnimationPanel animationPanel = new SortingAnimationPanel();
    private final JComboBox<String> datasetBox = new JComboBox<>(DATASET_NAMES);
    private final JComboBox<String> sorterBox =
            new JComboBox<>(
                    new String[] {
                        sorters[0].getName(), sorters[1].getName(), sorters[2].getName()
                    });
    private final JButton startButton = new JButton("Start");
    private final JButton playPauseButton = new JButton("Pause");
    private final JButton resetButton = new JButton("Reset");
    private final JSlider speedSlider = new JSlider(1, 500, 60);
    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel stepLabel = new JLabel("Step: 0");
    private final JTextArea topPreview = new JTextArea(4, 80);
    private final BlockingQueue<SortStep> stepQueue = new ArrayBlockingQueue<>(800);
    private final Timer timer;

    private volatile boolean cancelled = false;
    private volatile boolean workerFinished = false;
    private Thread workerThread;
    private long stepCount = 0;
    private List<CandidateLocation> originalLocations = new ArrayList<>();

    public SortingControlPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        timer = new Timer(30, event -> consumeSteps());
        buildLayout();
        wireActions();
        loadSelectedDataset();
        playPauseButton.setEnabled(false);
    }

    private void buildLayout() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        speedSlider.setMajorTickSpacing(100);
        speedSlider.setPaintTicks(true);

        controls.add(new JLabel("Dataset:"));
        controls.add(datasetBox);
        controls.add(new JLabel("Algorithm:"));
        controls.add(sorterBox);
        controls.add(startButton);
        controls.add(playPauseButton);
        controls.add(resetButton);
        controls.add(new JLabel("Speed:"));
        controls.add(speedSlider);
        controls.add(stepLabel);

        topPreview.setEditable(false);
        topPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        topPreview.setLineWrap(true);
        topPreview.setWrapStyleWord(true);

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.add(statusLabel, BorderLayout.NORTH);

        JScrollPane topScroll = new JScrollPane(topPreview);
        topScroll.setBorder(BorderFactory.createTitledBorder("Current Top 10 Preview"));
        bottom.add(topScroll, BorderLayout.CENTER);

        add(controls, BorderLayout.NORTH);
        add(animationPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void wireActions() {
        datasetBox.addActionListener(event -> resetAnimation());
        sorterBox.addActionListener(event -> resetAnimation());
        startButton.addActionListener(event -> startAnimation());
        playPauseButton.addActionListener(event -> togglePlayback());
        resetButton.addActionListener(event -> resetAnimation());
    }

    private void loadSelectedDataset() {
        int index = datasetBox.getSelectedIndex();
        originalLocations = CandidateCSVReader.readCandidates(DATASET_FILES[index]);
        animationPanel.setOriginalData(originalLocations);
        updateTopPreview(originalLocations);
        statusLabel.setText(
                "Ready: "
                        + DATASET_NAMES[index]
                        + " contains "
                        + originalLocations.size()
                        + " locations.");
        stepLabel.setText("Step: 0");
    }

    private void startAnimation() {
        cancelWorker();
        stepQueue.clear();
        stepCount = 0;
        workerFinished = false;
        cancelled = false;
        loadSelectedDataset();

        final AnimatedSorter sorter = sorters[sorterBox.getSelectedIndex()];
        final List<CandidateLocation> workingCopy = new ArrayList<>(originalLocations);
        statusLabel.setText("Running " + sorter.getName() + "...");
        playPauseButton.setText("Pause");
        playPauseButton.setEnabled(true);
        timer.start();

        workerThread =
                new Thread(
                        () -> {
                            sorter.sort(
                                    workingCopy,
                                    new SortStepListener() {
                                        @Override
                                        public void onStep(SortStep step) {
                                            while (!isCancelled()) {
                                                try {
                                                    if (stepQueue.offer(
                                                            step, 100, TimeUnit.MILLISECONDS)) {
                                                        return;
                                                    }
                                                } catch (InterruptedException e) {
                                                    Thread.currentThread().interrupt();
                                                    return;
                                                }
                                            }
                                        }

                                        @Override
                                        public boolean isCancelled() {
                                            return cancelled
                                                    || Thread.currentThread().isInterrupted();
                                        }
                                    });

                            workerFinished = true;
                            SwingUtilities.invokeLater(
                                    () -> {
                                        if (!cancelled && stepQueue.isEmpty()) {
                                            statusLabel.setText(sorter.getName() + " completed.");
                                        }
                                    });
                        },
                        "sorting-animation-worker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    private void togglePlayback() {
        if (timer.isRunning()) {
            timer.stop();
            playPauseButton.setText("Play");
            statusLabel.setText("Paused");
        } else {
            timer.start();
            playPauseButton.setText("Pause");
            statusLabel.setText(
                    "Running " + sorters[sorterBox.getSelectedIndex()].getName() + "...");
        }
    }

    private void resetAnimation() {
        cancelWorker();
        stepQueue.clear();
        timer.stop();
        workerFinished = false;
        stepCount = 0;
        playPauseButton.setText("Pause");
        playPauseButton.setEnabled(false);
        loadSelectedDataset();
        animationPanel.resetView();
    }

    private void cancelWorker() {
        cancelled = true;
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
        }
    }

    private void consumeSteps() {
        int stepsToConsume = speedSlider.getValue();
        SortStep latest = null;

        for (int i = 0; i < stepsToConsume; i++) {
            SortStep step = stepQueue.poll();
            if (step == null) {
                break;
            }
            latest = step;
            stepCount++;
        }

        if (latest != null) {
            animationPanel.setStep(latest);
            updateTopPreview(latest.getSnapshot());
            stepLabel.setText("Step: " + stepCount);
            statusLabel.setText(
                    sorters[sorterBox.getSelectedIndex()].getName()
                            + ": "
                            + latest.getDescription());
        }

        if (workerFinished && stepQueue.isEmpty()) {
            timer.stop();
            playPauseButton.setEnabled(false);
            playPauseButton.setText("Pause");
            if (!cancelled) {
                statusLabel.setText(
                        sorters[sorterBox.getSelectedIndex()].getName()
                                + " completed. Final order is shown.");
            }
        }
    }

    private void updateTopPreview(List<CandidateLocation> locations) {
        StringBuilder builder = new StringBuilder("Current first 10: ");
        int limit = Math.min(10, locations.size());
        for (int i = 0; i < limit; i++) {
            CandidateLocation location = locations.get(i);
            if (i > 0) {
                builder.append(" | ");
            }
            builder.append(location.getLocationId())
                    .append("(")
                    .append(location.getPriorityScore())
                    .append(")");
        }
        topPreview.setText(builder.toString());
    }
}
