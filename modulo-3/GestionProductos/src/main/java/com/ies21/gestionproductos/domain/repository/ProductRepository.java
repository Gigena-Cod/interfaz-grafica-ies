package com.ies21.gestionproductos.domain.repository;

import com.ies21.gestionproductos.domain.model.Product;
import java.util.List;

/** Contrato independiente del motor de almacenamiento. */
public interface ProductRepository {
    List<Product> findAll();
    java.util.Optional<Product> findById(String id);
    void create(Product product);
    void update(Product product);
    void delete(String id);
}
