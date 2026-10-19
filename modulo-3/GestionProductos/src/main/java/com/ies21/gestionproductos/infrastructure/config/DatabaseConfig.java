package com.ies21.gestionproductos.infrastructure.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

/**
 * Configuración privada de persistencia; el entorno tiene prioridad sobre .env.local.
 * @param uri cadena de conexión que no debe registrarse ni versionarse
 * @param database nombre de la base de datos que utilizará la aplicación
 */
public record DatabaseConfig(String uri, String database) {
    public DatabaseConfig {
        if (uri == null || uri.isBlank()) throw new IllegalArgumentException("Configurá MONGODB_URI en el entorno o en .env.local.");
        if (database == null || !database.matches("[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException("MONGODB_DATABASE debe contener letras, números, guiones o guiones bajos.");
    }
    /**
     * Carga las variables sin modificar el entorno del equipo. Prioriza
     * MONGODB_JAVA_URI y MONGODB_URI del entorno y después sus equivalentes locales.
     * @return configuración validada, con inventory_manager como base predeterminada
     * @throws IllegalStateException si existe el archivo local pero no puede leerse
     * @throws IllegalArgumentException si falta la URI o el nombre de base es inválido
     */
    public static DatabaseConfig load() {
        Properties local = new Properties();
        Path file = Path.of(".env.local");
        if (Files.exists(file)) {
            try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { local.load(reader); }
            catch (IOException ex) { throw new IllegalStateException("No se pudo leer .env.local.", ex); }
        }
        String uri = System.getenv("MONGODB_JAVA_URI");
        if (uri == null || uri.isBlank()) uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isBlank()) uri = local.getProperty("MONGODB_JAVA_URI", local.getProperty("MONGODB_URI"));
        return new DatabaseConfig(uri, value("MONGODB_DATABASE", local, "inventory_manager"));
    }
    private static String value(String key, Properties local, String fallback) {
        String environment = System.getenv(key);
        return environment == null || environment.isBlank() ? local.getProperty(key, fallback) : environment;
    }
    @Override public String toString() { return "DatabaseConfig[uri=<oculta>, database=" + database + "]"; }
}
