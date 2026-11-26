package com.scheduling.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

public class MainFrame extends JFrame {

    private final InputPanel input = new InputPanel();
    private final GanttPanel gantt = new GanttPanel();
    private final MatrixPanel matrix = new MatrixPanel();
    private final MetricsPanel metrics = new MetricsPanel();
    private final SweepPanel sweep = new SweepPanel(input);
    private final JButton step = new JButton("Step");
    private final JButton play = new JButton("Play");
    private final JButton reset = new JButton("Reset");
    private final Timer timer;

    private SimulationRunner.Result result;
    private int shown;

    public MainFrame() {
        super("Cloud-Cluster CPU Scheduling Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton run = new JButton("Run simulation");
        run.addActionListener(e -> runSimulation());
        step.addActionListener(e -> advance());
        play.addActionListener(e -> togglePlay());
        reset.addActionListener(e -> restart());
        setReplayEnabled(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        actions.add(run);
        actions.add(reset);
        actions.add(step);
        actions.add(play);

        JPanel north = new JPanel(new BorderLayout());
        north.add(input, BorderLayout.CENTER);
        north.add(actions, BorderLayout.SOUTH);

        JSplitPane simulation = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, new JScrollPane(gantt), matrix);
        simulation.setResizeWeight(0.3);
        simulation.setDividerLocation(130);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Timeline", simulation);
        tabs.addTab("Metrics", metrics);
        tabs.addTab("CS vs Q", sweep);

        add(north, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        timer = new Timer(450, e -> advance());

        setSize(1100, 760);
        setLocationRelativeTo(null);
    }

    private void runSimulation() {
        timer.stop();
        play.setText("Play");
        try {
            result = SimulationRunner.run(input.readSpecs(), input.readConfig());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        shown = result.timeline().size();
        gantt.setTimeline(result.timeline(), shown);
        matrix.setTimeline(result.timeline(), result.config().algorithm(), shown);
        metrics.setResult(result);
        setReplayEnabled(true);
    }

    private void restart() {
        timer.stop();
        play.setText("Play");
        shown = 0;
        gantt.setVisible(0);
        matrix.setVisible(0);
    }

    private void advance() {
        if (result == null) {
            return;
        }
        if (shown >= result.timeline().size()) {
            timer.stop();
            play.setText("Play");
            return;
        }
        shown++;
        gantt.setVisible(shown);
        matrix.setVisible(shown);
    }

    private void togglePlay() {
        if (result == null) {
            return;
        }
        if (timer.isRunning()) {
            timer.stop();
            play.setText("Play");
            return;
        }
        if (shown >= result.timeline().size()) {
            restart();
        }
        timer.start();
        play.setText("Pause");
    }

    private void setReplayEnabled(boolean enabled) {
        step.setEnabled(enabled);
        play.setEnabled(enabled);
        reset.setEnabled(enabled);
    }
}
