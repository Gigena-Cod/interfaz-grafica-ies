/**
 * @file Carga inicial explícita de productos en MongoDB.
 * Valida el JSON antes de escribir y usa $setOnInsert por ID: repetir la ejecución
 * conserva los registros existentes. --check consulta sin modificar la colección.
 * Las estadísticas se derivan de products y el cliente siempre se cierra en finally.
 */
import { readFile } from 'node:fs/promises';
import { MongoClient, Decimal128, Int32 } from 'mongodb';

const uri = process.env.MONGODB_URI;
const databaseName = process.env.MONGODB_DATABASE || 'inventory_manager';
const checkOnly = process.argv.includes('--check');
if (!uri || !/^[A-Za-z0-9_-]+$/.test(databaseName)) {
  console.error('Configurá MONGODB_URI y un MONGODB_DATABASE válido en el entorno o .env.local.');
  process.exit(1);
}

const client = new MongoClient(uri, { serverSelectionTimeoutMS: 10000, connectTimeoutMS: 5000 });
try {
  const data = JSON.parse(await readFile(new URL('./products.json', import.meta.url), 'utf8'));
  const ids = new Set();
  /**
   * Operaciones de alta idempotentes. Los importes se convierten a Decimal128 y
   * el stock a Int32 para conservar los tipos esperados por la aplicación Java.
   * @type {import('mongodb').AnyBulkWriteOperation<import('mongodb').Document>[]}
   */
  const operations = data.map(product => {
    if (!product.id || ids.has(product.id) || !product.name?.trim() || !product.category?.trim()
        || !/^\d+(\.\d{1,2})?$/.test(product.price) || Number(product.price) <= 0
        || !Number.isSafeInteger(product.stock) || product.stock < 0 || product.stock > 2147483647
        || typeof product.available !== 'boolean') throw new Error('INVALID_SEED');
    ids.add(product.id);
    return {
      updateOne: {
        filter: { _id: product.id },
        update: { $setOnInsert: {
          name: product.name.trim(), category: product.category.trim(),
          price: Decimal128.fromString(product.price), stock: new Int32(product.stock), available: product.available
        } },
        upsert: true
      }
    };
  });
  await client.connect();
  const db = client.db(databaseName);
  await db.command({ ping: 1 });
  const collection = db.collection('products');
  if (!checkOnly) {
    const result = await collection.bulkWrite(operations, { ordered: true });
    console.log(`Seed: ${result.upsertedCount} insertados; ${result.matchedCount} existentes conservados.`);
  }
  const summary = await collection.aggregate([{ $group: {
    _id: '$category', products: { $sum: 1 }, stock: { $sum: '$stock' },
    available: { $sum: { $cond: ['$available', 1, 0] } },
    lowStock: { $sum: { $cond: [{ $lt: ['$stock', 5] }, 1, 0] } }
  } }, { $sort: { _id: 1 } }]).toArray();
  console.log(`MongoDB OK · Base: ${databaseName} · Productos: ${await collection.countDocuments()}`);
  for (const row of summary) console.log(`${row._id}: ${row.products} productos, ${row.stock} unidades, ${row.available} disponibles, ${row.lowStock} con stock bajo`);
} catch (error) {
  // No mostrar mensajes del driver que puedan contener datos de conexión.
  console.error(error.message === 'INVALID_SEED' ? 'Los productos del seed no son válidos.'
    : `No se pudo completar el seed (${error.name}). Revisá Atlas: credenciales, red e IP autorizada.`);
  process.exitCode = 1;
} finally {
  await client.close();
}
