package com.ies21.gestionproductos.infrastructure.features.statistics;
import com.ies21.gestionproductos.infrastructure.components.Ui;

import java.awt.*;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.swing.*;

/** Gráfico vertical de cantidades de productos, sin dependencias externas. */
public final class CategoryBarChart extends JPanel {
    private Map<String, Long> counts = Map.of();
    private static final Color[] COLORS = {new Color(20,79,128), new Color(42,97,231), new Color(13,148,136), new Color(100,116,139)};
    public CategoryBarChart() { setBackground(Color.WHITE); setPreferredSize(new Dimension(560,300)); }
    public void setCounts(Map<String,Long> values) {
        counts = new LinkedHashMap<>(values);
        getAccessibleContext().setAccessibleName("Productos por categoría: " + counts);
        repaint();
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("Segoe UI",Font.PLAIN,12));
            long max=counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
            long total=counts.values().stream().mapToLong(Long::longValue).sum();
            if(max==0) { g.setColor(Ui.MUTED); g.drawString("Sin productos registrados",24,70); return; }
            int left=45, top=40, bottom=getHeight()-65, width=Math.max(1,getWidth()-65), height=Math.max(1,bottom-top);
            long scale=Math.max(5,max+(max+3)/4);
            for(int i=0;i<=5;i++) {
                int y=bottom-height*i/5;
                g.setColor(new Color(232,237,245)); g.drawLine(left,y,left+width,y);
                g.setColor(Ui.MUTED); g.drawString(String.format(java.util.Locale.ROOT,"%.1f",scale*i/5.0),5,y+4);
            }
            int slot=width/counts.size(), index=0;
            for(var entry:counts.entrySet()) {
                int barWidth=Math.max(8,(int)(slot*0.55)), x=left+index*slot+(slot-barWidth)/2;
                int h=(int)(height*entry.getValue()/(double)scale), y=bottom-h;
                g.setColor(COLORS[index%COLORS.length]); g.fillRect(x,y,barWidth,h);
                g.setColor(Ui.INK);
                String value=entry.getValue()+" productos";
                g.drawString(value,x+(barWidth-g.getFontMetrics().stringWidth(value))/2,y-8);
                String percent=String.format(java.util.Locale.forLanguageTag("es-AR"),"%.1f%%",entry.getValue()*100.0/total);
                g.setColor(Ui.MUTED); g.drawString(percent,x+(barWidth-g.getFontMetrics().stringWidth(percent))/2,y-24);
                String label=entry.getKey();
                while(g.getFontMetrics().stringWidth(label)>slot-8 && label.length()>3) label=label.substring(0,label.length()-2)+"…";
                g.setColor(Ui.INK); g.drawString(label,left+index*slot+(slot-g.getFontMetrics().stringWidth(label))/2,bottom+24);
                index++;
            }
        } finally { g.dispose(); }
    }
}
