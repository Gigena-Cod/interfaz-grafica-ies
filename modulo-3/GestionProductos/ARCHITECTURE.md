# InventoryManager — Arquitectura

Aplicación de escritorio Java Swing / Maven / NetBeans. Se conserva el paquete
raíz `com.ies21.gestionproductos` y la entrada `GestionProductos`, evitando cambiar
la identidad del proyecto o reconstruir componentes funcionales.

## Estructura del código activo

```text
com.ies21.gestionproductos
├── GestionProductos.java
├── domain
│   ├── model
│   │   ├── Product.java
│   │   ├── ProductCategories.java
│   │   ├── ProductStatus.java
│   │   └── InventorySummary.java
│   ├── repository/ProductRepository.java
│   └── service
│       ├── ProductService.java
│       └── InventoryReportService.java
└── infrastructure
    ├── config/AppConfig.java
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
    └── persistence/InMemoryProductRepository.java
```

Pueden permanecer directorios anteriores vacíos; no contienen código ni clases
compilables. La limpieza Maven elimina las clases antiguas de target antes de
compilar y empaquetar.

## Responsabilidades y dependencias

- Product es un record inmutable con BigDecimal. Las modificaciones producen una
  nueva instancia manteniendo el ID; las listas devueltas no permiten mutación.
- ProductRepository define consultar todos, consultar por ID, crear, actualizar y
  eliminar. Solo depende del modelo. El dominio no importa Swing, infraestructura
  ni bibliotecas de bases de datos.
- ProductService concentra CRUD, validación, normalización, búsqueda por nombre o
  ID, categorías, filtros y resumen del inventario. Rechaza nombres/categorías
  vacíos o nulos, precio nulo o no positivo, stock negativo y productos inexistentes.
  Stock bajo significa menos de 5 unidades, incluyendo los agotados.
- ProductCategories define las cuatro categorías estándar una sola vez. También se
  conservan categorías diferentes preexistentes. ProductStatus representa filtros
  de negocio sin depender del texto visible de un botón o combo.
- InventorySummary contiene los conteos y distribuciones inmutables para las vistas.
- InventoryReportService devuelve una instantánea filtrada, fecha, disponibilidad,
  stock bajo y valuación (suma de precio × stock). No genera HTML ni estilos.
- InventoryReportRenderer, dentro de reports, presenta esos datos como HTML o texto.
  Escapa los valores del usuario antes de incorporarlos al HTML local del JEditorPane.
- AppConfig selecciona el repositorio en memoria y el reloj de Buenos Aires.
  Para incorporar otra persistencia, se implementará ProductRepository y se cambiará
  la selección en AppConfig. No hay drivers, conexiones ni configuración de Mongo/MySQL.

## Navegación y datos compartidos

MainWindow es el único JFrame de la aplicación. MainShell recibe un único
ProductService y lo comparte con todas las vistas y sus diálogos. El constructor
sin parámetros que necesita NetBeans obtiene ese servicio desde AppConfig.

ScreenRouter usa CardLayout. MainShell crea exactamente una instancia de cada
vista: catálogo, detalle, estadísticas y reportes. Navegar o realizar CRUD recarga
los datos; no reemplaza el ProductsPanel. El catálogo conserva consulta, filtros,
orden y selección por ID siempre que el producto siga visible. Si una categoría
seleccionada deja de existir, se vuelve al filtro de todas las categorías.

El detalle consulta el ID actual al entrar y al editar. El catálogo nunca utiliza
el índice visible de la fila como identidad: convierte la selección al modelo.
Los resúmenes y reportes se actualizan al entrar a sus vistas o al pulsar actualizar.

Alta y edición mantienen ProductFormDialog con dos modos. No se duplicó código
para crear AddProductDialog y EditProductDialog: los tres flujos (alta, edición y
eliminación) usan dos implementaciones de JDialog a través de BaseModal.
La eliminación exige ELIMINAR exacto, muestra nombre e ID y no se ejecuta al cancelar.
El aviso sobre posibles compras es condicional; la aplicación no gestiona compras.

Cancelar, Escape o cerrar un modal no guarda. Las entradas numéricas se interpretan
en el formulario; las reglas de negocio se validan en el servicio. Los errores se
muestran con JOptionPane y se registran, conservando los datos para su corrección.
Los datos de ejemplo se cargan una sola vez en el repositorio de la sesión.

## NetBeans y ejecución

Abrir el proyecto Maven y ejecutar GestionProductos. MainWindow está ahora en
`infrastructure/navigation`; el .java y el .form se movieron juntos y la referencia
a MainShell en el formulario se actualizó. MainWindow.main delega al mismo arranque
para evitar diferencias al ejecutar el archivo desde NetBeans.

El formulario integra MainShell como componente personalizado. Compilar antes de
abrir Design. Las vistas y modales internos siguen escritos en Source; no se
agregaron .form individuales ni se verificó la apertura visual del diseñador.

## Compilación y verificaciones

Requiere JDK 26, según la configuración existente del proyecto. Maven de NetBeans
está disponible en `C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd`.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-26.0.2.1'
& 'C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd' -B -ntp clean -Pverification verify
```

Con Maven configurado en PATH: `mvn clean -Pverification verify`.
Las verificaciones están en src/test/java. Son programas Java sin dependencias
externas de pruebas y el perfil verification los ejecuta en procesos separados.
Un `mvn test` común los compila; para ejecutarlos debe activarse dicho perfil.
La prueba de modales y el flujo integrado requieren un escritorio disponible.

- StatisticsReportsCheck: búsqueda, limpieza, navegación, límites de stock, conteos,
  recarga tras CRUD, filtros, valuación, fecha local, reportes y estados vacíos.
- ModalSmokeCheck: alta, números inválidos, coma decimal, edición con ID estable,
  cancelación, confirmación exacta y eliminación, selección de una tabla ordenada.
- IntegratedWorkflowCheck: ejecuta los botones reales de MainWindow en la secuencia
  alta → búsqueda → detalle → edición → estadísticas → reporte → eliminación.
  Comprueba una instancia de cada panel, un único JFrame, filtros y selección
  conservados, validaciones nulas/productos inexistentes y escape del HTML.

El pom solo agrega un perfil con exec-maven-plugin para ejecutar estas verificaciones.
No incorpora frameworks ni dependencias de base de datos. Se conservan las vistas,
colores y comportamientos funcionales existentes.

## Resumen de la refactorización

Se movió dominio a domain y la interfaz a infrastructure/features. El contrato de
persistencia quedó en dominio y su implementación en infrastructure/persistence.
MainWindow, MainShell y ScreenRouter pasaron a navigation. Se agregó AppConfig,
InventorySummary, ProductCategories, ProductStatus e InventoryReportRenderer.
Se removieron DatabaseConfig y el paquete marcador de Mongo, ambos sin uso.
Se actualizaron imports, formulario de NetBeans y verificaciones. Se eliminaron
constructores de catálogo con callbacks vacíos y callbacks circulares con arrays.
