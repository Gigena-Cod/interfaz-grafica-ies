package com.ies21.gestionproductos.infrastructure.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Estructura genérica: encabezado, contenido y dos acciones. */
public class BaseModal extends JDialog {
    protected final JPanel body = new JPanel(new BorderLayout(0, 18));
    protected final JButton accept;

    public BaseModal(Window owner, String title, String description, String acceptText, boolean danger) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 24));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(26, 28, 26, 28));
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(Ui.label(title, 24, Ui.INK, true), BorderLayout.NORTH);
        JTextArea subtitle = new JTextArea(description);
        subtitle.setEditable(false);
        subtitle.setFocusable(false);
        subtitle.setLineWrap(true);
        subtitle.setWrapStyleWord(true);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Ui.MUTED);
        subtitle.setRows(2);
        header.add(subtitle, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);
        body.setOpaque(false);
        root.add(body, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton cancel = Ui.button("Cancelar", false);
        cancel.addActionListener(e -> dispose());
        accept = Ui.button(acceptText, true);
        if (danger) accept.setBackground(Ui.RED);
        actions.add(cancel);
        actions.add(accept);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent event) { dispose(); }
        });
        if (!danger) getRootPane().setDefaultButton(accept);
        setResizable(false);
    }

    protected void open() {
        pack();
        setSize(Math.max(600, getWidth()), getHeight());
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    protected void error(RuntimeException ex) {
        Ui.runSafely(this, () -> { throw ex; });
    }
}
