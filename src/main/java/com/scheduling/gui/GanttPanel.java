package com.scheduling.gui;

import com.scheduling.model.TickSnapshot;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

final class GanttPanel extends JPanel {

  private static final int CELL = 36;
  private static final int LEFT = 24;
  private static final int TOP = 20;
  private static final int BAR_HEIGHT = 48;
  private static final Color[] PALETTE = {
      new Color(0x8EC5FC), new Color(0xA8E6A1), new Color(0xFFD18A), new Color(0xF4A6B7),
      new Color(0xC9B5F0), new Color(0x9EE0D6), new Color(0xF6E58D), new Color(0xB8C4D0)
  };

  private List<TickSnapshot> timeline = List.of();
  private int visible;
  private final Map<String, Color> colors = new LinkedHashMap<>();

  void setTimeline(List<TickSnapshot> timeline, int visible) {
    this.timeline = timeline;
    this.visible = visible;
    colors.clear();
    for (TickSnapshot tick : timeline) {
      if (tick.isBusy() && !colors.containsKey(tick.running())) {
        colors.put(tick.running(), PALETTE[colors.size() % PALETTE.length]);
      }
    }
    revalidate();
    repaint();
  }

  void setVisible(int visible) {
    this.visible = visible;
    repaint();
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(LEFT * 2 + CELL * Math.max(timeline.size(), 12), TOP + BAR_HEIGHT + 44);
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    Graphics2D g = (Graphics2D) graphics.create();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setFont(getFont().deriveFont(Font.BOLD, 13f));
    FontMetrics fm = g.getFontMetrics();

    if (timeline.isEmpty()) {
      g.setColor(Color.GRAY);
      g.drawString("Press \"Run simulation\" to draw the Gantt chart", LEFT, TOP + BAR_HEIGHT / 2);
      g.dispose();
      return;
    }

    int index = 0;
    while (index < visible && index < timeline.size()) {
      String label = timeline.get(index).running();
      int end = index;
      while (end + 1 < visible && end + 1 < timeline.size()
          && timeline.get(end + 1).running().equals(label)) {
        end++;
      }
      int x = LEFT + index * CELL;
      int width = (end - index + 1) * CELL;
      TickSnapshot first = timeline.get(index);

      Color fill;
      String text;
      if (first.isContextSwitch()) {
        fill = new Color(0x9AA0A6);
        text = "CS";
      } else if (first.isIdle()) {
        fill = new Color(0xE8EAED);
        text = "idle";
      } else {
        fill = colors.get(label);
        text = label;
      }
      g.setColor(fill);
      g.fillRect(x, TOP, width, BAR_HEIGHT);
      g.setColor(new Color(0x3C4043));
      g.setStroke(new BasicStroke(1.2f));
      g.drawRect(x, TOP, width, BAR_HEIGHT);
      g.drawString(text, x + (width - fm.stringWidth(text)) / 2, TOP + BAR_HEIGHT / 2 + fm.getAscent() / 2 - 2);
      index = end + 1;
    }

    g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
    fm = g.getFontMetrics();
    g.setColor(getForeground());
    int shown = Math.min(visible, timeline.size());
    for (int i = 0; i <= shown; i++) {
      int time = i < timeline.size() ? timeline.get(i).time() : timeline.get(timeline.size() - 1).time() + 1;
      int x = LEFT + i * CELL;
      g.drawLine(x, TOP + BAR_HEIGHT, x, TOP + BAR_HEIGHT + 6);
      String text = Integer.toString(time);
      g.drawString(text, x - fm.stringWidth(text) / 2, TOP + BAR_HEIGHT + 22);
    }
    g.dispose();
  }
}
