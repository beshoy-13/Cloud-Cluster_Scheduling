package com.scheduling.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

final class LineChartPanel extends JPanel {

    record Series(String name, double[] x, double[] y) {}

    private static final Color[] COLORS = {
        new Color(0x1A73E8), new Color(0xD93025), new Color(0x188038), new Color(0xE37400),
        new Color(0x9334E6), new Color(0x007B83)
    };

    private final String title;
    private final String xLabel;
    private final String yLabel;
    private List<Series> series = new ArrayList<>();

    LineChartPanel(String title, String xLabel, String yLabel) {
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(460, 340));
    }

    void setSeries(List<Series> series) {
        this.series = series;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = g.getFontMetrics();

        int left = 62;
        int right = 20;
        int top = 54;
        int bottom = 50;
        int w = getWidth() - left - right;
        int h = getHeight() - top - bottom;

        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        g.drawString(title, left, 22);
        g.setFont(g.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
        fm = g.getFontMetrics();

        if (series.isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("Run the sweep to see the chart", left + 20, top + h / 2);
            g.dispose();
            return;
        }

        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        double yMax = 0;
        for (Series s : series) {
            for (int i = 0; i < s.x().length; i++) {
                xMin = Math.min(xMin, s.x()[i]);
                xMax = Math.max(xMax, s.x()[i]);
                yMax = Math.max(yMax, s.y()[i]);
            }
        }
        if (xMax == xMin) {
            xMax = xMin + 1;
        }
        double yTop = niceCeiling(yMax);

        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 5; i++) {
            int y = top + h - h * i / 5;
            g.setColor(new Color(0xE0E0E0));
            g.drawLine(left, y, left + w, y);
            g.setColor(Color.BLACK);
            String label = String.format("%.1f", yTop * i / 5);
            g.drawString(label, left - 8 - fm.stringWidth(label), y + 4);
        }
        g.setColor(Color.BLACK);
        g.drawLine(left, top, left, top + h);
        g.drawLine(left, top + h, left + w, top + h);

        for (double xv = Math.ceil(xMin); xv <= xMax; xv += Math.max(1, Math.ceil((xMax - xMin) / 10))) {
            int px = left + (int) Math.round((xv - xMin) / (xMax - xMin) * w);
            g.drawLine(px, top + h, px, top + h + 5);
            String label = Integer.toString((int) xv);
            g.drawString(label, px - fm.stringWidth(label) / 2, top + h + 20);
        }
        g.drawString(xLabel, left + w / 2 - fm.stringWidth(xLabel) / 2, getHeight() - 8);

        AffineTransform old = g.getTransform();
        g.rotate(-Math.PI / 2, 16, top + h / 2.0 + fm.stringWidth(yLabel) / 2.0);
        g.drawString(yLabel, 16, (int) (top + h / 2.0 + fm.stringWidth(yLabel) / 2.0));
        g.setTransform(old);

        int legendX = left + 6;
        for (int s = 0; s < series.size(); s++) {
            Series line = series.get(s);
            Color color = COLORS[s % COLORS.length];
            g.setColor(color);
            g.setStroke(new BasicStroke(2f));
            int prevX = 0;
            int prevY = 0;
            for (int i = 0; i < line.x().length; i++) {
                int px = left + (int) Math.round((line.x()[i] - xMin) / (xMax - xMin) * w);
                int py = top + h - (int) Math.round(line.y()[i] / yTop * h);
                if (i > 0) {
                    g.drawLine(prevX, prevY, px, py);
                }
                g.fillOval(px - 3, py - 3, 7, 7);
                prevX = px;
                prevY = py;
            }
            g.fillRect(legendX, 34, 14, 4);
            g.setColor(Color.BLACK);
            g.drawString(line.name(), legendX + 18, 40);
            legendX += 26 + fm.stringWidth(line.name()) + 10;
        }
        g.dispose();
    }

    private double niceCeiling(double value) {
        if (value <= 0) {
            return 1;
        }
        double magnitude = Math.pow(10, Math.floor(Math.log10(value)));
        double[] steps = {1, 2, 2.5, 5, 10};
        for (double step : steps) {
            if (value <= step * magnitude) {
                return step * magnitude;
            }
        }
        return 10 * magnitude;
    }
}
