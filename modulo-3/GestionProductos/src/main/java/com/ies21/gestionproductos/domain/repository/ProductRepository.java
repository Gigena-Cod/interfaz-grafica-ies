package com.ies21.gestionproductos.domain.repository;

import com.ies21.gestionproductos.domain.model.Product;
import java.util.List;

/** Contrato independiente del motor de almacenamiento. */
public interface ProductRepository {
    /** @return todos los productos actuales del almacenamiento */
    List<Product> findAll();
    /**
     * @param id identificador estable
     * @return producto encontrado, o un resultado vacío si no existe
     */
    java.util.Optional<Product> findById(String id);
    /**
     * @param product producto nuevo, con identificador único
     * @throws IllegalArgumentException si el identificador ya está ocupado
     */
    void create(Product product);
    /**
     * @param product valores completos del producto que conserva su identificador
     * @throws IllegalArgumentException si no existe un producto con ese ID
     */
    void update(Product product);
    /**
     * @param id identificador que se eliminará
     * @throws IllegalArgumentException si el producto no existe
     */
    void delete(String id);
}
