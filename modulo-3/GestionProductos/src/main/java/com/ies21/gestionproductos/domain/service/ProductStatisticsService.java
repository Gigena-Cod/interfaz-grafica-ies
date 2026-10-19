package com.ies21.gestionproductos.domain.service;

import com.ies21.gestionproductos.domain.model.*;
import java.util.*;

/**
 * Calcula las estadísticas desde los productos actuales, sin mantener estadísticas
 * persistidas ni datos de ejemplo independientes del catálogo.
 */
public final class ProductStatisticsService {
    private final ProductService productService;
    /**
     * @param productService servicio que accede al mismo repositorio que utiliza el catálogo
     */
    public ProductStatisticsService(ProductService productService) {
        this.productService = Objects.requireNonNull(productService);
    }
    /**
     * Lee los productos una vez y agrupa registros y alertas por categoría.
     * Mantiene las categorías estándar con valor cero e incorpora otras existentes.
     * Disponible se determina por el campo booleano; stock bajo incluye agotados.
     * @return resumen inmutable con totales y distribuciones de esa lectura
     */
    public InventorySummary getSummary() {
        var products = productService.getProducts();
        Map<String, Long> counts = new LinkedHashMap<>();
        Map<String, Long> low = new LinkedHashMap<>();
        ProductCategories.STANDARD.forEach(category -> { counts.put(category, 0L); low.put(category, 0L); });
        products.forEach(product -> {
            counts.merge(product.category(), 1L, Long::sum);
            low.putIfAbsent(product.category(), 0L);
            if (ProductService.isLowStock(product)) low.merge(product.category(), 1L, Long::sum);
        });
        return new InventorySummary(products.size(), products.stream().filter(Product::available).count(),
                products.stream().filter(ProductService::isLowStock).count(), products.stream().filter(p -> p.stock() == 0).count(), counts, low);
    }
}
