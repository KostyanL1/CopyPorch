package com.legenkiy.ui;

import javax.swing.JList;
import javax.swing.JViewport;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class HistoryHoverListener extends MouseAdapter {
    private final JList<?> list;
    private int hoveredIndex = -1;

    HistoryHoverListener(JList<?> list, JViewport viewport) {
        this.list = list;
        list.addMouseListener(this);
        list.addMouseMotionListener(this);
        viewport.addChangeListener(event -> {
            if (list.isShowing()) {
                updateHover(list.getMousePosition());
            }
        });
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        updateHover(event.getPoint());
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        updateHover(event.getPoint());
    }

    @Override
    public void mouseExited(MouseEvent event) {
        updateHover(null);
    }

    boolean isHovered(int index) {
        return index >= 0 && index == hoveredIndex;
    }

    private void updateHover(Point point) {
        int index = point == null ? -1 : list.locationToIndex(point);
        Rectangle bounds = index < 0 ? null : list.getCellBounds(index, index);
        if (bounds == null || !bounds.contains(point)) {
            index = -1;
        }
        if (hoveredIndex != index) {
            hoveredIndex = index;
            list.repaint();
        }
    }
}
