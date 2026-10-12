package com.ies21.gestionproductos.infrastructure.navigation;

import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.infrastructure.config.AppConfig;
import com.ies21.gestionproductos.infrastructure.components.*;
import com.ies21.gestionproductos.infrastructure.features.products.*;
import com.ies21.gestionproductos.infrastructure.features.statistics.StatisticsPanel;
import com.ies21.gestionproductos.infrastructure.features.reports.ReportsPanel;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Mantiene una instancia de cada vista y un único servicio compartido. */
public class MainShell extends JPanel {
    private final ScreenRouter router = new ScreenRouter();
    private final java.util.Map<String, JButton> navigation = new java.util.LinkedHashMap<>();
    private final ProductsPanel products;
    private final ProductDetailPanel detail;
    private final StatisticsPanel statistics;
    private final ReportsPanel reports;

    /** Constructor usado por NetBeans GUI Builder. */
    public MainShell() { this(AppConfig.createProductService()); }

    public MainShell(ProductService service) {
        setLayout(new BorderLayout());
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(250, 251, 255));
        sidebar.setBorder(new EmptyBorder(32, 20, 24, 20));
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.add(Ui.label("InventoryManager", 20, Ui.INK, true));
        menu.add(Box.createVerticalStrut(6));
        menu.add(Ui.label("Gestión de productos", 12, Ui.MUTED, false));
        menu.add(Box.createVerticalStrut(48));
        menu.add(Ui.label("MÓDULOS PRINCIPALES", 11, Ui.MUTED, true));
        menu.add(Box.createVerticalStrut(20));

        for (String name : new String[]{"Productos", "Estadísticas", "Reportes"}) {
            JButton button = Ui.button(name, false);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            navigation.put(name, button);
            menu.add(button);
            menu.add(Box.createVerticalStrut(12));
        }
        detail = new ProductDetailPanel(service, this::showProducts);
        products = new ProductsPanel(service, product -> {
            detail.showProduct(product.id());
            router.navigate("detalle-producto");
            select("Productos");
        }, this::showProducts);
        statistics = new StatisticsPanel(service);
        reports = new ReportsPanel(service);
        router.register("productos", products);
        router.register("detalle-producto", detail);
        router.register("estadisticas", statistics);
        router.register("reportes", reports);
        navigation.get("Productos").addActionListener(e -> showProducts());
        navigation.get("Estadísticas").addActionListener(e -> Ui.runSafely(this, () -> {
            statistics.refreshData();
            router.navigate("estadisticas");
            select("Estadísticas");
        }));
        navigation.get("Reportes").addActionListener(e -> Ui.runSafely(this, () -> {
            reports.refreshReport();
            router.navigate("reportes");
            select("Reportes");
        }));
        sidebar.add(menu, BorderLayout.NORTH);
        sidebar.add(Ui.label("●  Sesión local · demo", 12, Ui.GREEN, false), BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);
        add(router, BorderLayout.CENTER);
        router.navigate("productos");
        select("Productos");
    }
    private void showProducts() {
        Ui.runSafely(this, () -> {
            products.reloadData();
            router.navigate("productos");
            select("Productos");
        });
    }
    private void select(String selected) {
        navigation.forEach((name, button) -> {
            button.setBackground(name.equals(selected) ? Ui.PRIMARY : Ui.BACKGROUND);
            button.setForeground(name.equals(selected) ? Color.WHITE : Ui.INK);
        });
    }
}
