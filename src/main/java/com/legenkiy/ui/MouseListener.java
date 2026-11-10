package com.legenkiy.ui;

import com.legenkiy.history.HistoryService;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class MouseListener extends MouseAdapter {

    private JList<String> list;
    private JFrame frame;
    private final HistoryService historyService;

    public MouseListener(JList<String> list, JFrame frame, HistoryService historyService) {
        this.list = list;
        this.frame = frame;
        this.historyService = historyService;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            if (!SwingUtilities.isLeftMouseButton(e)) return;

            int index = list.locationToIndex(e.getPoint());

            if (index < 0 || !list.getCellBounds(index, index).contains(e.getPoint())) return;

            String value = list.getModel().getElementAt(index);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null);
            historyService.saveToStorage(value);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        frame.dispose();
    }

}
