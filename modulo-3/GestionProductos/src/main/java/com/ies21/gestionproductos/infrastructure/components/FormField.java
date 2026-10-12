package com.ies21.gestionproductos.infrastructure.components;

import java.awt.*;
import javax.swing.*;

/** Etiqueta y control reutilizables para formularios. */
public final class FormField extends JPanel {
    public FormField(String title, JComponent input) {
        super(new BorderLayout(0, 9));
        setOpaque(false);
        JLabel label = Ui.label(title, 13, Ui.INK, true);
        label.setLabelFor(input);
        input.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        input.setPreferredSize(new Dimension(250, 42));
        add(label, BorderLayout.NORTH);
        add(input, BorderLayout.CENTER);
    }
}
