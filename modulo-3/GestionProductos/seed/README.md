# Seed inicial de InventoryManager

El script JavaScript carga 24 productos en `inventory_manager.products`. Node se
usa únicamente para esta herramienta; la aplicación sigue siendo Java Swing.
Estadísticas y reportes se calculan desde products, sin colecciones duplicadas.

## Configuración

Requiere Node 22 o superior. Desde la carpeta GestionProductos:

1. Copiar `.env.example` a `.env.local` y completar MONGODB_URI con la conexión de Atlas.
2. Mantener `MONGODB_DATABASE=inventory_manager` o elegir otra base explícitamente.
3. Instalar y ejecutar:

```powershell
npm --prefix seed ci
npm --prefix seed run check
npm --prefix seed run seed
```

`.env.local` está excluido de Git. También pueden usarse variables de entorno.
Las credenciales nunca van en products.json, package.json ni scripts.

## Comportamiento

- Valida los productos antes de conectarse y escribir.
- Usa `_id` estable y `upsert` con `$setOnInsert`.
- Repetir el seed no duplica productos ni sobrescribe cambios existentes.
- Si se eliminó un producto del catálogo inicial, otra ejecución lo vuelve a agregar.
- Nunca usa drop, deleteMany ni borra productos ajenos al seed.
- Guarda precio como Decimal128 y stock como entero Int32, compatibles con Java.
- Muestra un resumen real por categoría: registros, unidades, disponibles y stock bajo.
- `run check` consulta y verifica la conexión; no modifica datos.
- El seed no se ejecuta al iniciar la aplicación.

## DNS del JDK

Si Node conecta pero Java informa un fallo al resolver TXT/SRV:

```powershell
npm --prefix seed run configure-java
```

El script obtiene los hosts y opciones publicados por Atlas y guarda una URI
equivalente `mongodb://` con TLS en MONGODB_JAVA_URI, dentro de `.env.local`.
No cambia la configuración de red del sistema y no muestra la URI en consola.
Si Atlas cambia sus hosts, volver a ejecutar configure-java.

## Uso desde Java

AppConfig selecciona MongoProductRepository sin fallback a memoria. NetBeans debe
ejecutarse con la carpeta del proyecto como directorio de trabajo para leer `.env.local`.
Las variables de entorno tienen prioridad; MONGODB_JAVA_URI permite seleccionar
explícitamente la conexión equivalente para el JDK.

La implementación usa el [driver oficial Java de MongoDB](https://www.mongodb.com/docs/drivers/java/sync/current/get-started/).
