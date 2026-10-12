package com.ies21.gestionproductos.infrastructure.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public final class StatCard extends JPanel {
    public StatCard(String title, String value, String detail, Color accent) {
        this(title, value, detail, accent, false);
    }

    public StatCard(String title, String value, String detail, Color accent, boolean compact) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, compact ? 6 : 14));
        int padding = compact ? 12 : 22;
        setBorder(new CompoundBorder(new LineBorder(new Color(231, 236, 247)), new EmptyBorder(padding, padding, padding, padding)));
        add(Ui.label(title, 12, Ui.MUTED, true), BorderLayout.NORTH);
        add(Ui.label(value, compact ? 24 : 32, accent, true), BorderLayout.CENTER);
        add(Ui.label(detail, 12, Ui.MUTED, false), BorderLayout.SOUTH);
    }
}
