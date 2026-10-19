/**
 * @file Prepara una URI equivalente para un JDK cuyo DNS no resuelva TXT/SRV.
 * Consulta los hosts publicados por Atlas y conserva sus opciones junto con TLS.
 * Escribe solo MONGODB_JAVA_URI en .env.local, sin mostrar credenciales en consola.
 * No cambia la configuración de red del sistema ni los documentos de MongoDB.
 */
import { resolveSrv, resolveTxt } from 'node:dns/promises';
import { readFile, writeFile } from 'node:fs/promises';

// Alternativa oficial mongodb:// para equipos donde el DNS del JDK no resuelve SRV/TXT.
// Solo escribe la configuración privada; no modifica el sistema ni muestra la URI.
try {
  const uri = process.env.MONGODB_URI;
  if (!uri?.startsWith('mongodb+srv://')) throw new Error('SRV_REQUIRED');
  const url = new URL(uri);
  const servers = await resolveSrv(`_mongodb._tcp.${url.hostname}`);
  const txt = await resolveTxt(url.hostname);
  const options = new URLSearchParams(txt.flat().join(''));
  for (const [key, value] of url.searchParams) options.set(key, value);
  options.set('tls', 'true');
  const hosts = servers.sort((a, b) => a.name.localeCompare(b.name)).map(s => `${s.name}:${s.port}`).join(',');
  const direct = `mongodb://${url.username}:${url.password}@${hosts}${url.pathname || '/'}?${options}`;
  const file = new URL('../.env.local', import.meta.url);
  const local = await readFile(file, 'utf8');
  const clean = local.split(/\r?\n/).filter(line => !line.startsWith('MONGODB_JAVA_URI=')).join('\n').trimEnd();
  await writeFile(file, `${clean}\nMONGODB_JAVA_URI=${direct}\n`, { mode: 0o600 });
  console.log('Conexión equivalente para Java preparada en .env.local (URI oculta).');
} catch (error) {
  console.error(`No se pudo preparar la conexión Java (${error.name}). Revisá DNS y MONGODB_URI.`);
  process.exitCode = 1;
}
