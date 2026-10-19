import com.ies21.gestionproductos.domain.model.*;
import com.ies21.gestionproductos.domain.service.*;
import com.ies21.gestionproductos.infrastructure.config.*;
import com.ies21.gestionproductos.infrastructure.persistence.MongoProductRepository;
import java.math.BigDecimal;
import java.util.UUID;

/** Se ejecuta solo con el perfil mongo-verification; limpia únicamente su propio producto. */
public class MongoRepositoryCheck {
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        DatabaseConfig config = DatabaseConfig.load();
        String id = "VERIFY-" + UUID.randomUUID();
        try (MongoProductRepository repository = new MongoProductRepository(config)) {
            repository.ping();
            ProductService products = new ProductService(repository);
            ProductStatisticsService statistics = new ProductStatisticsService(products);
            var original = statistics.getSummary();
            Product product = new Product(id, "Verificación temporal de MongoDB", "Otros", new BigDecimal("12.34"), 3, true);
            try {
                repository.create(product);
                check(products.getProduct(id).equals(product), "Lectura y Decimal128 exacto");
                check(statistics.getSummary().total() == original.total() + 1, "Alta reflejada en estadísticas");
                check(statistics.getSummary().lowStock() == original.lowStock() + 1, "Stock bajo calculado desde Mongo");
                check(products.searchProducts("  VERIFICACIÓN TEMPORAL  ").stream().anyMatch(p -> p.id().equals(id)), "Búsqueda real");
                // Otra conexión debe recuperar lo persistido, sin compartir listas ni estado en memoria.
                try (MongoProductRepository independent = new MongoProductRepository(config)) {
                    check(independent.findById(id).orElseThrow().equals(product), "Persistencia entre conexiones");
                }
                Product updated = new Product(id, "Verificación MongoDB actualizada", "Detergentes", new BigDecimal("15.67"), 6, false);
                products.updateProduct(updated);
                check(products.getProduct(id).equals(updated), "Actualización real con ID estable");
                check(statistics.getSummary().lowStock() == original.lowStock(), "Estadísticas tras editar stock");
                var report = new InventoryReportService(products, AppConfig.clock()).generateReport("Detergentes", ProductStatus.UNAVAILABLE);
                check(report.products().contains(updated), "Reporte consulta MongoDB actualizado");
                products.deleteProduct(id);
                check(repository.findById(id).isEmpty(), "Eliminación real");
                check(statistics.getSummary().total() == original.total(), "Sin registros temporales residuales");
                verifyScreens(products, original.total());
                System.out.println("PASS MongoDB: ping, CRUD, decimales, búsqueda, estadísticas, reportes y persistencia entre conexiones. Productos: " + original.total());
            } finally {
                if (repository.findById(id).isPresent()) repository.delete(id);
            }
        }
    }

    private static void verifyScreens(ProductService products, int expectedTotal) {
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                var window = new com.ies21.gestionproductos.infrastructure.navigation.MainWindow(products);
                try {
                    var shell = ModalSmokeCheck.find(window, com.ies21.gestionproductos.infrastructure.navigation.MainShell.class).getFirst();
                    var table = ModalSmokeCheck.find(shell, javax.swing.JTable.class).getFirst();
                    check(table.getRowCount() == expectedTotal, "La pantalla muestra los productos persistidos");
                    ModalSmokeCheck.button(shell, "Estadísticas").doClick();
                    StatisticsReportsCheck.preview(shell, "target/statistics-mongo-preview.png");
                    ModalSmokeCheck.button(shell, "Reportes").doClick();
                    var report = ModalSmokeCheck.find(shell, javax.swing.JEditorPane.class).getFirst();
                    check(report.getText().contains("Total de productos: " + expectedTotal), "Reporte visual con datos de Atlas");
                    StatisticsReportsCheck.preview(shell, "target/reports-mongo-preview.png");
                } catch (Exception ex) { throw new RuntimeException(ex); }
                finally { window.dispose(); }
            });
        } catch (Exception ex) { throw new RuntimeException("Falló la verificación visual con MongoDB.", ex); }
    }
}
