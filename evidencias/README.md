# Evidencias — Taller 8 (Rick & Morty)

## Capturas obtenidas en dispositivo físico (vía ADB)

| Archivo | Descripción | Estado |
|---------|-------------|--------|
| `01-lista-con-internet.png` | Lista real con personajes de la API (nombre y estado) | ✅ Capturada |
| `04-version-corregida-error.png` | Versión corregida en estado Error (sin crash, con botón Reintentar) | ✅ Capturada |
| `pantalla_detalle_error.png` | Pantalla de detalle que falla al cargar (mensaje: "No se pudo cargar el detalle. Reintenta.") | ⚠️ Capturada con error |

## Capturas NO obtenidas (error persistente no resuelto)

| Archivo | Descripción | Estado | Motivo |
|---------|-------------|--------|--------|
| `02-detalle-personaje.png` | Detalle de un personaje al tocar su fila | ❌ No capturada | El detalle se abre pero nunca carga los datos del personaje (error persistente, sin logs visibles por restricción del fabricante del dispositivo) |
| `03-logcat-excepcion-temporal.png` | Logcat de la versión SIN try/catch con excepción exacta | ❌ No capturada | No se pudo reproducir: el dispositivo no expone logs de app vía ADB |
| `05-despues-de-reintentar.png` | Red restaurada, se pulsó "Reintentar" y vuelve a Success | ❌ No capturada | El detalle no carga ni tras pulsar "Reintentar" |

## Error persistente documentado

- La app compila correctamente (`BUILD SUCCESSFUL`).
- El APK se instala en el dispositivo (`149265557N001560`).
- La lista de personajes carga correctamente con internet (personajes reales de la API).
- **Al tocar un personaje, se abre la pantalla de detalle pero falla al cargar los datos**, mostrando el mensaje: "No se pudo cargar el detalle. Reintenta."
- El dispositivo tiene conectividad a internet (ping a `rickandmortyapi.com`: 0% pérdida, ~16ms).
- Los logs de aplicación no son visibles vía ADB en este dispositivo (restricción del fabricante), por lo que no se pudo identificar la excepción exacta (HttpException, IOException, etc.).
- Se intentó pulsar "Reintentar" automáticamente, pero el detalle sigue fallando.

## Excepción observada (versión corregida, detalle)

```
retrofit2.HttpException: HTTP 404
	at retrofit2.KotlinExtensions$await$2$2.onResponse(KotlinExtensions.kt:53)
```

Este es un **error HTTP** (el servidor respondió con código 404), no una falla de conexión. La versión corregida lo captura y muestra el estado Error con el botón "Reintentar", sin cerrar la aplicación.

## Cómo capturar las evidencias faltantes (si se resuelve el error del detalle)

1. **Detalle (02):** Con internet conectado, abre la app y toca un personaje. Si carga, captura.
2. **Logcat excepción temporal (03):**
   - Vuelve temporalmente al Repository sin try/catch
   - Recompila e instala
   - Con internet, abre la app
   - Corta internet (Modo Avión)
   - Pulsa recarga → la app debería crashear
   - Ejecuta: `adb logcat -d > logcat.txt` y busca `AndroidRuntime` o `Exception`
   - Restaura la versión corregida
3. **Reintentar éxito (05):**
   - Con internet, ve a la pantalla de Error
   - Pulsa "Reintentar"
   - Captura cuando cargue el detalle

## Archivos del proyecto

- `app/build/outputs/apk/debug/app-debug.apk` — APK instalable
- `ANALISIS.md` — Análisis completo con predicción, causa, corrección
- Código fuente en `app/src/main/java/com/example/taller_8/`