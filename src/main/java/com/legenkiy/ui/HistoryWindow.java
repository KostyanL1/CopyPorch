package com.legenkiy.ui;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;

public class HistoryWindow extends JFrame {

    private static final String ICON_PATH = "/copyporch-icon.png";
    private static final Color HISTORY_BACKGROUND = new Color(0x292929);
    private static final Color HISTORY_HOVER = new Color(0x3F3F3F);
    private static final Color TITLE_BACKGROUND = new Color(0x383838);
    private static final Color TITLE_HOVER = new Color(0x525252);

    private boolean exiting;

    public HistoryWindow(JList<String> list, Runnable stopClipboardMonitor) {
        super("CopyPorch");
        setUndecorated(true);
        Image image = loadIcon();
        setIconImage(image);

        JPanel window = new JPanel(new BorderLayout());
        window.setBackground(HISTORY_BACKGROUND);
        window.setBorder(BorderFactory.createLineBorder(new Color(0x4D4D4D)));
        window.add(createTitleBar(image), BorderLayout.NORTH);
        window.add(createHistoryPane(list), BorderLayout.CENTER);
        setContentPane(window);
        setSize(400, 360);
        setResizable(false);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setupTray(image, stopClipboardMonitor);
        HistoryHotkey.register(this::toggleHistory);
    }

    private JScrollPane createHistoryPane(JList<String> list) {
        list.setBackground(HISTORY_BACKGROUND);
        list.setForeground(new Color(0xF0F0F0));
        list.setSelectionBackground(new Color(0x606060));
        list.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(list);
        list.setCellRenderer(new HistoryPreviewRenderer(list, scrollPane.getViewport(), HISTORY_HOVER));
        scrollPane.getViewport().setBackground(HISTORY_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                trackColor = HISTORY_BACKGROUND;
                thumbColor = new Color(0x686868);
                thumbDarkShadowColor = thumbColor;
                thumbLightShadowColor = thumbColor;
                thumbHighlightColor = thumbColor;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createDarkArrow(orientation);
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createDarkArrow(orientation);
            }

            private JButton createDarkArrow(int orientation) {
                return new BasicArrowButton(orientation, trackColor, trackColor, thumbColor, trackColor);
            }
        });
        return scrollPane;
    }

    private JPanel createTitleBar(Image image) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TITLE_BACKGROUND);
        bar.setPreferredSize(new Dimension(0, 32));

        JLabel title = new JLabel(getTitle(), new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH)),
                JLabel.LEADING);
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        bar.add(title, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1, 2));
        controls.setOpaque(false);
        controls.add(createTitleButton("−", "Minimize", () -> setExtendedState(JFrame.ICONIFIED)));
        controls.add(createTitleButton("×", "Close", () ->
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))));
        bar.add(controls, BorderLayout.EAST);

        MouseAdapter drag = new MouseAdapter() {
            private Point offset;

            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    offset = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), HistoryWindow.this);
                }
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (offset != null) {
                    setLocation(event.getXOnScreen() - offset.x, event.getYOnScreen() - offset.y);
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    offset = null;
                }
            }
        };
        title.addMouseListener(drag);
        title.addMouseMotionListener(drag);
        return bar;
    }

    private JButton createTitleButton(String symbol, String label, Runnable action) {
        JButton button = new JButton(symbol);
        button.setToolTipText(label);
        button.getAccessibleContext().setAccessibleName(label);
        button.setPreferredSize(new Dimension(42, 32));
        button.setForeground(Color.WHITE);
        button.setBackground(TITLE_BACKGROUND);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.getModel().addChangeListener(event ->
                button.setBackground(button.getModel().isRollover() ? TITLE_HOVER : TITLE_BACKGROUND));
        button.addActionListener(event -> action.run());
        return button;
    }

    private Image loadIcon() {
        try {
            return ImageIO.read(Objects.requireNonNull(HistoryWindow.class.getResource(ICON_PATH)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupTray(Image image, Runnable stopClipboardMonitor) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon icon = new TrayIcon(image);
            icon.setImageAutoSize(true);
            icon.addActionListener(e -> SwingUtilities.invokeLater(this::showHistory));

            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(e -> SwingUtilities.invokeLater(() -> {
                exiting = true;
                stopClipboardMonitor.run();
                tray.remove(icon);
                dispose();
            }));
            PopupMenu menu = new PopupMenu();
            menu.add(exit);
            icon.setPopupMenu(menu);
            tray.add(icon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    private void toggleHistory() {
        if (isVisible() && (getExtendedState() & JFrame.ICONIFIED) == 0) {
            setVisible(false);
        } else {
            showHistory();
        }
    }

    private void showHistory() {
        if (exiting) {
            return;
        }
        setExtendedState(JFrame.NORMAL);
        setVisible(true);
        toFront();
    }

}
