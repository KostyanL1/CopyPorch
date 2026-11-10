package com.legenkiy;

import com.legenkiy.history.HistoryService;
import com.legenkiy.storage.ClipboardScheduler;
import com.legenkiy.storage.StorageService;
import com.legenkiy.ui.HistoryWindow;
import com.legenkiy.ui.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

public class Main {
    static void main() throws IOException {
        Properties properties = new Properties();
        String appData = System.getenv().getOrDefault("LOCALAPPDATA", System.getProperty("user.home"));
        properties.setProperty("PATH", Path.of(appData, "CopyPorch", "history").toString());
        try (InputStream config = Main.class.getResourceAsStream("/history-path.cfg")) {
            if (config == null) {
                throw new IOException("Missing history-path.cfg");
            }
            Reader reader = new InputStreamReader(config, StandardCharsets.UTF_8);
            properties.load(reader);
        }

        StorageService storageService = new StorageService(properties);
        HistoryService historyService = new HistoryService(storageService);
        SwingUtilities.invokeLater(() -> openWindow(historyService));
    }

    private static void openWindow(HistoryService historyService) {
        JList<String> list = historyService.getListFromHistory();
        JPanel content = new JPanel(new BorderLayout());
        content.add(new JScrollPane(list), BorderLayout.CENTER);

        HistoryWindow window = new HistoryWindow(content);
        list.addMouseListener(new MouseListener(list, window, historyService));

        ClipboardScheduler scheduler = new ClipboardScheduler();
        scheduler.startClipboardMonitor((DefaultListModel<String>) list.getModel());
        window.setVisible(true);
    }
}
