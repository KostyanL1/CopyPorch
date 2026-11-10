package com.legenkiy.ui;

import com.legenkiy.Main;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.util.Objects;

public class HistoryWindow extends JFrame {

    private static final String ICON_PATH = "/copyporch-icon.png";

    public HistoryWindow(JPanel content) {
        super("CopyPorch");
        setContentPane(content);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setupTray();
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void setupTray( ) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = ImageIO.read(Objects.requireNonNull(Main.class.getResource(ICON_PATH)));
            TrayIcon icon = new TrayIcon(image);
            icon.setImageAutoSize(true);
            icon.addActionListener(e -> SwingUtilities.invokeLater(() -> this.setVisible(true)));
            tray.add(icon);
        } catch (IOException | AWTException e) {
            throw new RuntimeException(e);
        }
    }

}
