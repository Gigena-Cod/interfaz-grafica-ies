package com.ies21.gestionproductos.infrastructure.features.products;

import com.ies21.gestionproductos.domain.model.Product;
import com.ies21.gestionproductos.domain.model.ProductStatus;
import com.ies21.gestionproductos.infrastructure.features.products.dialogs.*;
import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.domain.service.ProductStatisticsService;
import com.ies21.gestionproductos.infrastructure.components.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

public final class ProductsPanel extends JPanel {
    private final ProductService service;
    private final ProductStatisticsService statisticsService;
    private final JPanel stats = new JPanel(new GridLayout(1, 3, 18, 0));
    private JTable table;
    private boolean reloading;
    private final JTextField search = new JTextField(24);
    private final JComboBox<String> category = new JComboBox<>();
    private final JComboBox<String> stock = new JComboBox<>(new String[]{"Todo el stock", "Disponible", "Stock bajo", "Agotado"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"PRODUCTO", "CÓDIGO", "CATEGORÍA", "PRECIO", "STOCK", "ESTADO"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
        @Override public Class<?> getColumnClass(int col) { return col == 4 ? Integer.class : String.class; }
    };
    private final JLabel count = Ui.label("", 12, Ui.MUTED, false);

    public ProductsPanel(ProductService service, java.util.function.Consumer<Product> openDetail, Runnable onChanged) {
        this.service = java.util.Objects.requireNonNull(service);
        this.statisticsService = new ProductStatisticsService(service);
        java.util.Objects.requireNonNull(openDetail);
        java.util.Objects.requireNonNull(onChanged);
        setLayout(new BorderLayout(0, 24));
        setBackground(Ui.BACKGROUND);
        setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(Ui.label("CATÁLOGO   /   GESTIÓN DE PRODUCTOS", 12, Ui.PRIMARY, true));
        heading.add(Box.createVerticalStrut(10));
        heading.add(Ui.label("Productos & Almacén", 30, Ui.INK, true));
        heading.add(Box.createVerticalStrut(8));
        heading.add(Ui.label("Consultá tu catálogo, precios y existencias en un solo lugar.", 13, Ui.MUTED, false));
        heading.add(Box.createVerticalStrut(24));
        stats.setOpaque(false);
        heading.add(stats);
        for (Component child : heading.getComponents()) {
            if (child instanceof JComponent component) component.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        add(heading, BorderLayout.NORTH);

        JPanel catalog = new JPanel(new BorderLayout(0, 18));
        catalog.setBackground(Color.WHITE);
        catalog.setBorder(new EmptyBorder(22, 22, 18, 22));
        JPanel tools = new JPanel(new BorderLayout(0, 18));
        tools.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 5));
        titles.setOpaque(false);
        titles.add(Ui.label("Catálogo de productos", 17, Ui.INK, true));
        titles.add(Ui.label("Buscá por nombre o código y combiná los filtros.", 12, Ui.MUTED, false));
        JPanel catalogHeading = new JPanel(new BorderLayout(16, 0));
        catalogHeading.setOpaque(false);
        catalogHeading.add(titles, BorderLayout.CENTER);
        JButton addProduct = Ui.button("+ Agregar producto", true);
        addProduct.addActionListener(e -> Ui.runSafely(this, () ->
                new ProductFormDialog(SwingUtilities.getWindowAncestor(this), service, null, onChanged).showDialog()));
        catalogHeading.add(addProduct, BorderLayout.EAST);
        tools.add(catalogHeading, BorderLayout.NORTH);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        search.setToolTipText("Buscar por producto o código");
        search.getAccessibleContext().setAccessibleName("Buscar por producto o código");
        search.setPreferredSize(new Dimension(260, 34));
        filters.add(search);
        category.addItem("Todas las categorías");
        service.getCategories().forEach(category::addItem);
        category.setPreferredSize(new Dimension(180, 34));
        stock.setPreferredSize(new Dimension(140, 34));
        filters.add(category);
        filters.add(stock);
        JButton find = Ui.button("Buscar", true);
        find.addActionListener(e -> refresh());
        search.addActionListener(e -> refresh());
        filters.add(find);
        JButton clear = Ui.button("Limpiar", false);
        clear.addActionListener(e -> { search.setText(""); category.setSelectedIndex(0); stock.setSelectedIndex(0); });
        filters.add(clear);
        tools.add(filters, BorderLayout.CENTER);
        catalog.add(tools, BorderLayout.NORTH);

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Runnable openSelected = () -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String id = (String) model.getValueAt(table.convertRowIndexToModel(row), 1);
            Ui.runSafely(this, () -> openDetail.accept(service.getProduct(id)));
        };
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2 && table.rowAtPoint(event.getPoint()) >= 0) openSelected.run();
            }
        });
        table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "openDetail");
        table.getActionMap().put("openDetail", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { openSelected.run(); }
        });
        JButton detail = Ui.button("Ver detalle", true);
        detail.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> detail.setEnabled(table.getSelectedRow() >= 0));
        detail.addActionListener(e -> openSelected.run());
        JButton delete = Ui.button("Eliminar producto", false);
        delete.setForeground(Ui.RED);
        delete.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> delete.setEnabled(table.getSelectedRow() >= 0));
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            String id = (String) model.getValueAt(table.convertRowIndexToModel(row), 1);
            Ui.runSafely(this, () -> new DeleteProductDialog(SwingUtilities.getWindowAncestor(this), service, service.getProduct(id), onChanged).showDialog());
        });
        table.setRowHeight(58);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(Ui.INK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(237, 241, 249));
        table.setSelectionBackground(new Color(233, 231, 255));
        table.setSelectionForeground(Ui.PRIMARY);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setForeground(Ui.MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getColumnModel().getColumn(0).setPreferredWidth(320);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setForeground(selected ? Ui.PRIMARY : Ui.INK);
                setBackground(selected ? t.getSelectionBackground() : row % 2 == 0 ? Color.WHITE : new Color(249, 250, 254));
                if (col == 5 && !selected) setForeground("Disponible".equals(value) ? Ui.GREEN : "Stock bajo".equals(value) ? Ui.RED : Ui.MUTED);
                setHorizontalAlignment(col == 3 || col == 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return this;
            }
        };
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(233, 237, 247)));
        catalog.add(scroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.add(count, BorderLayout.CENTER);
        JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rowActions.setOpaque(false);
        rowActions.add(delete);
        rowActions.add(detail);
        footer.add(rowActions, BorderLayout.EAST);
        catalog.add(footer, BorderLayout.SOUTH);
        add(catalog, BorderLayout.CENTER);
        add(Ui.label("MongoDB · Productos guardados en la base de datos", 12, Ui.MUTED, false), BorderLayout.SOUTH);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        category.addActionListener(e -> refresh());
        stock.addActionListener(e -> refresh());
        reloadData();
    }

    /**
     * Recarga categorías e indicadores sin reemplazar el panel ni borrar los filtros.
     * Suspende los eventos del combo durante su reconstrucción para evitar consultas
     * intermedias; después actualiza la tabla y conserva la selección por ID si sigue visible.
     */
    public void reloadData() {
        Object selectedCategory = category.getSelectedItem();
        reloading = true;
        try {
            category.removeAllItems();
            category.addItem("Todas las categorías");
            service.getCategories().forEach(category::addItem);
            for (int i = 0; i < category.getItemCount(); i++)
                if (category.getItemAt(i).equals(selectedCategory)) category.setSelectedIndex(i);
            var summary = statisticsService.getSummary();
            stats.removeAll();
            stats.add(new StatCard("TOTAL DE PRODUCTOS", "" + summary.total(), "Productos registrados en el catálogo", Ui.INK));
            stats.add(new StatCard("DISPONIBLES PARA VENTA", "" + summary.available(), "Con disponibilidad para la venta", Ui.GREEN));
            stats.add(new StatCard("PRODUCTOS CON STOCK BAJO", "" + summary.lowStock(), "Menos de 5 unidades · incluye agotados", Ui.RED));
        } finally { reloading = false; }
        refresh();
        revalidate();
        repaint();
    }

    /**
     * Traduce los filtros visibles a criterios del servicio y reconstruye el modelo.
     * Convierte índices entre vista y modelo para conservar la identidad seleccionada
     * aunque el usuario haya ordenado la tabla. Presenta los errores mediante Ui.
     */
    private void refresh() {
        if (reloading) return;
        Ui.runSafely(this, () -> {
            String selectedId = table.getSelectedRow() < 0 ? null :
                    (String) model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 1);
            ProductStatus status = switch (stock.getSelectedIndex()) {
                case 1 -> ProductStatus.AVAILABLE;
                case 2 -> ProductStatus.LOW_STOCK;
                case 3 -> ProductStatus.OUT_OF_STOCK;
                default -> ProductStatus.ALL;
            };
            var filtered = service.filterProducts(search.getText(), category.getSelectedIndex() > 0 ? (String) category.getSelectedItem() : null, status);
            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
            model.setRowCount(0);
            for (Product p : filtered) {
                model.addRow(new Object[]{p.name(), p.id(), p.category(), currency.format(p.price()), p.stock(),
                    p.stock() == 0 ? "Agotado" : !p.available() ? "No disponible" : ProductService.isLowStock(p) ? "Stock bajo" : "Disponible"});
                if (p.id().equals(selectedId)) {
                    int viewRow = table.convertRowIndexToView(model.getRowCount() - 1);
                    if (viewRow >= 0) table.setRowSelectionInterval(viewRow, viewRow);
                }
            }
            count.setText(filtered.isEmpty() ? "No se encontraron productos con estos filtros." :
                    "Mostrando " + filtered.size() + " de " + service.getTotalProducts() + " productos");
        });
    }
}
