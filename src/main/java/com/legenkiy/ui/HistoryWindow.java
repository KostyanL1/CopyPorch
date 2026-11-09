package com.legenkiy.ui;

import com.legenkiy.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;

public class HistoryWindow extends JFrame {

    public HistoryWindow(JPanel content) {
        super("CopyPorch");

        setContentPane(content);
        setSize(400, 300);
        setLocationRelativeTo(null);
        SystemTray tray = SystemTray.getSystemTray();


        try {
            Image image = ImageIO.read(Main.class.getResource("/copyporch-icon.png"));
            TrayIcon icon = new TrayIcon(image);
            icon.setImageAutoSize(true);
            icon.addActionListener(e ->
                    SwingUtilities.invokeLater(() -> this.setVisible(true)));
            tray.add(icon);
        } catch (IOException | AWTException e) {
            throw new RuntimeException(e);
        }

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

}
