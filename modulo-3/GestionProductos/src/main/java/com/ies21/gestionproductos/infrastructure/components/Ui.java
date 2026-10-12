package com.ies21.gestionproductos.infrastructure.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class Ui {
    public static final Color PRIMARY = new Color(85, 70, 235);
    public static final Color INK = new Color(20, 29, 48);
    public static final Color MUTED = new Color(125, 143, 169);
    public static final Color BACKGROUND = new Color(245, 247, 253);
    public static final Color GREEN = new Color(0, 153, 111);
    public static final Color RED = new Color(255, 56, 92);

    private Ui() { }

    public static void runSafely(Component owner, Runnable action) {
        try { action.run(); }
        catch (RuntimeException ex) {
            java.util.logging.Logger.getLogger(Ui.class.getName()).log(java.util.logging.Level.WARNING, "Operación de interfaz fallida", ex);
            JOptionPane.showMessageDialog(owner,
                    ex.getMessage() == null ? "No se pudo completar la operación." : ex.getMessage(),
                    "No se pudo completar la operación", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static JLabel label(String text, int size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    public static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(primary ? Color.WHITE : INK);
        button.setBackground(primary ? PRIMARY : BACKGROUND);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
