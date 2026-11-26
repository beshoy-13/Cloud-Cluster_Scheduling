package com.scheduling.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

final class SweepPanel extends JPanel {

    private final InputPanel input;
    private final JSpinner qFrom = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JSpinner qTo = new JSpinner(new SpinnerNumberModel(8, 1, 50, 1));
    private final JSpinner csFrom = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
    private final JSpinner csTo = new JSpinner(new SpinnerNumberModel(3, 0, 20, 1));
    private final LineChartPanel waitingChart =
            new LineChartPanel("Average waiting time vs Q", "Quantum (Q)", "Average waiting time");
    private final LineChartPanel turnaroundChart =
            new LineChartPanel("Average turnaround time vs Q", "Quantum (Q)", "Average turnaround time");
    private final JPanel charts = new JPanel(new GridLayout(1, 2, 8, 0));

    SweepPanel(InputPanel input) {
        super(new BorderLayout());
        this.input = input;

        JButton run = new JButton("Run sweep (ACS)");
        run.addActionListener(e -> runSweep());
        JButton save = new JButton("Save charts as PNG");
        save.addActionListener(e -> savePng());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        controls.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
        controls.add(new JLabel("Q from"));
        controls.add(qFrom);
        controls.add(new JLabel("to"));
        controls.add(qTo);
        controls.add(new JLabel("   CS from"));
        controls.add(csFrom);
        controls.add(new JLabel("to"));
        controls.add(csTo);
        controls.add(run);
        controls.add(save);

        charts.add(waitingChart);
        charts.add(turnaroundChart);

        add(controls, BorderLayout.NORTH);
        add(charts, BorderLayout.CENTER);
    }

    private void runSweep() {
        try {
            int q1 = ((Number) qFrom.getValue()).intValue();
            int q2 = ((Number) qTo.getValue()).intValue();
            int c1 = ((Number) csFrom.getValue()).intValue();
            int c2 = ((Number) csTo.getValue()).intValue();
            if (q1 > q2 || c1 > c2) {
                throw new IllegalArgumentException("Each 'from' value must be less than or equal to its 'to' value.");
            }
            List<SimulationRunner.ProcessSpec> specs = input.readSpecs();
            SimulationRunner.Config base = input.readConfig();

            List<LineChartPanel.Series> waiting = new ArrayList<>();
            List<LineChartPanel.Series> turnaround = new ArrayList<>();
            int points = q2 - q1 + 1;
            for (int cs = c1; cs <= c2; cs++) {
                double[] x = new double[points];
                double[] wait = new double[points];
                double[] tat = new double[points];
                for (int q = q1; q <= q2; q++) {
                    SimulationRunner.Result result = SimulationRunner.run(
                            specs,
                            new SimulationRunner.Config(
                                    SimulationRunner.ACS, q, cs, base.agingInterval()));
                    x[q - q1] = q;
                    wait[q - q1] = result.metrics().getAverageWaitingTime();
                    tat[q - q1] = result.metrics().getAverageTurnaroundTime();
                }
                waiting.add(new LineChartPanel.Series("CS=" + cs, x, wait));
                turnaround.add(new LineChartPanel.Series("CS=" + cs, x, tat));
            }
            waitingChart.setSeries(waiting);
            turnaroundChart.setSeries(turnaround);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void savePng() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("cs-q-sweep.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        BufferedImage image = new BufferedImage(
                Math.max(1, charts.getWidth()), Math.max(1, charts.getHeight()), BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        charts.printAll(g);
        g.dispose();
        try {
            ImageIO.write(image, "png", chooser.getSelectedFile());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not save", JOptionPane.ERROR_MESSAGE);
        }
    }
}
