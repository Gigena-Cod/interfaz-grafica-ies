# InventoryManager

Sistema de escritorio para gestionar un catálogo de productos de limpieza,
desarrollado con **Java Swing, Maven, Apache NetBeans y MongoDB Atlas**.

Permite listar, buscar, agregar, consultar, modificar y eliminar productos, además
de visualizar estadísticas y generar reportes del inventario. Los datos de la
aplicación se guardan en MongoDB y permanecen disponibles al volver a ejecutarla.

## Resumen de funcionamiento

```text
Pantallas Swing
      ↓
Servicios de dominio
      ↓
Contrato ProductRepository
      ↓
MongoProductRepository
      ↓
MongoDB: inventory_manager.products
```

La interfaz utiliza una sola ventana principal (`MainWindow`). Un `CardLayout`
cambia entre cuatro vistas sin abrir otros JFrame. Los formularios de alta,
edición y eliminación utilizan ventanas modales `JDialog`.

Todas las vistas comparten el mismo servicio de productos y el mismo repositorio.
Las estadísticas y los reportes se calculan desde los productos guardados; no
existen colecciones de estadísticas de ejemplo que puedan quedar desactualizadas.

## Qué puede hacer el usuario

| Pantalla | Funcionamiento |
| --- | --- |
| Productos | Muestra el catálogo en una tabla, con indicadores y filtros por categoría y stock. |
| Detalle | Consulta los datos actualizados del producto seleccionado por su ID. |
| Estadísticas | Muestra conteos por categoría, barras verticales, disponibilidad y alertas de reposición. |
| Reportes | Genera un documento de solo lectura con filtros, fecha, productos y valuación. |

### Catálogo y búsqueda

- Buscar por nombre o código, sin distinguir mayúsculas y minúsculas.
- La búsqueda funciona al escribir, con el botón **Buscar** o con Enter en el campo.
- Una búsqueda vacía muestra todos los productos compatibles con los demás filtros.
- **Limpiar** restablece la búsqueda y los filtros.
- Las celdas no se editan directamente. Las columnas se pueden ordenar.
- Para abrir el detalle, seleccionar una fila y usar **Ver detalle**, doble clic o Enter.
- Al volver al catálogo se conserva el panel, la consulta y la selección por ID cuando
  el producto sigue visible. Una categoría que dejó de existir vuelve al filtro general.

### Agregar, editar y eliminar

**Agregar producto** abre un formulario con nombre, categoría, precio, stock y
una casilla de disponibilidad. **Editar producto**, desde el detalle, reutiliza
el mismo formulario con los valores existentes; el identificador permanece estable.

El servicio exige nombre y categoría, precio mayor que cero y stock entero no
negativo. Se eliminan espacios externos del nombre y de la categoría. El formulario
admite coma o punto decimal en el precio, sin separadores de miles.

**Eliminar producto** muestra el nombre y el código y exige escribir exactamente
`ELIMINAR`. Cancelar, Escape o cerrar el modal no guarda ni elimina datos. El aviso
sobre posibles compras es condicional: esta aplicación no gestiona compras ni realiza
eliminaciones en cascada sobre otras colecciones.

### Estadísticas y reportes

- Las barras cuentan **productos registrados**, no unidades de stock.
- Disponible significa que el campo `available` está habilitado; es independiente del stock.
- Stock bajo significa **menos de 5 unidades**, incluyendo productos agotados.
- Agotado significa stock igual a cero.
- La valuación del inventario es la suma de **precio × stock** de los productos incluidos.
- Al entrar a Estadísticas se consulta el resumen actual; **Actualizar estadísticas** lo recarga.
- Al entrar a Reportes se genera una vista previa; sus filtros se aplican con **Generar reporte**.
- Cada reporte incluye fecha de Buenos Aires, total, ID, nombre, categoría, precio,
  stock y disponibilidad. Las tarjetas corresponden a los resultados filtrados.
- El reporte no es editable. No hay exportación PDF/Excel ni integración de impresión.

## Requisitos

- **JDK 26**, definido por `maven.compiler.release` en el pom.
- **Maven**, instalado en PATH o incluido con Apache NetBeans.
- Acceso a **MongoDB Atlas**, con usuario autorizado y la IP del equipo habilitada.
- **Node.js 22 o superior**, únicamente para ejecutar los scripts de seed.
- Un escritorio gráfico disponible para ejecutar Swing y las pruebas de ventanas.

La aplicación es Java Swing; Node no se necesita para su funcionamiento habitual.

## Configuración inicial

Ejecutar los comandos desde la carpeta **GestionProductos**, donde está este README.

1. Copiar `.env.example` como `.env.local`.
2. Completar `MONGODB_URI` con la conexión privada de Atlas.
3. Mantener `MONGODB_DATABASE=inventory_manager` o elegir otra base explícitamente.

```powershell
Copy-Item .env.example .env.local
```

Abrir el archivo y completar sus valores. `.env.local` y `seed/node_modules` están
excluidos de Git. No agregar credenciales a las clases, al README ni al pom.

La configuración acepta estas variables:

| Variable | Uso |
| --- | --- |
| `MONGODB_URI` | Cadena de conexión a MongoDB. |
| `MONGODB_DATABASE` | Base de datos; por defecto `inventory_manager`. |
| `MONGODB_JAVA_URI` | Conexión equivalente opcional cuando el DNS del JDK no resuelve TXT/SRV. |

Las variables de entorno tienen prioridad sobre el archivo local. Para la URI se
consulta primero `MONGODB_JAVA_URI`, luego `MONGODB_URI`, y después los equivalentes
locales en el mismo orden. En NetBeans, usar la raíz del proyecto como directorio
de trabajo para que se encuentre `.env.local`.

## Cargar los productos iniciales

```powershell
npm --prefix seed ci
npm --prefix seed run check
npm --prefix seed run seed
```

El seed usa los **24 productos** definidos en `seed/products.json` y muestra un
resumen real de la colección. Usa IDs estables y `$setOnInsert`: repetirlo no duplica
ni sobrescribe productos existentes. Si se eliminó un producto del catálogo inicial,
volver a ejecutar el seed lo agrega nuevamente.

`run check` comprueba la conexión y consulta el resumen sin escribir. El seed no
borra colecciones ni se ejecuta automáticamente al abrir la aplicación. Las cuatro
categorías iniciales son Detergentes, Desinfectantes, Lavandinas y Otros.

Más detalles: [seed/README.md](seed/README.md).

## Ejecutar la aplicación

### Apache NetBeans

1. Abrir este proyecto como proyecto Maven.
2. Seleccionar un JDK compatible con Java 26.
3. Configurar la conexión local como se explicó arriba.
4. Ejecutar el proyecto con **F6**.

La clase principal es `com.ies21.gestionproductos.GestionProductos`.
`nbactions.xml` fija las acciones de ejecución para que NetBeans no intente abrir
el marcador literal `${packageClassName}`. Si el IDE conserva una acción anterior,
cerrar y volver a abrir el proyecto.

### Terminal

Con Maven en PATH:

```powershell
mvn compile exec:java
```

Ejemplo con las rutas instaladas en el equipo de desarrollo:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-26.0.2.1'
& 'C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd' compile exec:java
```

Para compilar y empaquetar sin iniciar la ventana:

```powershell
mvn package
```

El JAR queda en `target/`; requiere el driver y sus dependencias en el classpath.
No es un JAR autocontenido para ejecutar directamente con doble clic o `java -jar`.

## Arquitectura

```text
src/main/java/com/ies21/gestionproductos/
├── GestionProductos.java
├── domain/
│   ├── model/          Entidades, categorías, filtros y resumen inmutable
│   ├── repository/     Contrato ProductRepository y error de persistencia
│   └── service/        Productos, estadísticas y reportes
└── infrastructure/
    ├── config/         Selección de dependencias, conexión y reloj
    ├── components/     Estilos, campos, tarjetas y modal base
    ├── navigation/     MainWindow, MainShell y ScreenRouter
    ├── features/
    │   ├── products/   Catálogo, detalle y dialogs/
    │   ├── statistics/ Pantalla y gráfico de barras
    │   └── reports/    Pantalla y presentación del documento
    └── persistence/    MongoProductRepository
```

| Clase | Responsabilidad |
| --- | --- |
| `ProductService` | CRUD, validaciones, normalización, búsqueda y filtros. |
| `ProductStatisticsService` | Totales y agrupaciones a partir de una lectura de productos. |
| `InventoryReportService` | Instantánea filtrada, fecha y cálculos del reporte. |
| `ProductRepository` | Declara `findAll`, `findById`, `create`, `update` y `delete`. |
| `MongoProductRepository` | Implementa las operaciones usando el driver oficial de MongoDB. |
| `InventoryReportRenderer` | Convierte los datos del reporte a HTML o texto; escapa valores del usuario. |
| `AppConfig` | Comparte el cliente y servicio, verifica la conexión y libera recursos al salir. |
| `DatabaseConfig` | Lee la configuración privada sin exponer la URI al imprimirla. |
| `MainShell` / `ScreenRouter` | Conservan una instancia de las cuatro vistas y coordinan la navegación. |

El dominio no importa Swing ni el driver. Para usar otra persistencia se implementa
`ProductRepository` y se cambia su selección en `AppConfig`, conservando las pantallas.
`InMemoryProductRepository` está únicamente en `src/test/java`, para pruebas aisladas;
no se incluye en la aplicación ni se utiliza como fallback cuando Mongo falla.

### Recorrido de un cambio

1. El usuario completa el modal.
2. El formulario interpreta precio y stock.
3. `ProductService` normaliza y valida los datos.
4. `ProductRepository` deriva la escritura a `MongoProductRepository`.
5. MongoDB guarda el cambio y la vista se recarga mediante un callback.
6. Al consultar estadísticas o reportes, se leen esos mismos productos actualizados.

## Modelo y persistencia

`Product` es un record inmutable con `id`, `name`, `category`, `price`, `stock` y
`available`. El precio usa `BigDecimal`; una edición genera una nueva instancia
conservando el identificador. Las nuevas altas reciben un UUID.

La colección es `products`. El ID se guarda como `_id` de tipo String, el importe
como BSON `Decimal128`, el stock como entero y la disponibilidad como booleano.
El seed y Java utilizan el mismo esquema. No se guardan estadísticas duplicadas.

La conexión inicial se comprueba fuera del hilo de eventos de Swing. Las consultas
posteriores de las vistas usan el driver síncrono; una red lenta puede demorar una
acción. Hay tiempos máximos de red y mensajes mediante `JOptionPane` ante errores.
La aplicación no cambia silenciosamente a una lista local si falla Atlas.

## Verificaciones

Las verificaciones se encuentran en `src/test/java` y se ejecutan con perfiles Maven.

```powershell
# Negocio, navegación, formularios y flujo completo sin Atlas
mvn -Pverification verify

# CRUD real y vistas contra Atlas; requiere la configuración de conexión
mvn -Pmongo-verification verify

# Compilación limpia y todas las verificaciones
mvn clean -Pverification,mongo-verification verify
```

Un `mvn test` común compila estas clases, pero no ejecuta los programas de verificación;
para eso se usan los perfiles anteriores. Los perfiles de interfaz requieren escritorio.

| Verificación | Cobertura |
| --- | --- |
| `StatisticsReportsCheck` | Búsqueda, limpieza, filtros, estadísticas, fechas, valuación y estados vacíos. |
| `ModalSmokeCheck` | Alta, validación numérica, edición, cancelación y confirmación exacta de eliminación. |
| `IntegratedWorkflowCheck` | Recorrido completo con una ventana principal, sin paneles duplicados y con filtros conservados. |
| `MongoRepositoryCheck` | CRUD persistido, decimales, lectura desde otra conexión, estadísticas, reportes y vistas reales. |

La prueba Mongo crea un producto temporal con ID propio y lo elimina al terminar;
no borra los productos del seed. Genera vistas previas en `target/`.

## Documentación del código

Los servicios y métodos complejos tienen **Javadoc en español**: propósito,
parámetros, resultados, validaciones y errores esperados. Los scripts JavaScript
incluyen comentarios JSDoc en español. Para generar la documentación HTML de Java:

```powershell
mvn javadoc:javadoc
```

La documentación HTML se genera en `target/reports/apidocs/index.html`.
La documentación técnica más detallada está en [ARCHITECTURE.md](ARCHITECTURE.md).

## Problemas frecuentes

| Mensaje o situación | Qué revisar |
| --- | --- |
| Clase principal `${packageClassName}` no encontrada | Reabrir el proyecto y ejecutar con F6; las acciones correctas están en `nbactions.xml`. |
| Falta `MONGODB_URI` | Completar `.env.local` o definir la variable; ejecutar desde la raíz del proyecto. |
| Error TXT/SRV desde Java, aunque Node conecta | Ejecutar `npm --prefix seed run configure-java`; ver explicación debajo. |
| No se puede acceder a MongoDB | Revisar URI, usuario, permisos, conexión a Internet e IP autorizada en Atlas. |
| No aparecen productos | Comprobar base seleccionada, filtros y ejecución del seed. |
| Java no admite release 26 | Configurar `JAVA_HOME` y el JDK del proyecto en NetBeans. |
| Aviso de que no se encontró SLF4J | Es un aviso del registro del driver; no equivale por sí solo a un fallo de conexión. |

`configure-java` consulta los hosts y opciones que publica Atlas y escribe una URI
`mongodb://` equivalente con TLS en `MONGODB_JAVA_URI`, dentro de `.env.local`.
No muestra credenciales ni cambia el DNS del equipo. Si Atlas cambia sus hosts,
volver a ejecutar el comando.

## Edición visual en NetBeans

`MainWindow.java` y `MainWindow.form` permanecen juntos en `infrastructure/navigation`.
El diseñador integra `MainShell` como un componente personalizado; en modo diseño
muestra un contenedor sin conectarse a MongoDB. Los paneles y modales internos se
editan en **Source**, ya que no tienen archivos `.form` propios.

Se conserva Swing y el diseño existente. No se implementan dashboard, compras,
usuarios, facturación, exportaciones ni impresión. La pestaña Design dentro del
IDE no forma parte de las verificaciones automatizadas.
