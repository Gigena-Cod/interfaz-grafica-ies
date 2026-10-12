package com.ies21.gestionproductos.domain.service;

import com.ies21.gestionproductos.domain.model.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class InventoryReportService {
    private final ProductService products;
    private final Clock clock;
    public InventoryReportService(ProductService products, Clock clock) {
        this.products = Objects.requireNonNull(products);
        this.clock = Objects.requireNonNull(clock);
    }
    public record Report(List<Product> products, ZonedDateTime generatedAt, String category, ProductStatus status,
            long available, long lowStock, BigDecimal value) {
        public Report { products = List.copyOf(products); }
        public int total() { return products.size(); }
    }
    public Report generateReport(String category, ProductStatus status) {
        var snapshot = products.filterProducts("", category, status);
        var value = snapshot.stream().map(p -> p.price().multiply(BigDecimal.valueOf(p.stock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Report(snapshot, ZonedDateTime.now(clock), category, status,
                snapshot.stream().filter(Product::available).count(),
                snapshot.stream().filter(ProductService::isLowStock).count(), value);
    }
}
