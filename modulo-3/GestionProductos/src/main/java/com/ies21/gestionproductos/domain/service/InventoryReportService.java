package com.ies21.gestionproductos.domain.service;

import com.ies21.gestionproductos.domain.model.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * Genera los datos del reporte de inventario, sin decidir su presentación visual.
 * El reloj se inyecta para controlar la fecha en las pruebas.
 */
public final class InventoryReportService {
    private final ProductService products;
    private final Clock clock;
    /**
     * @param products servicio compartido de consulta de productos
     * @param clock reloj que define la fecha y zona horaria del documento
     */
    public InventoryReportService(ProductService products, Clock clock) {
        this.products = Objects.requireNonNull(products);
        this.clock = Objects.requireNonNull(clock);
    }
    /**
     * Instantánea inmutable de los datos incluidos en un documento.
     * @param products registros que cumplen los filtros
     * @param generatedAt fecha y zona horaria de generación
     * @param category categoría aplicada, o {@code null} para todas
     * @param status filtro de estado aplicado
     * @param available cantidad de productos disponibles dentro del resultado
     * @param lowStock cantidad con menos de cinco unidades dentro del resultado
     * @param value suma del precio multiplicado por el stock de cada resultado
     */
    public record Report(List<Product> products, ZonedDateTime generatedAt, String category, ProductStatus status,
            long available, long lowStock, BigDecimal value) {
        public Report { products = List.copyOf(products); }
        public int total() { return products.size(); }
    }
    /**
     * Consulta los productos filtrados y calcula el reporte sobre esa misma lista.
     * La valuación usa {@link BigDecimal} para conservar la precisión monetaria.
     * @param category categoría exacta o {@code null} para todas
     * @param status filtro de disponibilidad o stock
     * @return datos del reporte; una nueva llamada consulta nuevamente el repositorio
     */
    public Report generateReport(String category, ProductStatus status) {
        var snapshot = products.filterProducts("", category, status);
        var value = snapshot.stream().map(p -> p.price().multiply(BigDecimal.valueOf(p.stock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Report(snapshot, ZonedDateTime.now(clock), category, status,
                snapshot.stream().filter(Product::available).count(),
                snapshot.stream().filter(ProductService::isLowStock).count(), value);
    }
}
