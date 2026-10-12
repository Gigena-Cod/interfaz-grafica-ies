package com.ies21.gestionproductos.domain.service;

import com.ies21.gestionproductos.domain.repository.ProductRepository;
import com.ies21.gestionproductos.domain.model.*;
import java.math.BigDecimal;
import java.util.*;

public final class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) { this.repository = Objects.requireNonNull(repository); }
    public List<Product> getProducts() { return repository.findAll(); }
    public static boolean isLowStock(Product product) { return product.stock() < 5; }

    public Product getProduct(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El identificador es obligatorio.");
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("El producto ya no existe."));
    }
    public void createProduct(String name, String category, BigDecimal price, int stock, boolean available) {
        repository.create(validated(new Product(UUID.randomUUID().toString(), name, category, price, stock, available)));
    }
    public void updateProduct(Product product) {
        Product normalized = validated(product);
        getProduct(normalized.id());
        repository.update(normalized);
    }
    public void deleteProduct(String id) { getProduct(id); repository.delete(id); }

    private Product validated(Product product) {
        if (product == null) throw new IllegalArgumentException("El producto es obligatorio.");
        if (product.id() == null || product.id().isBlank()) throw new IllegalArgumentException("El identificador es obligatorio.");
        if (product.name() == null || product.name().isBlank()) throw new IllegalArgumentException("Ingresá el nombre del producto.");
        if (product.category() == null || product.category().isBlank()) throw new IllegalArgumentException("Ingresá una categoría.");
        if (product.price() == null || product.price().signum() <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        if (product.stock() < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
        return new Product(product.id(), product.name().strip(), product.category().strip(), product.price(), product.stock(), product.available());
    }
    public List<Product> searchProducts(String query) { return filterProducts(query, null, ProductStatus.ALL); }
    public List<Product> getProductsByCategory(String category) { return filterProducts("", category, ProductStatus.ALL); }
    public int getTotalProducts() { return getProducts().size(); }
    public List<Product> getLowStockProducts() { return filterProducts("", null, ProductStatus.LOW_STOCK); }
    public List<Product> filterProducts(String query, String category, ProductStatus status) {
        String normalized = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        Objects.requireNonNull(status, "status");
        return getProducts().stream()
                .filter(p -> (p.name() + " " + p.id()).toLowerCase(Locale.ROOT).contains(normalized))
                .filter(p -> category == null || p.category().equals(category))
                .filter(p -> switch (status) {
                    case ALL -> true;
                    case AVAILABLE -> p.available();
                    case UNAVAILABLE -> !p.available();
                    case LOW_STOCK -> isLowStock(p);
                    case OUT_OF_STOCK -> p.stock() == 0;
                }).toList();
    }
    public List<String> getCategories() {
        return getProducts().stream().map(Product::category).distinct().sorted().toList();
    }
    public InventorySummary getSummary() {
        var products = getProducts();
        Map<String, Long> counts = new LinkedHashMap<>();
        Map<String, Long> low = new LinkedHashMap<>();
        ProductCategories.STANDARD.forEach(category -> { counts.put(category, 0L); low.put(category, 0L); });
        products.forEach(product -> {
            counts.merge(product.category(), 1L, Long::sum);
            low.putIfAbsent(product.category(), 0L);
            if (isLowStock(product)) low.merge(product.category(), 1L, Long::sum);
        });
        return new InventorySummary(products.size(), products.stream().filter(Product::available).count(),
                products.stream().filter(ProductService::isLowStock).count(), products.stream().filter(p -> p.stock() == 0).count(), counts, low);
    }
}
