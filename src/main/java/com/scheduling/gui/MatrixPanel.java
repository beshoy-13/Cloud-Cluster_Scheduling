package com.scheduling.gui;

import com.scheduling.model.TickSnapshot;
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

final class MatrixPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Time", "Running", "Remaining", "Tier 1", "Tier 2"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<TickSnapshot> timeline = List.of();

    MatrixPanel() {
        super(new BorderLayout());
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(380);
        table.getColumnModel().getColumn(4).setPreferredWidth(380);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                if (!selected) {
                    String running = String.valueOf(t.getValueAt(row, 1));
                    if (TickSnapshot.CONTEXT_SWITCH.equals(running)) {
                        c.setBackground(new Color(0xE6E8EB));
                    } else if (TickSnapshot.IDLE.equals(running)) {
                        c.setBackground(new Color(0xF5F5F5));
                    } else {
                        c.setBackground(t.getBackground());
                    }
                }
                return c;
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    void setTimeline(List<TickSnapshot> timeline, String algorithm, int visible) {
        this.timeline = timeline;
        boolean acs = SimulationRunner.ACS.equals(algorithm);
        table.getColumnModel().getColumn(3).setHeaderValue(acs ? "Tier 1 (Round Robin)" : "Ready queue");
        table.getColumnModel().getColumn(4).setHeaderValue(acs ? "Tier 2 (Priority)" : "-");
        table.getTableHeader().repaint();
        setVisible(visible);
    }

    void setVisible(int visible) {
        model.setRowCount(0);
        int shown = Math.min(visible, timeline.size());
        for (int i = 0; i < shown; i++) {
            TickSnapshot tick = timeline.get(i);
            model.addRow(new Object[] {
                tick.time(),
                tick.running(),
                tick.remaining() < 0 ? "-" : tick.remaining(),
                format(tick.tier1()),
                format(tick.tier2())
            });
        }
        if (shown > 0) {
            table.scrollRectToVisible(table.getCellRect(shown - 1, 0, true));
        }
    }

    private String format(List<String> items) {
        return items.isEmpty() ? "[Empty]" : "[" + String.join(", ", items) + "]";
    }
}
