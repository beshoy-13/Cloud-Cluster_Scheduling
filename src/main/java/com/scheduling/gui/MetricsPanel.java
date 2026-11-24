package com.scheduling.gui;

import com.scheduling.model.Process;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

final class MetricsPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Process", "Arrival", "Burst", "Finish", "Turnaround", "Waiting", "Response"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JLabel averageTurnaround = new JLabel();
    private final JLabel averageWaiting = new JLabel();
    private final JLabel averageResponse = new JLabel();
    private final JLabel utilization = new JLabel();

    MetricsPanel() {
        super(new BorderLayout());
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel summary = new JPanel(new GridLayout(2, 2, 12, 4));
        summary.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        summary.add(averageTurnaround);
        summary.add(averageWaiting);
        summary.add(averageResponse);
        summary.add(utilization);
        add(summary, BorderLayout.SOUTH);
    }

    void setResult(SimulationRunner.Result result) {
        model.setRowCount(0);
        for (Process p : result.processes()) {
            model.addRow(new Object[] {
                p.getId(),
                p.getArrivalTime(),
                p.getBurstTime(),
                p.getFinishTime(),
                p.getTurnaroundTime(),
                p.getWaitingTime(),
                p.getResponseTime()
            });
        }
        averageTurnaround.setText(String.format("Average turnaround time: %.2f", result.metrics().getAverageTurnaroundTime()));
        averageWaiting.setText(String.format("Average waiting time: %.2f", result.metrics().getAverageWaitingTime()));
        averageResponse.setText(String.format("Average response time: %.2f", result.metrics().getAverageResponseTime()));
        utilization.setText(String.format("CPU utilization (useful ticks / total ticks): %.1f%%", result.cpuUtilization()));
    }
}
