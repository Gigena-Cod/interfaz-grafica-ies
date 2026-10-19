package com.ies21.gestionproductos.infrastructure.persistence;

import com.ies21.gestionproductos.domain.repository.ProductRepository;
import com.ies21.gestionproductos.domain.model.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class InMemoryProductRepository implements ProductRepository {
    private final List<Product> products = new ArrayList<>();

    public InMemoryProductRepository() {
        add("Lavandina Concentrada 2L", "Lavandinas", "650", 85);
        add("Desinfectante Pino Antibacterial 1L", "Desinfectantes", "920", 8);
        add("Detergente Lavavajillas Limón 750ml", "Detergentes", "480", 120);
        add("Limpiador Multiuso Gatillo 500ml", "Otros", "750", 5);
        add("Cloro Gel Desengrasante 1L", "Lavandinas", "1100", 42);
        add("Desodorante Ambiental Lavanda 400ml", "Otros", "890", 0);
        add("Detergente Polvo Ropa Blanca 3kg", "Detergentes", "2450", 18);
        add("Alcohol Líquido 70% 1000ml", "Desinfectantes", "1350", 64);
    }

    private void add(String name, String category, String price, int stock) {
        products.add(new Product("PRD-%04d".formatted(products.size() + 1), name, category,
                new BigDecimal(price), stock, stock > 0));
    }

    public List<Product> findAll() { return List.copyOf(products); }
    public java.util.Optional<Product> findById(String id) {
        return products.stream().filter(p -> p.id().equals(id)).findFirst();
    }
    public void create(Product product) {
        if (products.stream().anyMatch(p -> p.id().equals(product.id()))) {
            throw new IllegalArgumentException("El ID ya existe");
        }
        products.add(product);
    }
    public void update(Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).id().equals(product.id())) { products.set(i, product); return; }
        }
        throw new IllegalArgumentException("Producto inexistente");
    }
    public void delete(String id) {
        if (!products.removeIf(p -> p.id().equals(id))) throw new IllegalArgumentException("El producto ya no existe.");
    }
}
