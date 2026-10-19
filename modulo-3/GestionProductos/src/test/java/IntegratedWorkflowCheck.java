import com.ies21.gestionproductos.infrastructure.navigation.*;
import com.ies21.gestionproductos.infrastructure.features.products.*;
import com.ies21.gestionproductos.infrastructure.features.products.dialogs.*;
import com.ies21.gestionproductos.infrastructure.features.statistics.*;
import com.ies21.gestionproductos.infrastructure.features.reports.*;
import com.ies21.gestionproductos.domain.service.*;
import com.ies21.gestionproductos.domain.model.*;
import com.ies21.gestionproductos.infrastructure.persistence.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.function.Consumer;
import javax.swing.*;

/** Prueba de la secuencia completa sobre los botones reales de MainWindow. */
public class IntegratedWorkflowCheck {
    static <T extends JDialog> void openModal(JButton trigger, Class<T> type, Consumer<T> action) {
        Throwable[] failure = {null};
        javax.swing.Timer driver = new javax.swing.Timer(50, null);
        driver.addActionListener(e -> {
            for (Window window : Window.getWindows()) {
                if (type.isInstance(window) && window.isShowing()) {
                    driver.stop();
                    try { action.accept(type.cast(window)); }
                    catch (Throwable ex) { failure[0] = ex; window.dispose(); }
                    return;
                }
            }
        });
        javax.swing.Timer timeout = new javax.swing.Timer(8000, e -> {
            failure[0] = new AssertionError("El modal no completó su operación");
            driver.stop();
            for (Window window : Window.getWindows()) if (window instanceof JDialog) window.dispose();
        });
        timeout.setRepeats(false);
        driver.start();
        timeout.start();
        try { trigger.doClick(); }
        finally { driver.stop(); timeout.stop(); }
        if (failure[0] != null) throw new AssertionError("Falló el flujo del modal", failure[0]);
    }
    static void check(boolean ok, String message) { ModalSmokeCheck.check(ok, message); }
    static void invalid(Runnable action) {
        try { action.run(); throw new AssertionError("Se aceptó un dato inválido"); }
        catch (IllegalArgumentException expected) { check(expected.getMessage() != null, "Error comprensible"); }
    }
    public static void main(String[] args) throws Exception {
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    ProductService isolated = new ProductService(new InMemoryProductRepository());
                    invalid(() -> isolated.createProduct(null, "Otros", BigDecimal.ONE, 0, true));
                    invalid(() -> isolated.createProduct("Nombre", null, BigDecimal.ONE, 0, true));
                    invalid(() -> isolated.getProduct("inexistente"));
                    invalid(() -> isolated.updateProduct(new Product("inexistente", "Nombre", "Otros", BigDecimal.ONE, 0, true)));
                    check(isolated.getTotalProducts() == 8, "Validaciones sin efectos parciales");
                    isolated.createProduct(" <b> & producto ", " Otros ", BigDecimal.ONE, 1, true);
                    String escaped = new InventoryReportRenderer().renderHtml(new InventoryReportService(isolated, java.time.Clock.systemUTC()).generateReport(null,ProductStatus.ALL));
                    check(escaped.contains("&lt;b&gt; &amp; producto"), "HTML escapado");

                    MainWindow window = new MainWindow(new ProductService(new InMemoryProductRepository()));
                    MainShell shell = ModalSmokeCheck.find(window, MainShell.class).getFirst();
                    ProductsPanel catalog = ModalSmokeCheck.find(shell, ProductsPanel.class).getFirst();
                    JTable table = ModalSmokeCheck.find(catalog, JTable.class).getFirst();
                    JTextField search = ModalSmokeCheck.find(catalog, JTextField.class).getFirst();
                    var chart = ModalSmokeCheck.find(shell, CategoryBarChart.class).getFirst();
                    openModal(ModalSmokeCheck.button(catalog,"+ Agregar producto"), ProductFormDialog.class, dialog -> {
                        ModalSmokeCheck.field(dialog,"Nombre *").setText("  Producto integrado  ");
                        ModalSmokeCheck.find(dialog,JComboBox.class).getFirst().setSelectedItem("Detergentes");
                        ModalSmokeCheck.field(dialog,"Precio (ARS) *").setText("15,50");
                        ModalSmokeCheck.field(dialog,"Stock (unidades) *").setText("3");
                        ModalSmokeCheck.clickWithMessage(ModalSmokeCheck.button(dialog,"Guardar producto"));
                    });
                    check(ModalSmokeCheck.find(shell,ProductsPanel.class).getFirst() == catalog, "Una sola instancia del catálogo");
                    check(table.getRowCount() == 9, "Alta actualiza el listado");
                    search.setText("PRODUCTO INTEGRADO");
                    check(table.getRowCount() == 1, "Alta encontrable por búsqueda");
                    String id = table.getValueAt(0,1).toString();
                    table.setRowSelectionInterval(0,0);
                    ModalSmokeCheck.button(catalog,"Ver detalle").doClick();
                    var detail = ModalSmokeCheck.find(shell,ProductDetailPanel.class).getFirst();
                    check(detail.isVisible(), "Detalle visible");
                    openModal(ModalSmokeCheck.button(detail,"Editar producto"), ProductFormDialog.class, dialog -> {
                        check(ModalSmokeCheck.field(dialog,"Nombre *").getText().equals("Producto integrado"), "Detalle consulta datos actuales");
                        ModalSmokeCheck.field(dialog,"Nombre *").setText("Producto integrado actualizado");
                        ModalSmokeCheck.field(dialog,"Stock (unidades) *").setText("4");
                        ModalSmokeCheck.clickWithMessage(ModalSmokeCheck.button(dialog,"Guardar cambios"));
                    });
                    check(ModalSmokeCheck.find(detail,JTextField.class).stream().anyMatch(field -> field.getText().equals("Producto integrado actualizado")), "Detalle recarga tras editar");
                    ModalSmokeCheck.button(detail,"← Volver al catálogo").doClick();
                    check(search.getText().equals("PRODUCTO INTEGRADO") && table.getRowCount() == 1, "Filtros conservados");
                    check(table.getValueAt(0,1).equals(id) && table.getValueAt(0,4).equals(4), "ID estable y datos actualizados");
                    check(table.getSelectedRow() == 0, "Selección conservada");
                    ModalSmokeCheck.button(shell,"Estadísticas").doClick();
                    check(chart.getAccessibleContext().getAccessibleName().contains("Detergentes=3"), "Estadísticas comparten datos");
                    ModalSmokeCheck.button(shell,"Reportes").doClick();
                    JEditorPane report = ModalSmokeCheck.find(shell,JEditorPane.class).getFirst();
                    check(report.getText().contains("Producto integrado actualizado") && report.getText().contains("Total de productos: 9"), "Reporte comparte datos");
                    ModalSmokeCheck.button(shell,"Productos").doClick();
                    openModal(ModalSmokeCheck.button(catalog,"Eliminar producto"), DeleteProductDialog.class, dialog -> {
                        ModalSmokeCheck.field(dialog,"Escribí ELIMINAR para confirmar").setText("ELIMINAR");
                        ModalSmokeCheck.clickWithMessage(ModalSmokeCheck.button(dialog,"Eliminar producto"));
                    });
                    check(table.getRowCount() == 0, "Eliminado desaparece de búsqueda");
                    ModalSmokeCheck.button(catalog,"Limpiar").doClick(); check(table.getRowCount() == 8, "Catálogo recupera cantidad original");
                    ModalSmokeCheck.button(shell,"Estadísticas").doClick();
                    check(chart.getAccessibleContext().getAccessibleName().contains("Detergentes=2"), "Eliminado desaparece de estadísticas");
                    ModalSmokeCheck.button(shell,"Reportes").doClick();
                    check(!report.getText().contains("Producto integrado actualizado") && report.getText().contains("Total de productos: 8"), "Eliminado desaparece de reportes");
                    check(ModalSmokeCheck.find(shell,ScreenRouter.class).getFirst().getComponentCount() == 4, "Cuatro vistas sin duplicados");
                    check(java.util.Arrays.stream(Window.getWindows()).filter(w -> w instanceof JFrame).count() == 1, "Un único JFrame");
                    System.out.println("PASS: validaciones, escape HTML y flujo completo en MainWindow: alta, búsqueda, detalle, edición, filtros/selección, estadísticas, reporte y eliminación.");
                } finally { for (Window window : Window.getWindows()) window.dispose(); }
            });
        } catch (Exception ex) { ex.printStackTrace(); System.exit(1); }
    }
}
