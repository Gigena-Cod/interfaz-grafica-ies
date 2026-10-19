package com.ies21.gestionproductos.infrastructure.features.reports;

import com.ies21.gestionproductos.domain.service.*;
import com.ies21.gestionproductos.infrastructure.components.*;
import java.awt.*;
import com.ies21.gestionproductos.infrastructure.config.AppConfig;
import com.ies21.gestionproductos.domain.model.ProductStatus;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class ReportsPanel extends JPanel {
    private final InventoryReportService service;
    private final ProductService products;
    private final JEditorPane report=new JEditorPane("text/html","");
    private final JComboBox<String> category=new JComboBox<>();
    private final JComboBox<String> status=new JComboBox<>(new String[]{"Todos los estados","Disponibles","No disponibles","Stock bajo","Agotados"});
    private final JPanel stats=new JPanel(new GridLayout(1,4,12,0));
    public ReportsPanel(ProductService products) {
        super(new BorderLayout(0,16)); this.products=products;
        service=new InventoryReportService(products,AppConfig.clock());
        setBackground(Ui.BACKGROUND); setBorder(new EmptyBorder(22,24,22,24));
        JPanel top=new JPanel(new BorderLayout(0,14)); top.setOpaque(false);
        JPanel titles=new JPanel(new GridLayout(2,1,0,5)); titles.setOpaque(false);
        titles.add(Ui.label("Reportes",26,Ui.INK,true)); titles.add(Ui.label("Generación y visualización de reportes del inventario",13,Ui.MUTED,false)); top.add(titles,BorderLayout.NORTH);
        JPanel controls=new JPanel(new BorderLayout(0,12)); controls.setBackground(Color.WHITE); controls.setBorder(new EmptyBorder(16,16,16,16));
        controls.add(Ui.label("Generar reporte de inventario",16,Ui.INK,true),BorderLayout.NORTH);
        JPanel filters=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); filters.setOpaque(false);
        category.setPreferredSize(new Dimension(180,32)); status.setPreferredSize(new Dimension(170,32));
        filters.add(Ui.label("Categoría",12,Ui.MUTED,false)); filters.add(category); filters.add(Ui.label("Estado",12,Ui.MUTED,false)); filters.add(status);
        JButton clear=Ui.button("Limpiar",false); clear.addActionListener(e->Ui.runSafely(this, ()->{category.setSelectedIndex(0);status.setSelectedIndex(0);refreshReport();})); filters.add(clear);
        JButton generate=Ui.button("Generar reporte",true); generate.addActionListener(e->Ui.runSafely(this, this::refreshReport)); filters.add(generate);
        controls.add(filters,BorderLayout.CENTER); top.add(controls,BorderLayout.CENTER); stats.setOpaque(false); top.add(stats,BorderLayout.SOUTH); add(top,BorderLayout.NORTH);
        JPanel preview=new JPanel(new BorderLayout(0,10)); preview.setBackground(Color.WHITE); preview.setBorder(new EmptyBorder(14,14,14,14));
        preview.add(Ui.label("Vista previa del documento · Solo lectura",13,Ui.INK,true),BorderLayout.NORTH);
        report.setEditable(false); report.getAccessibleContext().setAccessibleName("Reporte de inventario");
        JScrollPane scroll=new JScrollPane(report); scroll.setBorder(BorderFactory.createLineBorder(new Color(225,232,243))); preview.add(scroll); add(preview);
        add(Ui.label("MongoDB · Los filtros se aplican al generar el reporte · Valores en ARS",12,Ui.MUTED,false),BorderLayout.SOUTH);
        reloadCategories();
    }
    private void reloadCategories() {
        Object selected=category.getSelectedItem(); category.removeAllItems(); category.addItem("Todas las categorías");
        products.getCategories().forEach(category::addItem);
        for(int i=0;i<category.getItemCount();i++) if(category.getItemAt(i).equals(selected)) category.setSelectedIndex(i);
    }
    /**
     * Genera una nueva instantánea con los filtros elegidos y la presenta en modo lectura.
     * Actualiza los indicadores con esa misma instantánea para que coincidan con el documento.
     */
    public void refreshReport() {
        reloadCategories();
        var generated=service.generateReport(category.getSelectedIndex()==0?null:(String)category.getSelectedItem(),ProductStatus.values()[status.getSelectedIndex()]);
        report.setText(new InventoryReportRenderer().renderHtml(generated)); report.setCaretPosition(0); stats.removeAll();
        stats.add(new StatCard("TOTAL EN REPORTE",""+generated.total(),"Productos incluidos",Ui.INK,true));
        stats.add(new StatCard("DISPONIBLES",""+generated.available(),"Habilitados para la venta",Ui.GREEN,true));
        stats.add(new StatCard("STOCK BAJO",""+generated.lowStock(),"Menos de 5 unidades",Ui.RED,true));
        stats.add(new StatCard("VALUACIÓN",NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")).format(generated.value()),"Precio × stock · ARS",Ui.PRIMARY,true));
        revalidate(); repaint();
    }
}
