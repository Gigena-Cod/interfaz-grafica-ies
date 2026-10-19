package com.ies21.gestionproductos.infrastructure.persistence;

import com.ies21.gestionproductos.domain.model.Product;
import com.ies21.gestionproductos.domain.repository.*;
import com.ies21.gestionproductos.infrastructure.config.DatabaseConfig;
import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.Decimal128;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import static com.mongodb.client.model.Filters.eq;

/** Adaptador de persistencia. MongoDB es la única fuente de datos de la aplicación. */
public final class MongoProductRepository implements ProductRepository, AutoCloseable {
    private final MongoClient client;
    private final MongoCollection<Document> collection;

    /**
     * Prepara un cliente reutilizable con tiempos máximos de red y selecciona products.
     * La conexión efectiva se comprueba con {@link #ping()}.
     * @param config configuración privada cargada desde entorno o archivo local
     * @throws RepositoryException si la URI o su resolución DNS no permiten crear el cliente
     */
    public MongoProductRepository(DatabaseConfig config) {
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(config.uri()))
                    .applyToClusterSettings(builder -> builder.serverSelectionTimeout(10, TimeUnit.SECONDS))
                    .applyToSocketSettings(builder -> builder.connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS))
                    .build();
            client = MongoClients.create(settings);
            collection = client.getDatabase(config.database()).getCollection("products");
        } catch (RuntimeException ex) {
            String detail = String.valueOf(ex.getMessage()).replace(config.uri(), "<conexión oculta>");
            try {
                String userInfo = java.net.URI.create(config.uri()).getUserInfo();
                if (userInfo != null) for (String part : userInfo.split(":", 2)) if (!part.isEmpty()) detail = detail.replace(part, "***");
            } catch (IllegalArgumentException ignored) { detail = "URI inválida."; }
            throw new RepositoryException("No se pudo configurar MongoDB: " + detail, null);
        }
    }
    /**
     * Verifica acceso al servidor antes de iniciar la interfaz.
     * @throws RepositoryException si MongoDB no responde o rechaza el acceso
     */
    public void ping() {
        execute(() -> client.getDatabase(collection.getNamespace().getDatabaseName()).runCommand(new Document("ping", 1)));
    }
    /**
     * Traduce errores del driver a excepciones comprensibles para la aplicación.
     * @param <T> tipo de resultado de la operación
     * @param operation consulta o escritura que se ejecutará
     * @return resultado producido por la operación
     * @throws IllegalArgumentException ante un identificador duplicado
     * @throws RepositoryException ante otros errores de persistencia o conexión
     */
    private <T> T execute(Supplier<T> operation) {
        try { return operation.get(); }
        catch (MongoWriteException ex) {
            if (ex.getError().getCategory() == ErrorCategory.DUPLICATE_KEY)
                throw new IllegalArgumentException("El identificador del producto ya existe.");
            throw new RepositoryException("MongoDB no pudo guardar la operación. Revisá permisos y datos.", ex);
        } catch (MongoException ex) {
            throw new RepositoryException("No se pudo acceder a MongoDB. Revisá conexión, credenciales y acceso de red en Atlas.", ex);
        }
    }
    /**
     * Lee la colección ordenada por ID y cierra el cursor al terminar o fallar.
     * @return copia inmutable de los productos recuperados
     */
    public List<Product> findAll() {
        return execute(() -> {
            List<Product> products = new ArrayList<>();
            try (MongoCursor<Document> cursor = collection.find().sort(new Document("_id", 1)).iterator()) {
                while (cursor.hasNext()) products.add(fromDocument(cursor.next()));
            }
            return List.copyOf(products);
        });
    }
    /** {@inheritDoc} */
    public Optional<Product> findById(String id) {
        return execute(() -> Optional.ofNullable(collection.find(eq("_id", id)).first()).map(MongoProductRepository::fromDocument));
    }
    /** {@inheritDoc} */
    public void create(Product product) {
        execute(() -> { collection.insertOne(toDocument(product)); return null; });
    }
    /**
     * Reemplaza el documento completo sin crear uno nuevo si el ID no existe.
     * @param product valores validados del producto
     * @throws IllegalArgumentException si el documento ya no existe
     */
    public void update(Product product) {
        execute(() -> {
            if (collection.replaceOne(eq("_id", product.id()), toDocument(product)).getMatchedCount() == 0)
                throw new IllegalArgumentException("El producto ya no existe.");
            return null;
        });
    }
    /** {@inheritDoc} */
    public void delete(String id) {
        execute(() -> {
            if (collection.deleteOne(eq("_id", id)).getDeletedCount() == 0)
                throw new IllegalArgumentException("El producto ya no existe.");
            return null;
        });
    }
    /**
     * Convierte la entidad al esquema BSON: ID String en _id y precio Decimal128.
     * @param product entidad que se persistirá
     * @return documento independiente listo para escritura
     */
    public static Document toDocument(Product product) {
        return new Document("_id", product.id()).append("name", product.name()).append("category", product.category())
                .append("price", new Decimal128(product.price())).append("stock", product.stock()).append("available", product.available());
    }
    /**
     * Reconstruye la entidad y convierte el importe persistido a BigDecimal.
     * Admite Decimal128 y valores numéricos/textuales convertibles para el precio.
     * @param document documento con el esquema de products
     * @return entidad recuperada
     * @throws RepositoryException si un campo no se puede convertir al tipo esperado
     */
    public static Product fromDocument(Document document) {
        try {
            Object storedPrice = document.get("price");
            BigDecimal price = storedPrice instanceof Decimal128 decimal ? decimal.bigDecimalValue() : new BigDecimal(storedPrice.toString());
            return new Product(document.getString("_id"), document.getString("name"), document.getString("category"),
                    price, document.getInteger("stock"), document.getBoolean("available"));
        } catch (RuntimeException ex) {
            throw new RepositoryException("Hay un producto con formato inválido en MongoDB. Revisá el esquema de products.", ex);
        }
    }
    /** Cierra el cliente y sus recursos de red; no elimina documentos. */
    @Override public void close() { client.close(); }
}
