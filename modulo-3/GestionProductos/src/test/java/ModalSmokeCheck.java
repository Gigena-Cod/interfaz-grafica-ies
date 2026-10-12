import com.ies21.gestionproductos.infrastructure.navigation.MainShell;
import com.ies21.gestionproductos.infrastructure.features.products.dialogs.*;
import com.ies21.gestionproductos.domain.model.ProductStatus;
import com.ies21.gestionproductos.infrastructure.components.*;
import com.ies21.gestionproductos.infrastructure.features.products.*;
import com.ies21.gestionproductos.domain.service.*;
import com.ies21.gestionproductos.infrastructure.persistence.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Ejecutar con javac/java y un escritorio disponible. No requiere dependencias. */
public class ModalSmokeCheck {
 static <T> java.util.List<T> find(Container root, Class<T> type) {
  var found = new ArrayList<T>();
  for (Component c : root.getComponents()) {
   if (type.isInstance(c)) found.add(type.cast(c));
   if (c instanceof Container nested) found.addAll(find(nested, type));
  }
  return found;
 }
 static JButton button(Container root, String text) { return find(root,JButton.class).stream().filter(b->text.equals(b.getText())).findFirst().orElseThrow(); }
 static JTextField field(Container root,String text) { return (JTextField)find(root,JLabel.class).stream().filter(l->text.equals(l.getText())).findFirst().orElseThrow().getLabelFor(); }
 static void check(boolean ok, String message) { if(!ok) throw new AssertionError(message); }
 static void clickWithMessage(JButton button) {
  javax.swing.Timer closer = new javax.swing.Timer(80,e->{
   for(Window w:Window.getWindows()) if(w instanceof JDialog d && d.isShowing() && !find(d,JOptionPane.class).isEmpty()) d.dispose();
  });
  closer.start();
  try { button.doClick(); } finally { closer.stop(); }
 }
 static void layout(Container root) { root.doLayout(); for(Component c:root.getComponents()) if(c instanceof Container nested) layout(nested); }
 static void preview(JDialog dialog,String file) throws Exception {
  dialog.pack(); dialog.setSize(620,dialog.getHeight());
  Container pane=dialog.getContentPane(); pane.setSize(620,dialog.getHeight()-dialog.getInsets().top-dialog.getInsets().bottom); layout(pane); layout(pane);
  BufferedImage image=new BufferedImage(pane.getWidth(),pane.getHeight(),BufferedImage.TYPE_INT_RGB);
  Graphics2D g=image.createGraphics(); g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON); pane.printAll(g); g.dispose();
  ImageIO.write(image,"png",new File(file));
 }
 public static void main(String[] args) throws Exception {
  try {
   SwingUtilities.invokeAndWait(()->{ try {
    ProductService service=new ProductService(new InMemoryProductRepository());
    int[] changes={0};
    ProductFormDialog add=new ProductFormDialog(null,service,null,()->changes[0]++);
    preview(add,"target/add-product-preview.png");
    field(add,"Nombre *").setText("  Producto de prueba  ");
    find(add,JComboBox.class).getFirst().setSelectedItem("Otros");
    field(add,"Precio (ARS) *").setText("0");
    clickWithMessage(button(add,"Guardar producto"));
    check(service.getProducts().size()==8 && changes[0]==0,"Zero price rejected");
    field(add,"Precio (ARS) *").setText("12,50");
    field(add,"Stock (unidades) *").setText("-1");
    clickWithMessage(button(add,"Guardar producto"));
    check(service.getProducts().size()==8,"Negative stock rejected");
    field(add,"Stock (unidades) *").setText("4");
    clickWithMessage(button(add,"Guardar producto"));
    check(service.getProducts().size()==9 && changes[0]==1,"Create callback");
    var created=service.getProducts().stream().filter(p->p.name().equals("Producto de prueba")).findFirst().orElseThrow();
    check(created.price().toPlainString().equals("12.50"),"Decimal comma and trim");
    ProductFormDialog edit=new ProductFormDialog(null,service,created,()->changes[0]++);
    preview(edit,"target/edit-product-preview.png");
    field(edit,"Nombre *").setText("Nombre modificado");
    clickWithMessage(button(edit,"Guardar cambios"));
    check(service.getProduct(created.id()).name().equals("Nombre modificado") && changes[0]==2,"Stable ID update");
    ProductFormDialog cancel=new ProductFormDialog(null,service,service.getProduct(created.id()),()->changes[0]++);
    field(cancel,"Nombre *").setText("No guardar"); button(cancel,"Cancelar").doClick();
    check(service.getProduct(created.id()).name().equals("Nombre modificado") && changes[0]==2,"Cancel edit");
    DeleteProductDialog deletion=new DeleteProductDialog(null,service,service.getProduct(created.id()),()->changes[0]++);
    preview(deletion,"target/delete-product-preview.png");
    JButton remove=button(deletion,"Eliminar producto");
    JTextField confirmation=field(deletion,"Escribí ELIMINAR para confirmar");
    check(!remove.isEnabled(),"Delete initially disabled");
    confirmation.setText("eliminar"); check(!remove.isEnabled(),"Exact phrase required");
    confirmation.setText("ELIMINAR"); check(remove.isEnabled(),"Delete enabled");
    confirmation.setText(""); check(!remove.isEnabled(),"Delete disabled again");
    confirmation.setText("ELIMINAR"); clickWithMessage(remove);
    check(service.getProducts().size()==8 && changes[0]==3,"Confirmed deletion");
    DeleteProductDialog canceledDelete=new DeleteProductDialog(null,service,service.getProducts().getFirst(),()->changes[0]++);
    button(canceledDelete,"Cancelar").doClick(); check(service.getProducts().size()==8,"Cancel delete");
    MainShell shell=new MainShell(service);
    JTable table=find(shell,JTable.class).getFirst();
    check(!button(find(shell,ProductsPanel.class).getFirst(),"Eliminar producto").isEnabled(),"No selection no deletion");
    table.getRowSorter().toggleSortOrder(0); table.setRowSelectionInterval(0,0);
    String selected=table.getValueAt(0,1).toString(); button(shell,"Ver detalle").doClick();
    var detail=find(shell,com.ies21.gestionproductos.infrastructure.features.products.ProductDetailPanel.class).getFirst();
    check(detail.isVisible(),"Open selected sorted product");
    check(find(detail,JLabel.class).stream().anyMatch(l->l.getText().endsWith(selected)),"Selected ID detail");
    button(detail,"← Volver al catálogo").doClick();
    check(find(shell,JTable.class).getFirst().getRowCount()==8,"Catalog rebuilt");
    System.out.println("PASS: add, validation, decimal comma, edit, immutable ID, cancel, exact deletion phrase, delete, sorted navigation and catalog reload.");
   } catch(Exception ex) { throw new RuntimeException(ex); } finally { for(Window w:Window.getWindows()) w.dispose(); } });
  } catch(Exception ex) { ex.printStackTrace(); System.exit(1); }
 }
}
