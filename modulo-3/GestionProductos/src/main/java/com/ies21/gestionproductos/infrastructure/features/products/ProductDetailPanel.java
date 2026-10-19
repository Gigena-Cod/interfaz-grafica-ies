package com.ies21.gestionproductos.infrastructure.features.products;

import com.ies21.gestionproductos.domain.model.Product;
import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.infrastructure.components.*;
import com.ies21.gestionproductos.infrastructure.features.products.dialogs.ProductFormDialog;
import com.ies21.gestionproductos.infrastructure.features.products.dialogs.DeleteProductDialog;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class ProductDetailPanel extends JPanel {
    private final JLabel code = Ui.label("", 13, Ui.MUTED, false);
    private final JPanel values = new JPanel(new GridLayout(0, 2, 24, 24));
    private final ProductService service;
    private Product current;

    public ProductDetailPanel(ProductService service, Runnable back) {
        super(new BorderLayout(0, 24));
        this.service = service;
        setBackground(Ui.BACKGROUND);
        setBorder(new EmptyBorder(28, 30, 28, 30));
        JPanel header = new JPanel(new BorderLayout(0, 18));
        header.setOpaque(false);
        JButton returnButton = Ui.button("← Volver al catálogo", false);
        returnButton.addActionListener(e -> back.run());
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(returnButton);
        header.add(backRow, BorderLayout.NORTH);
        header.add(Ui.label("Detalle del producto", 30, Ui.INK, true), BorderLayout.CENTER);
        header.add(code, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        JPanel card = new JPanel(new BorderLayout(0, 28));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(28, 28, 28, 28));
        card.add(Ui.label("Datos del producto", 18, Ui.INK, true), BorderLayout.NORTH);
        values.setOpaque(false);
        card.add(values, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton delete = Ui.button("Eliminar producto", false);
        delete.setForeground(Ui.RED);
        delete.addActionListener(e -> Ui.runSafely(this, () -> {
            if (current != null) new DeleteProductDialog(SwingUtilities.getWindowAncestor(this), service,
                    service.getProduct(current.id()), back).showDialog();
        }));
        JButton edit = Ui.button("Editar producto", true);
        edit.addActionListener(e -> Ui.runSafely(this, () -> {
            if (current != null) new ProductFormDialog(SwingUtilities.getWindowAncestor(this), service,
                    service.getProduct(current.id()), () -> showProduct(current.id())).showDialog();
        }));
        actions.add(delete);
        actions.add(edit);
        card.add(actions, BorderLayout.SOUTH);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        add(top, BorderLayout.CENTER);
        add(Ui.label("MongoDB · Los cambios se guardan en la base de datos.", 12, Ui.MUTED, false), BorderLayout.SOUTH);
    }

    /**
     * Consulta nuevamente el producto para evitar mostrar una copia desactualizada.
     * @param id identificador estable seleccionado en el catálogo
     * @throws IllegalArgumentException si el producto fue eliminado o el ID es inválido
     */
    public void showProduct(String id) {
        current = service.getProduct(id);
        code.setText("CATÁLOGO  /  " + current.id());
        values.removeAll();
        value("Nombre", current.name());
        value("Categoría", current.category());
        value("Precio (ARS)", NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")).format(current.price()));
        value("Stock", current.stock() + " unidades");
        value("Disponibilidad", current.available() ? "Disponible para la venta" : "No disponible");
        revalidate();
        repaint();
    }

    private void value(String title, String text) {
        JTextField display = new JTextField(text);
        display.setEditable(false);
        display.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        display.setBackground(Color.WHITE);
        display.setForeground(Ui.INK);
        values.add(new FormField(title, display));
    }
}
