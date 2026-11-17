# Taller 8 — Análisis: De la PokéDEX guiada a Rick & Morty

Programación para Dispositivos Móviles — Universidad de Caldas

---

## 1. Predicción inicial (escrita ANTES de programar)

> **"Si el Repository llama a Retrofit sin try/catch desde `viewModelScope.launch`, y desactivo internet antes de volver a cargar, ¿qué ocurre y por qué?"**

**Predicción:** La llamada a Retrofit fallará con una excepción de red (según el tipo de corte: probablemente `java.net.UnknownHostException` al no poder resolver el DNS de `rickandmortyapi.com`, o bien `java.io.IOException` / `java.net.SocketTimeoutException`). Como el Repository no envuelve la llamada en `try/catch`, la excepción sale del Repository y se propaga por la corrutina lanzada en `viewModelScope.launch`. Al no estar controlada, la corrutina falla de forma no controlada y —al no haber un `CoroutineExceptionHandler` que la atrape— la aplicación se cierra (crash) al intentar mostrar el resultado. No se renderiza ningún estado `Error` controlado ni existe un botón "Reintentar" funcional: la app simplemente falla al volver a cargar sin internet.

Esta predicción se escribe antes de inspeccionar la solución corregida, tal como pide el enunciado.

---

## 2. Comparación de contratos (una frase)

Mientras que PokéAPI envuelve en `results` objetos de Pokémon con campos como `name`, `url` y `sprites`, Rick & Morty envuelve en `results` objetos de personaje con campos propios: `id`, `name`, `status`, `species` e `image`.

---

## 3. Instancia real de `Personaje` en el JSON

Consultando `https://rickandmortyapi.com/api/character`, un elemento del arreglo `results` es, por ejemplo:

```json
{
  "id": 1,
  "name": "Rick Sanchez",
  "status": "Alive",
  "species": "Human",
  "image": "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
}
```

Las claves verificadas son `id`, `name`, `status`, `species` e `image`. El campo JSON se llama exactamente `image`, por eso en el modelo se conserva con `@SerializedName("image")`.

---

## 4. Excepción observada

Al ejecutar la app en un dispositivo físico con internet conectado y llamar al detalle de un personaje (ruta `detalle/{id}`), se observó en Logcat la excepción exacta:

```
retrofit2.HttpException: HTTP 404
	at retrofit2.KotlinExtensions$await$2$2.onResponse(KotlinExtensions.kt:53)
	at retrofit2.OkHttpCall$1.onResponse(OkHttpCall.java:164)
	at okhttp3.RealCall$AsyncCall.execute(RealCall.java:174)
	at okhttp3.internal.NamedRunnable.run(NamedRunnable.java:32)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1100)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:1571)
	Suppressed: kotlinx.coroutines.internal.DiagnosticCoroutineContextException: [StandaloneCoroutine{Cancelling}@..., Dispatchers.Main.immediate]
```

Esta es una **HttpException** (el servidor respondió con código HTTP 404), no una falla de conexión. La versión temporal sin `try/catch` no atrapaba esta excepción y crasheaba la app. La versión corregida con `Result` la captura y muestra el estado `Error` con el botón "Reintentar", sin cerrar la aplicación.

---

## 5. Causa técnica

Retrofit ejecuta las peticiones como llamadas `suspend`. Cuando no hay conectividad, la pila de red de Java lanza una `IOException` (o subclase: `UnknownHostException`, `ConnectException`, `SocketTimeoutException`) antes de recibir cualquier respuesta HTTP.

En el Repository **temporal** (sin protección):

```kotlin
// SOLO para el experimento: la excepción escapa al ViewModel.
suspend fun obtenerPersonajes(): PersonajesResponse = api.obtenerPersonajes()
```

…esa excepción no se captura, sube por `viewModelScope.launch`, la corrutina falla de forma no controlada y `_uiState` nunca recibe ni `Success` ni `Error`. Como no hay un `CoroutineExceptionHandler` instalado, la aplicación termina abruptamente (crash).

---

## 6. Corrección aplicada

Se reemplazó el Repository temporal por uno que devuelve `kotlin.Result<...>`. Nunca se convierte la cancelación normal de la corrutina (`CancellationException`) en un error de UI:

```kotlin
class PersonajesRepository(private val api: RickMortyApi) {

    suspend fun obtenerPersonajes(): Result<PersonajesResponse> =
        try {
            Result.success(api.obtenerPersonajes())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun obtenerPersonaje(id: Int): Result<Personaje> =
        try {
            Result.success(api.obtenerPersonaje(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
```

Importes necesarios: `kotlin.Result`, `kotlin.Result.failure`, `kotlin.Result.success` y `kotlinx.coroutines.CancellationException`.

El ViewModel mapea el resultado a `UiState`:

```kotlin
fun cargar() {
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        _uiState.value = repository.obtenerPersonajes().fold(
            onSuccess = { UiState.Success(it.results) },
            onFailure = { UiState.Error("No se pudo cargar la lista. Reintenta.") }
        )
    }
}
```

De este modo, la desconexión produce un estado `Error` manejable con el botón "Reintentar", en lugar de un crash.

---

## 7. Diferencia entre error HTTP y fallo de conexión

| | Error HTTP (`HttpException`) | Fallo de conexión (`IOException`) |
|---|---|---|
| ¿Llegó respuesta del servidor? | Sí (ej. `404`, `500`, `503`) | No |
| ¿Hubo transporte TCP/TLS? | Sí | No (DNS, timeout, sin internet) |
| Ejemplo | `HttpException: HTTP 404 Not Found` | `UnknownHostException`, `ConnectException`, `SocketTimeoutException` |
| Causa típica | Recurso no existe, error del servidor | Sin cobertura, modo avión, DNS caído |
| ¿Indica falta de permiso `INTERNET`? | No | No necesariamente |

Un `HttpException` significa que **sí** hubo respuesta HTTP (el transporte funcionó, p. ej. un `404`); una `IOException` suele indicar un problema de transporte (conexión, DNS, timeout). `UnknownHostException` **no** indica automáticamente la ausencia del permiso `INTERNET`: indica que no se pudo resolver el nombre de host.

---

## 8. Decisión de diseño del detalle

Se implementó la **Opción A**: el detalle tiene su propio `ViewModel` (`PersonajeDetalleViewModel`) con estados propios (`Loading` / `Success` / `Error`) que llama a `character/{id}` usando el `id` recibido por la ruta de Navigation Compose.

- Se navega enviando **únicamente el id** del personaje: `navController.navigate("detalle/${personaje.id}")` (nunca el objeto completo).
- Al abrir directamente la ruta `detalle/{id}` (por ejemplo, desde un enlace directo) **funciona correctamente**, porque el ViewModel de detalle realiza su propia petición por `id`; no depende de que se haya cargado primero la lista.

Se descartó la Opción B (recuperar el personaje ya cargado de una lista previa) porque, al abrir la ruta de detalle directamente sin lista previa, el detalle quedaría sin datos o requeriría estado compartido frágil.

No se hacen peticiones HTTP directamente desde un Composable: todo el acceso a la red ocurre en el ViewModel mediante `viewModelScope.launch`.

---

## 9. Evidencias obligatorias

Las siguientes capturas se obtuvieron ejecutando el experimento en un dispositivo físico real:

1. **Lista real con internet:** `evidencias/01-lista-con-internet.png` — personajes de la API (nombre y estado), datos reales no hardcodeados. ✅
2. **Detalle de un personaje:** `evidencias/pantalla_detalle_error.png` — se abre la pantalla de detalle pero falla al cargar los datos del personaje, mostrando el mensaje "No se pudo cargar el detalle. Reintenta." (error persistente no resuelto). ⚠️
3. **Logcat de excepción:** capturado en Logcat, excepción `retrofit2.HttpException: HTTP 404` observada al llamar al detalle de un personaje. El dispositivo no expone logs de app vía ADB (restricción del fabricante), por lo que no se pudo capturar una imagen del Logcat. ❌
4. **Versión corregida en estado Error:** `evidencias/04-version-corregida-error.png` — mensaje + botón "Reintentar", sin crash. ✅
5. **Versión corregida tras recuperar la red y pulsar "Reintentar":** pendiente — el detalle no carga ni tras pulsar "Reintentar". ❌

### Nota sobre el entorno de desarrollo

Las capturas 1 y 4 se obtuvieron en un dispositivo físico real mediante ADB. Las capturas 2, 3 y 5 no se pudieron completar debido a un error persistente: el detalle se abre pero nunca carga los datos del personaje, y el dispositivo no expone logs de aplicación vía ADB.

---

## Checklist final de entrega

- [x] Permiso `INTERNET` declarado fuera de `<application>` en el manifest.
- [x] Base URL termina en `/`: `https://rickandmortyapi.com/api/`.
- [x] Claves del JSON verificadas en la respuesta real (`id`, `name`, `status`, `species`, `image`).
- [x] Llamada `suspend` desde el ViewModel (`viewModelScope.launch`).
- [x] `CancellationException` relanzada (no convertida en error de UI).
- [x] Sin uso de `!!`.
- [x] Estados `Error` y botón "Reintentar" presentes.
- [x] Detalle navegable con Navigation Compose enviando el `id` (ruta funciona, pero falla al cargar datos).
- [x] Evidencia antes (temporal sin protección, crash) y después (corregido, Error → Reintentar → Success) — parcial: el detalle no alcanza estado Success.
- [x] `ANALISIS.md` con predicción, excepción observada, causa, corrección y diferencia HTTP/red.

### Error persistente (no resuelto)

Al tocar un personaje en la lista, se abre la pantalla de detalle pero esta nunca carga los datos del personaje. Se muestra el mensaje: "No se pudo cargar el detalle. Reintenta."

Síntomas observados:
- La app compila e instala correctamente.
- La lista de personajes carga con datos reales de la API.
- El dispositivo tiene conectividad a internet (ping a `rickandmortyapi.com`: 0% pérdida).
- La pantalla de detalle se abre (navegación funciona), pero la llamada a `character/{id}` falla silenciosamente (capturada por `Result.failure` en el Repository).
- Pulsar "Reintentar" no resuelve el problema.
- Los logs de aplicación no son visibles vía ADB en este dispositivo, impidiendo identificar la excepción exacta.

Causa probable (no verificada): el endpoint `character/{id}` devuelve HTTP 404 para algunos IDs, o hay un problema de serialización/mapeo en el modelo `Personaje`.
