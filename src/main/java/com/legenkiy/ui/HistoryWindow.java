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
