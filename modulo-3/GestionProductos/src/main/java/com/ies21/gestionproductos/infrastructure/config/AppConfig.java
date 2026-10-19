package com.ies21.gestionproductos.infrastructure.config;

import com.ies21.gestionproductos.domain.service.ProductService;
import com.ies21.gestionproductos.infrastructure.persistence.MongoProductRepository;
import java.time.Clock;
import java.time.ZoneId;

/** Punto de composición: selección del almacenamiento y configuración técnica. */
public final class AppConfig {
    private AppConfig() { }
    private static MongoProductRepository repository;
    private static ProductService products;

    /**
     * Crea y verifica MongoDB una sola vez y comparte el servicio durante la ejecución.
     * Si el ping falla, cierra el cliente candidato y propaga el error, sin fallback.
     * @return servicio de productos respaldado por MongoDB
     */
    public static synchronized ProductService createProductService() {
        if (products == null) {
            MongoProductRepository candidate = new MongoProductRepository(DatabaseConfig.load());
            try { candidate.ping(); }
            catch (RuntimeException ex) { candidate.close(); throw ex; }
            repository = candidate;
            products = new ProductService(repository);
            Runtime.getRuntime().addShutdownHook(new Thread(AppConfig::close, "mongo-close"));
        }
        return products;
    }
    /** Libera la conexión compartida; puede llamarse aunque ya haya sido cerrada. */
    public static synchronized void close() {
        if (repository != null) {
            repository.close();
            repository = null;
            products = null;
        }
    }
    /** @return reloj del sistema en la zona horaria de Buenos Aires */
    public static Clock clock() {
        return Clock.system(ZoneId.of("America/Argentina/Buenos_Aires"));
    }
}
