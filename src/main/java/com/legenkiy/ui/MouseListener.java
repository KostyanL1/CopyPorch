package com.legenkiy.ui;

import com.legenkiy.storage.ClipboardScheduler;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseListener extends MouseAdapter {

    private final JList<String> list;
    private final JFrame frame;
    private final ClipboardScheduler clipboardScheduler;

    public MouseListener(JList<String> list, JFrame frame, ClipboardScheduler clipboardScheduler) {
        this.list = list;
        this.frame = frame;
        this.clipboardScheduler = clipboardScheduler;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;

        int index = list.locationToIndex(e.getPoint());
        if (index < 0 || !list.getCellBounds(index, index).contains(e.getPoint())) return;

        String value = list.getModel().getElementAt(index);
        clipboardScheduler.copyFromHistory(value);
        frame.dispose();
    }

}
