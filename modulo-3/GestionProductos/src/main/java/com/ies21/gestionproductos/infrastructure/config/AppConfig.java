package com.ies21.gestionproductos.infrastructure.config;

import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.infrastructure.persistence.InMemoryProductRepository;
import java.time.Clock;
import java.time.ZoneId;

/** Punto de composición: selección del almacenamiento y configuración técnica. */
public final class AppConfig {
    private AppConfig() { }
    public static ProductService createProductService() {
        return new ProductService(new InMemoryProductRepository());
    }
    public static Clock clock() {
        return Clock.system(ZoneId.of("America/Argentina/Buenos_Aires"));
    }
}
