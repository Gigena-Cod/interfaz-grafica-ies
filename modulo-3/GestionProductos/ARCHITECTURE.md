# InventoryManager — Arquitectura y ejecución

Aplicación de escritorio Java Swing / Maven / NetBeans. Paquete raíz:
`com.ies21.gestionproductos`. La entrada sigue siendo `GestionProductos`.

## Organización

```text
GestionProductos.java
├── domain
│   ├── model
│   │   ├── Product.java
│   │   ├── ProductCategories.java
│   │   ├── ProductStatus.java
│   │   └── InventorySummary.java
│   ├── repository
│   │   ├── ProductRepository.java
│   │   └── RepositoryException.java
│   └── service
│       ├── ProductService.java
│       ├── ProductStatisticsService.java
│       └── InventoryReportService.java
└── infrastructure
    ├── config
    │   ├── AppConfig.java
    │   └── DatabaseConfig.java
    ├── components
    │   ├── Ui.java
    │   ├── FormField.java
    │   ├── StatCard.java
    │   └── BaseModal.java
    ├── navigation
    │   ├── MainWindow.java / MainWindow.form
    │   ├── MainShell.java
    │   └── ScreenRouter.java
    ├── features
    │   ├── products
    │   │   ├── ProductsPanel.java
    │   │   ├── ProductDetailPanel.java
    │   │   └── dialogs
    │   │       ├── ProductFormDialog.java
    │   │       └── DeleteProductDialog.java
    │   ├── statistics
    │   │   ├── StatisticsPanel.java
    │   │   └── CategoryBarChart.java
    │   └── reports
    │       ├── ReportsPanel.java
    │       └── InventoryReportRenderer.java
    └── persistence
        └── MongoProductRepository.java
```

## Servicios y patrón repositorio

- ProductRepository declara findAll, findById, create, update y delete. El contrato
  no depende del driver ni de Swing.
- ProductService centraliza CRUD, validaciones, normalización, búsqueda y filtros.
- ProductStatisticsService calcula conteos, categorías, disponibles y stock bajo.
  Obtiene los productos a través de ProductService, sin una lista propia.
- InventoryReportService genera una instantánea con filtros, fecha y valuación.
  InventoryReportRenderer presenta sus datos como HTML o texto dentro de Swing.
- MongoProductRepository implementa el contrato con el driver oficial síncrono.
  Cada consulta lee MongoDB; no hay almacenamiento ni fallback en memoria en producción.
- AppConfig comparte el servicio y cliente Mongo durante la ejecución. Cierra el
  cliente al terminar. La conexión inicial se verifica antes de crear la interfaz.
- InMemoryProductRepository se movió a src/test/java y se usa solo en verificaciones.

Product es inmutable y usa BigDecimal. Mongo guarda price como Decimal128 y stock
como entero. El ID público se almacena como _id de tipo String. Los IDs de nuevas
altas son UUID y permanecen estables al editar. Las operaciones comprueban el
resultado: modificar/eliminar un producto inexistente produce un error comprensible.
Las listas y los resúmenes devueltos son inmutables.

Stock bajo significa menos de 5 unidades, incluyendo agotados. La valuación es la
suma de precio × stock. Disponibilidad es un campo independiente editable.
No hay una colección de estadísticas: se calculan desde products para reflejar CRUD.

## Conexión local

1. Copiar .env.example como .env.local y completar MONGODB_URI.
2. Usar MONGODB_DATABASE=inventory_manager (o elegir otra base explícitamente).
3. Autorizar la IP y el usuario de base de datos en MongoDB Atlas.

.env.local está excluido de Git; las credenciales no están en las clases ni en el pom.
El entorno tiene prioridad. DatabaseConfig acepta MONGODB_JAVA_URI como alternativa
para un JDK que no pueda resolver TXT/SRV. Entre variables de entorno se prioriza
MONGODB_JAVA_URI y luego MONGODB_URI; después se consultan los valores locales.
La URI nunca se muestra al imprimir la configuración.

En este equipo el seed conecta con mongodb+srv, pero el resolver DNS del JDK falló
al consultar TXT. Se generó localmente una conexión equivalente mongodb:// con TLS:

```powershell
npm --prefix seed run configure-java
```

El script usa los hosts y opciones publicados por Atlas y escribe únicamente
MONGODB_JAVA_URI en .env.local. No cambia la configuración de red del equipo.
Si cambian los hosts del clúster, volver a ejecutarlo.

## Seed JavaScript explícito

Consultar seed/README.md. Requiere Node >=22; no forma parte de la aplicación Swing.

```powershell
npm --prefix seed ci
npm --prefix seed run check
npm --prefix seed run seed
```

seed/products.json contiene 24 productos en cuatro categorías. index.mjs valida los
datos y utiliza upsert con $setOnInsert por _id. Repetirlo no duplica ni sobrescribe
productos existentes. Si falta un producto inicial, se vuelve a insertar. No elimina
colecciones ni registros ajenos. Muestra el resumen real consultado en MongoDB.
La aplicación nunca ejecuta el seed automáticamente.

## Ejecución Java / NetBeans

Requiere JDK 26 (configuración existente). Desde GestionProductos:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-26.0.2.1'
& 'C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd' compile exec:java
```

Con Maven en PATH: `mvn compile exec:java`. En NetBeans, ejecutar GestionProductos
con la raíz del proyecto como directorio de trabajo para leer .env.local.

MainWindow es el único JFrame principal. CardLayout mantiene una instancia de
catálogo, detalle, estadísticas y reportes. Todos comparten el mismo servicio de
productos; los modales son JDialog. El catálogo conserva filtros y selección por ID.
Las vistas consultan datos al entrar/actualizar. Alta/edición reutilizan el mismo
formulario; eliminar exige ELIMINAR exacto. Cancelar no guarda cambios.
Los errores de configuración, red o persistencia se muestran mediante JOptionPane.
El driver síncrono usa tiempos máximos de conexión/selección de servidor/lectura.
La conexión inicial ocurre fuera del hilo de eventos; las consultas de las vistas
siguen siendo síncronas, por lo que una red lenta puede demorar una acción.

MainWindow.java y .form permanecen juntos en infrastructure/navigation. El formulario
integra MainShell como componente personalizado y conserva la creación con el servicio
inyectado. En Beans.isDesignTime, MainShell muestra un contenedor de diseño sin
conectarse a MongoDB. Las pantallas y modales internos siguen editándose en Source.
No se verificó la pestaña Design dentro del IDE.

## Verificaciones

Pruebas de interfaz y negocio, sin Atlas:

```powershell
mvn -Pverification verify
```

Prueba de persistencia real, requiere configuración de Atlas:

```powershell
mvn -Pmongo-verification verify
```

Todo, incluyendo limpieza previa:

```powershell
mvn clean -Pverification,mongo-verification verify
```

Las clases de src/test/java se ejecutan mediante perfiles de exec-maven-plugin,
sin frameworks adicionales de pruebas. Un mvn test común solo las compila.
Los perfiles que comprueban ventanas y modales requieren un escritorio disponible.

- StatisticsReportsCheck: búsqueda, filtros, navegación, estadísticas, reportes y vacíos.
- ModalSmokeCheck: validaciones, coma decimal, edición, ID estable, cancelación y eliminación.
- IntegratedWorkflowCheck: flujo completo sobre MainWindow con repositorio aislado de prueba.
- MongoRepositoryCheck: ping real, alta temporal, Decimal128, búsqueda, lectura desde
  otro cliente, edición, estadísticas, reporte y eliminación. Finalmente limpia solo
  su propio producto; no borra el seed. También verifica las vistas contra Atlas y
  genera target/statistics-mongo-preview.png y target/reports-mongo-preview.png.

Se comprobó el seed dos veces: 24 insertados la primera vez y cero nuevos la segunda.
Se verificaron operaciones reales desde Java y persistencia entre conexiones.
No se agregaron compras, autenticación, exportaciones ni impresión.
