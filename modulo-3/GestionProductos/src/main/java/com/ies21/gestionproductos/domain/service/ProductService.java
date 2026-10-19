package com.ies21.gestionproductos.domain.service;

import com.ies21.gestionproductos.domain.repository.ProductRepository;
import com.ies21.gestionproductos.domain.model.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Coordina los casos de uso de productos y sus validaciones de negocio.
 * Depende del contrato del repositorio, sin conocer MongoDB ni los controles Swing.
 * No almacena una lista propia: cada consulta obtiene los datos del repositorio.
 */
public final class ProductService {
    private final ProductRepository repository;
    /**
     * Construye el servicio con la persistencia elegida por la aplicación.
     * @param repository repositorio compartido; no admite {@code null}
     */
    public ProductService(ProductRepository repository) { this.repository = Objects.requireNonNull(repository); }
    /** @return productos actuales almacenados en el repositorio */
    public List<Product> getProducts() { return repository.findAll(); }
    /**
     * Aplica el mismo umbral en catálogo, estadísticas y reportes.
     * @param product producto que se evalúa
     * @return {@code true} si tiene menos de cinco unidades, incluyendo cero
     */
    public static boolean isLowStock(Product product) { return product.stock() < 5; }

    /**
     * Consulta un producto por su identidad estable, no por una fila de la tabla.
     * @param id identificador del producto
     * @return producto actualizado desde el repositorio
     * @throws IllegalArgumentException si el identificador está vacío o no existe
     */
    public Product getProduct(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El identificador es obligatorio.");
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("El producto ya no existe."));
    }
    /**
     * Valida el alta, elimina espacios externos y asigna un UUID antes de persistir.
     * @param name nombre obligatorio
     * @param category categoría obligatoria
     * @param price precio mayor que cero, expresado en pesos argentinos
     * @param stock cantidad entera no negativa
     * @param available indica si el producto está habilitado para la venta
     * @throws IllegalArgumentException si algún dato incumple las reglas de negocio
     */
    public void createProduct(String name, String category, BigDecimal price, int stock, boolean available) {
        repository.create(validated(new Product(UUID.randomUUID().toString(), name, category, price, stock, available)));
    }
    /**
     * Valida y reemplaza los datos del producto identificado por su ID.
     * @param product nuevos valores con el mismo identificador del producto editado
     * @throws IllegalArgumentException si los datos son inválidos o el producto no existe
     */
    public void updateProduct(Product product) {
        Product normalized = validated(product);
        getProduct(normalized.id());
        repository.update(normalized);
    }
    /**
     * Elimina un producto existente. La confirmación corresponde al diálogo de la vista.
     * @param id identificador que se eliminará
     * @throws IllegalArgumentException si el identificador es inválido o no existe
     */
    public void deleteProduct(String id) { getProduct(id); repository.delete(id); }

    /**
     * Centraliza las reglas compartidas por alta y edición sin mutar el objeto recibido.
     * @param product valores que se desean persistir
     * @return copia validada con nombre y categoría normalizados
     * @throws IllegalArgumentException si falta un campo obligatorio, el precio no es
     *         positivo o el stock es negativo
     */
    private Product validated(Product product) {
        if (product == null) throw new IllegalArgumentException("El producto es obligatorio.");
        if (product.id() == null || product.id().isBlank()) throw new IllegalArgumentException("El identificador es obligatorio.");
        if (product.name() == null || product.name().isBlank()) throw new IllegalArgumentException("Ingresá el nombre del producto.");
        if (product.category() == null || product.category().isBlank()) throw new IllegalArgumentException("Ingresá una categoría.");
        if (product.price() == null || product.price().signum() <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        if (product.stock() < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
        return new Product(product.id(), product.name().strip(), product.category().strip(), product.price(), product.stock(), product.available());
    }
    /**
     * Busca por nombre o ID ignorando mayúsculas y espacios externos.
     * @param query texto buscado; vacío o nulo equivale a no restringir la búsqueda
     * @return coincidencias del repositorio actual
     */
    public List<Product> searchProducts(String query) { return filterProducts(query, null, ProductStatus.ALL); }
    /**
     * @param category categoría exacta; {@code null} permite todas
     * @return productos de la categoría indicada
     */
    public List<Product> getProductsByCategory(String category) { return filterProducts("", category, ProductStatus.ALL); }
    /** @return cantidad de registros, no la suma de unidades en stock */
    public int getTotalProducts() { return getProducts().size(); }
    /** @return productos con menos de cinco unidades, incluyendo agotados */
    public List<Product> getLowStockProducts() { return filterProducts("", null, ProductStatus.LOW_STOCK); }
    /**
     * Combina los filtros mediante una condición lógica AND sobre una lectura del repositorio.
     * El filtrado se realiza en Java; la fuente de la lectura sigue siendo MongoDB.
     * @param query búsqueda parcial por nombre o ID; admite vacío o nulo
     * @param category categoría exacta o {@code null} para todas
     * @param status estado de disponibilidad o stock; no admite {@code null}
     * @return lista inmutable con los productos que cumplen todas las condiciones
     */
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
    /** @return categorías presentes en el repositorio, sin duplicados y ordenadas */
    public List<String> getCategories() {
        return getProducts().stream().map(Product::category).distinct().sorted().toList();
    }
}
