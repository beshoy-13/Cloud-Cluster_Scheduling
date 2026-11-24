package com.scheduling.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

final class InputPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"ID", "Arrival", "Burst", "Priority"}, 0);
    private final JTable table = new JTable(model);
    private final JComboBox<String> algorithm = new JComboBox<>(
            new String[] {SimulationRunner.ACS, SimulationRunner.SJF});
    private final JSpinner quantum = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JSpinner contextSwitch = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner aging = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));

    InputPanel() {
        super(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));

        table.setRowHeight(24);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new java.awt.Dimension(380, 130));

        JButton add = new JButton("Add process");
        add.addActionListener(e -> model.addRow(new Object[] {"P" + (model.getRowCount() + 1), 0, 1, 1}));
        JButton remove = new JButton("Remove selected");
        remove.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            int row = table.getSelectedRow();
            if (row >= 0) {
                model.removeRow(row);
            }
        });
        JButton tc1 = new JButton("Load Test Case 1");
        tc1.addActionListener(e -> loadTestCase1());
        JButton tc2 = new JButton("Load Test Case 2");
        tc2.addActionListener(e -> loadTestCase2());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        buttons.add(add);
        buttons.add(remove);
        buttons.add(tc1);
        buttons.add(tc2);

        JPanel left = new JPanel(new BorderLayout());
        left.add(tableScroll, BorderLayout.CENTER);
        left.add(buttons, BorderLayout.SOUTH);

        JPanel config = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.WEST;
        addRow(config, c, 0, "Algorithm", algorithm);
        addRow(config, c, 1, "Quantum (Q)", quantum);
        addRow(config, c, 2, "Context switch (CS)", contextSwitch);
        addRow(config, c, 3, "Aging interval", aging);

        algorithm.addActionListener(e -> {
            boolean acs = SimulationRunner.ACS.equals(algorithm.getSelectedItem());
            quantum.setEnabled(acs);
            aging.setEnabled(acs);
        });

        add(left, BorderLayout.CENTER);
        add(config, BorderLayout.EAST);

        loadTestCase1();
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, java.awt.Component field) {
        c.gridy = row;
        c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(field, c);
    }

    private void setRows(Object[][] rows) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    void loadTestCase1() {
        setRows(new Object[][] {{"P1", 0, 4, 2}, {"P2", 1, 7, 3}, {"P3", 3, 2, 1}});
        quantum.setValue(3);
        contextSwitch.setValue(0);
        aging.setValue(4);
    }

    void loadTestCase2() {
        setRows(new Object[][] {{"P1", 0, 5, 4}, {"P2", 1, 6, 2}, {"P3", 4, 4, 3}});
        quantum.setValue(3);
        contextSwitch.setValue(1);
        aging.setValue(4);
    }

    List<SimulationRunner.ProcessSpec> readSpecs() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        if (model.getRowCount() == 0) {
            throw new IllegalArgumentException("Add at least one process.");
        }
        List<SimulationRunner.ProcessSpec> specs = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            String id = String.valueOf(model.getValueAt(row, 0)).trim();
            if (id.isEmpty() || !ids.add(id)) {
                throw new IllegalArgumentException("Row " + (row + 1) + ": process ID must be unique and not empty.");
            }
            int arrival = parse(row, 1, "Arrival");
            int burst = parse(row, 2, "Burst");
            int priority = parse(row, 3, "Priority");
            if (arrival < 0) {
                throw new IllegalArgumentException("Row " + (row + 1) + ": arrival cannot be negative.");
            }
            if (burst <= 0) {
                throw new IllegalArgumentException("Row " + (row + 1) + ": burst must be greater than zero.");
            }
            specs.add(new SimulationRunner.ProcessSpec(id, arrival, burst, priority));
        }
        return specs;
    }

    SimulationRunner.Config readConfig() {
        return new SimulationRunner.Config(
                (String) algorithm.getSelectedItem(),
                intValue(quantum),
                intValue(contextSwitch),
                intValue(aging));
    }

    private int parse(int row, int column, String name) {
        String text = String.valueOf(model.getValueAt(row, column)).trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Row " + (row + 1) + ": " + name + " must be a whole number.");
        }
    }

    private int intValue(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException ignored) {
        }
        return ((Number) spinner.getValue()).intValue();
    }
}
