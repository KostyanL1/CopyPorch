package com.legenkiy.ui;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.SwingUtilities;

final class HistoryHotkey {
    private static final int HOTKEY_ID = 1;
    private static final int MODIFIERS = WinUser.MOD_CONTROL | WinUser.MOD_ALT | WinUser.MOD_NOREPEAT;

    private HistoryHotkey() {
    }

    static void register(Runnable toggleHistory) {
        if (!Platform.isWindows()) {
            return;
        }

        Thread listener = new Thread(() -> listen(toggleHistory), "copyporch-hotkey");
        listener.setDaemon(true);
        listener.start();
    }

    private static void listen(Runnable toggleHistory) {
        User32 windows = User32.INSTANCE;
        if (!windows.RegisterHotKey(null, HOTKEY_ID, MODIFIERS, 'V')) {
            System.getLogger(HistoryHotkey.class.getName())
                    .log(System.Logger.Level.WARNING, "Could not register Ctrl+Alt+V");
            return;
        }

        try {
            WinUser.MSG message = new WinUser.MSG();
            while (windows.GetMessage(message, null, 0, 0) > 0) {
                if (message.message == WinUser.WM_HOTKEY && message.wParam.intValue() == HOTKEY_ID) {
                    SwingUtilities.invokeLater(toggleHistory);
                }
            }
        } finally {
            windows.UnregisterHotKey(null, HOTKEY_ID);
        }
    }
}
