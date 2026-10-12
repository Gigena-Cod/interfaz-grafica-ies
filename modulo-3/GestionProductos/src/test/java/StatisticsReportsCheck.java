import com.ies21.gestionproductos.infrastructure.navigation.MainShell;
import com.ies21.gestionproductos.infrastructure.features.products.dialogs.*;
import com.ies21.gestionproductos.domain.model.ProductStatus;
import com.ies21.gestionproductos.domain.service.*;
import com.ies21.gestionproductos.infrastructure.persistence.*;
import com.ies21.gestionproductos.domain.model.Product;
import com.ies21.gestionproductos.infrastructure.components.*;
import com.ies21.gestionproductos.infrastructure.features.products.*;
import com.ies21.gestionproductos.infrastructure.features.statistics.*;
import com.ies21.gestionproductos.infrastructure.features.reports.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class StatisticsReportsCheck {
 static <T> java.util.List<T> find(Container root, Class<T> type) {
  var result=new ArrayList<T>();
  for(Component c:root.getComponents()) { if(type.isInstance(c)) result.add(type.cast(c)); if(c instanceof Container nested) result.addAll(find(nested,type)); }
  return result;
 }
 static JButton button(Container root,String name) { return find(root,JButton.class).stream().filter(b->name.equals(b.getText())).findFirst().orElseThrow(); }
 static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 static void layout(Container root) { root.doLayout(); for(Component c:root.getComponents()) if(c instanceof Container nested) layout(nested); }
 static void preview(MainShell shell,String path) throws Exception {
  shell.setSize(1366,768); layout(shell); layout(shell);
  BufferedImage image=new BufferedImage(1366,768,BufferedImage.TYPE_INT_RGB);
  Graphics2D g=image.createGraphics(); g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON); shell.printAll(g); g.dispose();
  ImageIO.write(image,"png",new File(path));
 }
 public static void main(String[] args) throws Exception {
  SwingUtilities.invokeAndWait(()->{ try {
   ProductService service=new ProductService(new InMemoryProductRepository());
   MainShell shell=new MainShell(service);
   ProductsPanel catalog=find(shell,ProductsPanel.class).getFirst();
   JTable table=find(catalog,JTable.class).getFirst();
   JTextField search=find(catalog,JTextField.class).getFirst();
   search.setText("  dEtErGeNtE  "); button(catalog,"Buscar").doClick(); check(table.getRowCount()==2,"Case insensitive search and trim");
   search.setText("no match"); button(catalog,"Buscar").doClick(); check(table.getRowCount()==0,"No matches");
   check(find(catalog,JLabel.class).stream().anyMatch(l->l.getText().contains("No se encontraron")),"Empty search message");
   button(catalog,"Limpiar").doClick(); check(table.getRowCount()==8 && search.getText().isEmpty(),"Clear search");
   button(catalog,"Buscar").doClick(); check(table.getRowCount()==8,"Empty query all products");
   button(shell,"Estadísticas").doClick();
   StatisticsPanel statistics=find(shell,StatisticsPanel.class).getFirst();
   check(statistics.isVisible(),"Statistics navigation");
   CategoryBarChart chart=find(statistics,CategoryBarChart.class).getFirst();
   check(chart.getAccessibleContext().getAccessibleName().contains("Detergentes=2"),"Category count");
   check(ProductService.isLowStock(new Product("x","x","Otros",BigDecimal.ONE,4,true)),"Four units low");
   check(!ProductService.isLowStock(new Product("x","x","Otros",BigDecimal.ONE,5,true)),"Five units not low");
   preview(shell,"target/statistics-preview.png");
   service.createProduct("Producto agregado", "Detergentes", new BigDecimal("25.50"), 3, true);
   button(shell,"Productos").doClick(); button(shell,"Estadísticas").doClick();
   check(chart.getAccessibleContext().getAccessibleName().contains("Detergentes=3"),"Refresh after add");
   button(shell,"Reportes").doClick();
   ReportsPanel reports=find(shell,ReportsPanel.class).getFirst();
   JEditorPane text=find(reports,JEditorPane.class).getFirst();
   check(reports.isVisible() && !text.isEditable(),"Readonly report navigation");
   check(text.getText().contains("Total de productos: 9") && text.getText().contains("Producto agregado"),"Current report");
   var added=service.getProducts().stream().filter(p->p.name().equals("Producto agregado")).findFirst().orElseThrow();
   service.deleteProduct(added.id()); button(reports,"Generar reporte").doClick();
   check(text.getText().contains("Total de productos: 8") && !text.getText().contains("Producto agregado"),"Regenerate after delete");
   var filtered=new InventoryReportService(service,Clock.systemUTC()).generateReport("Detergentes",ProductStatus.ALL);
   check(filtered.total()==2 && filtered.value().compareTo(new BigDecimal("101700"))==0,"Filtered valuation");
   var low=new InventoryReportService(service,Clock.systemUTC()).generateReport(null,ProductStatus.LOW_STOCK);
   check(low.total()==1 && low.lowStock()==1,"Low stock report filter");
   String fixed=new InventoryReportRenderer().renderText(new InventoryReportService(service,Clock.fixed(Instant.parse("2026-10-12T01:00:00Z"),ZoneId.of("America/Argentina/Buenos_Aires"))).generateReport(null,ProductStatus.ALL));
   check(fixed.contains("11/10/2026 22:00:00"),"Argentina date and time");
   for(Product product:service.getProducts()) check(fixed.contains(product.id()) && fixed.contains(product.name()) && fixed.contains(product.category()),"All report records");
   preview(shell,"target/reports-preview.png");
   for(Product product:service.getProducts()) service.deleteProduct(product.id());
   statistics.refreshData(); reports.refreshReport();
   check(chart.getAccessibleContext().getAccessibleName().contains("Detergentes=0"),"Empty chart data");
   check(text.getText().contains("No hay productos registrados para estos filtros."),"Empty report");
   System.out.println("PASS: search, clear, empty results, navigation, category counts, threshold boundaries, refresh after CRUD, report fields, local date and empty states.");
  } catch(Exception ex) { throw new RuntimeException(ex); } });
 }
}
