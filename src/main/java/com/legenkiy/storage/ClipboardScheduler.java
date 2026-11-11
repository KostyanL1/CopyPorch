package com.legenkiy.storage;

import com.legenkiy.exception.StorageException;
import com.legenkiy.history.HistoryService;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClipboardScheduler {
    private static final System.Logger LOG = System.getLogger(ClipboardScheduler.class.getName());
    private static final int INITIAL_DELAY = 0;
    private static final int DELAY_MS = 500;
    private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
    private final Clipboard clipboard;
    private final HistoryService historyService;

    private String lastText;
    private boolean clipboardInitialized;

    public ClipboardScheduler(Clipboard clipboard, HistoryService historyService) {
        this.clipboard = clipboard;
        this.historyService = historyService;
    }

    public void startClipboardMonitor(DefaultListModel<String> model) {
        readClipboardChange();
        monitor.scheduleWithFixedDelay(() -> {
            String text = readClipboardChange();
            if (text == null || text.isBlank()) {
                return;
            }
            try {
                historyService.saveToStorage(text);
            } catch (IOException | StorageException error) {
                LOG.log(System.Logger.Level.WARNING, "Could not save clipboard history", error);
            }
            SwingUtilities.invokeLater(() -> model.addElement(text));
        }, INITIAL_DELAY, DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public synchronized void copyFromHistory(String text) {
        clipboard.setContents(new StringSelection(text), null);
        lastText = text;
        clipboardInitialized = true;
    }

    public void stopClipboardMonitor() {
        monitor.shutdown();
    }

    private synchronized String readClipboardChange() {
        String text;
        try {
            text = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException ignored) {
            text = null;
        } catch (IOException | IllegalStateException ignored) {
            return null;
        }

        boolean changed = clipboardInitialized && !Objects.equals(lastText, text);
        lastText = text;
        clipboardInitialized = true;
        return changed ? text : null;
    }
}
