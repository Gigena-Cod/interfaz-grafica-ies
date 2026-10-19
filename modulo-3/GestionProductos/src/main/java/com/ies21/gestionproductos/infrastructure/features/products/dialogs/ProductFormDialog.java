package com.ies21.gestionproductos.infrastructure.features.products.dialogs;

import com.ies21.gestionproductos.domain.model.Product;
import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.infrastructure.components.*;
import com.ies21.gestionproductos.domain.model.ProductCategories;
import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;

/** Alta y edición reutilizan campos, validaciones y presentación. */
public final class ProductFormDialog extends BaseModal {
    private final JTextField name = new JTextField();
    private final JComboBox<String> category = new JComboBox<>();
    private final JTextField price = new JTextField();
    private final JTextField stock = new JTextField("0");
    private final JCheckBox available = new JCheckBox("Disponible para la venta", true);

    /**
     * Construye el formulario compartido por alta y edición. La vista interpreta los
     * números ingresados, el servicio valida el negocio y el callback recarga la vista.
     * @param owner ventana principal sobre la que se centra el modal
     * @param service servicio compartido de productos
     * @param product producto a editar, o {@code null} para registrar uno nuevo
     * @param onSaved acción posterior a una escritura exitosa; no se ejecuta al cancelar
     */
    public ProductFormDialog(Window owner, ProductService service, Product product, Runnable onSaved) {
        super(owner, product == null ? "Agregar producto" : "Editar producto",
                product == null ? "Completá los datos básicos del nuevo producto." : "Actualizá los datos del producto. El código se mantiene.",
                product == null ? "Guardar producto" : "Guardar cambios", false);
        category.addItem("Seleccionar categoría");
        ProductCategories.STANDARD.forEach(category::addItem);
        JPanel fields = new JPanel(new GridLayout(0, 2, 20, 20));
        fields.setOpaque(false);
        fields.add(new FormField("Nombre *", name));
        fields.add(new FormField("Categoría *", category));
        fields.add(new FormField("Precio (ARS) *", price));
        fields.add(new FormField("Stock (unidades) *", stock));
        available.setOpaque(false);
        available.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        body.add(fields, BorderLayout.CENTER);
        body.add(available, BorderLayout.SOUTH);
        if (product != null) {
            name.setText(product.name());
            if (!ProductCategories.STANDARD.contains(product.category()))
                category.addItem(product.category());
            category.setSelectedItem(product.category());
            price.setText(product.price().toPlainString());
            stock.setText(Integer.toString(product.stock()));
            available.setSelected(product.available());
            body.add(Ui.label("Código: " + product.id(), 12, Ui.MUTED, false), BorderLayout.NORTH);
        }
        accept.addActionListener(e -> {
            try {
                if (category.getSelectedIndex() == 0) throw new IllegalArgumentException("Seleccioná una categoría.");
                BigDecimal amount;
                int units;
                try {
                    amount = new BigDecimal(price.getText().strip().replace(',', '.'));
                    units = Integer.parseInt(stock.getText().strip());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Ingresá un precio numérico (ej. 480,00) y un stock entero.");
                }
                String selected = (String) category.getSelectedItem();
                if (product == null) service.createProduct(name.getText(), selected, amount, units, available.isSelected());
                else service.updateProduct(new Product(product.id(), name.getText().strip(), selected, amount, units, available.isSelected()));
            } catch (RuntimeException ex) { error(ex); return; }
            JOptionPane.showMessageDialog(this, "Producto guardado correctamente.", "Producto guardado", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            onSaved.run();
        });
    }

    public void showDialog() { open(); }
}
