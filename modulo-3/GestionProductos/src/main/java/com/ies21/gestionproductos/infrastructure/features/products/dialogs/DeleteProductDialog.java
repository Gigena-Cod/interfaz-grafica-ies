package com.ies21.gestionproductos.infrastructure.features.products.dialogs;

import com.ies21.gestionproductos.domain.model.Product;
import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.infrastructure.components.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public final class DeleteProductDialog extends BaseModal {
    public DeleteProductDialog(Window owner, ProductService service, Product product, Runnable onDeleted) {
        super(owner, "Eliminar producto", "Esta acción no se puede deshacer. Revisá el producto antes de confirmar.", "Eliminar producto", true);
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        JTextArea warning = new JTextArea("Eliminar producto: " + product.name() + "\nCódigo: " + product.id()
                + "\n\nSi existieran compras o movimientos asociados, podrían verse afectados. Esta demo no gestiona compras.");
        warning.setEditable(false);
        warning.setFocusable(false);
        warning.setLineWrap(true);
        warning.setWrapStyleWord(true);
        warning.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        warning.setForeground(Ui.INK);
        warning.setBackground(new Color(255, 243, 246));
        warning.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        warning.setRows(7);
        content.add(warning, BorderLayout.CENTER);
        JTextField confirmation = new JTextField();
        content.add(new FormField("Escribí ELIMINAR para confirmar", confirmation), BorderLayout.SOUTH);
        body.add(content);
        accept.setEnabled(false);
        accept.setBackground(new Color(255, 223, 230));
        confirmation.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                accept.setEnabled("ELIMINAR".equals(confirmation.getText()));
                accept.setBackground(accept.isEnabled() ? Ui.RED : new Color(255, 223, 230));
            }
            public void insertUpdate(DocumentEvent e) { changed(); }
            public void removeUpdate(DocumentEvent e) { changed(); }
            public void changedUpdate(DocumentEvent e) { changed(); }
        });
        accept.addActionListener(e -> {
            if (!"ELIMINAR".equals(confirmation.getText())) return;
            try { service.deleteProduct(product.id()); }
            catch (RuntimeException ex) { error(ex); return; }
            JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.", "Producto eliminado", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            onDeleted.run();
        });
    }
    public void showDialog() { open(); }
}
