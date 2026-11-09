package com.legenkiy.storage;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClipboardScheduler {
    private static final int INITIAL_DELAY = 0;
    private static final int DELAY_MS = 500;
    private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();

    private String lastText;

    public void startClipboardMonitor(DefaultListModel<String> model) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        monitor.scheduleWithFixedDelay(() -> {
            try {
                if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    lastText = null;
                    return;
                }

                String text = (String) clipboard.getData(DataFlavor.stringFlavor);

                if (text != null && !text.isBlank() && !text.equals(lastText)) {
                    lastText = text;
                    SwingUtilities.invokeLater(() -> model.addElement(text));
                }
            } catch (Exception _) {}
        }, INITIAL_DELAY, DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public void stopClipboardMonitor() {
        monitor.shutdownNow();
    }
}
