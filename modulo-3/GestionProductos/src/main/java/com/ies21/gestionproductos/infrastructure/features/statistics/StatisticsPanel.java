package com.ies21.gestionproductos.infrastructure.features.statistics;

import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.domain.service.ProductStatisticsService;
import com.ies21.gestionproductos.infrastructure.components.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class StatisticsPanel extends JPanel {
    private final ProductStatisticsService service;
    private final JPanel stats=new JPanel(new GridLayout(1,3,14,0));
    private final JPanel summary=new JPanel();
    private final CategoryBarChart chart=new CategoryBarChart();
    public StatisticsPanel(ProductService service) {
        super(new BorderLayout(0,18)); this.service=new ProductStatisticsService(service);
        setBackground(Ui.BACKGROUND); setBorder(new EmptyBorder(22,24,22,24));
        JPanel heading=new JPanel(new BorderLayout(0,18)); heading.setOpaque(false);
        JPanel titles=new JPanel(new GridLayout(2,1,0,5)); titles.setOpaque(false);
        titles.add(Ui.label("Estadísticas",26,Ui.INK,true));
        titles.add(Ui.label("Resumen visual del inventario y distribución por categoría",13,Ui.MUTED,false));
        JPanel titleRow=new JPanel(new BorderLayout()); titleRow.setOpaque(false); titleRow.add(titles);
        JButton refresh=Ui.button("Actualizar estadísticas",true); refresh.addActionListener(e->Ui.runSafely(this, this::refreshData));
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); actions.setOpaque(false); actions.add(refresh); titleRow.add(actions,BorderLayout.EAST);
        heading.add(titleRow,BorderLayout.NORTH); stats.setOpaque(false); heading.add(stats); add(heading,BorderLayout.NORTH);
        JPanel content=new JPanel(new BorderLayout(16,0)); content.setOpaque(false);
        JPanel card=card(); card.add(Ui.label("Productos por categoría",18,Ui.INK,true),BorderLayout.NORTH);
        card.add(chart); card.add(Ui.label("Cantidad de registros · No representa unidades de stock",12,Ui.MUTED,false),BorderLayout.SOUTH);
        content.add(card); summary.setLayout(new BoxLayout(summary,BoxLayout.Y_AXIS)); summary.setOpaque(false); summary.setPreferredSize(new Dimension(290,0));
        JScrollPane scroll=new JScrollPane(summary); scroll.setBorder(null); scroll.setPreferredSize(new Dimension(310,0)); scroll.getViewport().setBackground(Ui.BACKGROUND);
        content.add(scroll,BorderLayout.EAST); add(content);
        add(Ui.label("MongoDB · Estadísticas calculadas desde los productos guardados",12,Ui.MUTED,false),BorderLayout.SOUTH); refreshData();
    }
    private JPanel card() { JPanel panel=new JPanel(new BorderLayout(0,20)); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(20,20,20,20)); return panel; }
    private void line(JPanel panel,String text,Color color,boolean bold) { JLabel label=Ui.label(text,12,color,bold); label.setAlignmentX(LEFT_ALIGNMENT); panel.add(label); panel.add(Box.createVerticalStrut(12)); }
    /**
     * Consulta un resumen actual y actualiza tarjetas, barras y alertas de reposición.
     * Las barras cuentan registros de producto, no unidades disponibles en stock.
     */
    public void refreshData() {
        var data=service.getSummary(); stats.removeAll();
        stats.add(new StatCard("TOTAL DE PRODUCTOS",""+data.total(),"Registros del catálogo",Ui.INK));
        stats.add(new StatCard("PRODUCTOS DISPONIBLES",""+data.available(),"Habilitados para la venta",Ui.GREEN));
        stats.add(new StatCard("PRODUCTOS CON STOCK BAJO",""+data.lowStock(),"Stock < 5 unidades",Ui.RED));
        var counts=data.byCategory(); chart.setCounts(counts);
        summary.removeAll();
        JPanel distribution=new JPanel(); distribution.setBackground(Color.WHITE); distribution.setBorder(new EmptyBorder(18,18,18,18)); distribution.setLayout(new BoxLayout(distribution,BoxLayout.Y_AXIS));
        line(distribution,"Resumen de distribución",Ui.INK,true);
        var largest=counts.entrySet().stream().max(java.util.Map.Entry.comparingByValue()).orElseThrow();
        line(distribution,data.total()==0?"Sin productos registrados":"Mayor presencia: "+largest.getKey(),Ui.INK,true);
        for(var entry:counts.entrySet()) line(distribution,entry.getKey()+": "+entry.getValue()+" productos",Ui.MUTED,false);
        line(distribution,"Categorías activas: "+counts.values().stream().filter(n->n>0).count(),Ui.INK,false);
        summary.add(distribution); summary.add(Box.createVerticalStrut(16));
        JPanel alerts=new JPanel(); alerts.setBackground(Color.WHITE); alerts.setBorder(new EmptyBorder(18,18,18,18)); alerts.setLayout(new BoxLayout(alerts,BoxLayout.Y_AXIS));
        line(alerts,"Alertas de reposición",Ui.RED,true);
        var low=data.lowStockByCategory();
        if(data.lowStock()==0) line(alerts,"Sin productos con stock bajo",Ui.GREEN,false);
        for(var entry:counts.entrySet()) {
            long amount=low.getOrDefault(entry.getKey(),0L);
            if(amount>0) line(alerts,entry.getKey()+": "+amount+" con stock bajo",Ui.RED,false);
        }
        line(alerts,"Agotados: "+data.outOfStock(),Ui.MUTED,false);
        summary.add(alerts); summary.add(Box.createVerticalGlue()); revalidate(); repaint();
    }
}
