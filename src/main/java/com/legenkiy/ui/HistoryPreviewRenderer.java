package com.legenkiy.ui;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JViewport;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.TextLayout;

public class HistoryPreviewRenderer extends DefaultListCellRenderer {
    private static final int PADDING = 4;
    private final Color hoverBackground;
    private final HistoryHoverListener hoverListener;

    private String value = "";

    public HistoryPreviewRenderer(JList<?> list, JViewport viewport, Color hoverBackground) {
        this.hoverBackground = hoverBackground;
        this.hoverListener = new HistoryHoverListener(list, viewport);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean selected, boolean hasFocus) {
        this.value = (String) value;
        super.getListCellRendererComponent(list, "", index, selected, hasFocus);
        if (!selected && hoverListener.isHovered(index)) {
            setBackground(hoverBackground);
        }
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        int lineHeight = getFontMetrics(getFont()).getHeight();
        int height = HistoryPreviewLayout.MAX_LINES * lineHeight + insets.top + insets.bottom + 2 * PADDING;
        return new Dimension(1, height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Insets insets = getInsets();
        int x = insets.left + PADDING;
        int width = getWidth() - x - insets.right - PADDING;
        if (width <= 0) {
            return;
        }

        Graphics2D canvas = (Graphics2D) graphics;
        canvas.setColor(getForeground());
        FontMetrics metrics = canvas.getFontMetrics(getFont());
        int baseline = insets.top + PADDING + metrics.getAscent();
        var preview = new HistoryPreviewLayout(getFont(), canvas.getFontRenderContext(), width);
        for (TextLayout line : preview.wrap(value)) {
            line.draw(canvas, x, baseline);
            baseline += metrics.getHeight();
        }
    }
}
