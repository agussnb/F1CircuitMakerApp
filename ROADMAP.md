# 🏎️ CircuitMakerF1 — Roadmap

> App Android (Kotlin + Jetpack Compose) para crear circuitos estilo F1 grabando un recorrido con GPS, calcular sus 3 sectores y la cantidad de vueltas según la regla FIA, y hacer vueltas rápidas cronometradas por sector.
> Enfoque inicial: ciclismo. A futuro: atletismo.

---

# 🏁 ESTADO ACTUAL

- [x] Fase 1 — Fundamentos de Compose
- [x] Fase 2 — Estado y recomposición
- [x] Fase 3 — Navegación básica
- [x] Fase 4 — Persistencia con Room
- [x] Fase 0 — Git y limpieza
- [x] Fase 5 — Arquitectura (hexagonal liviana)
- [x] Fase 6 — GPS confiable (falta probar en la calle con la pantalla apagada)
- [x] Fase 7 — Crear circuito
- [x] Fase 8 — Modo vuelta rápida
- [ ] Fase 9 — UX real (hecha en buena parte)
- [ ] Fase 10 — Estadísticas y opciones (hecha en parte)

#### Estás en etapa: **demo v0.1 lista para testear** (ver "Cómo probar la demo" al final).

---

## 🧹 FASE 0 — Git y limpieza

### Objetivo: tener el proyecto versionado y sin bugs conocidos antes de construir encima.

#### TODO 1 — Git

- [x] Subir el proyecto a GitHub
- [x] Revisar que `.gitignore` excluya `build/`, `.gradle/` y `local.properties`
- [x] Agregar este roadmap en la raíz del repo
- [x] Definir convención de commits (`feat:`, `fix:`, `refactor:`, `docs:`)

#### TODO 2 — Bugs actuales

- [x] Arreglar la lógica invertida de permisos en `RecordingCircuitScreen`
- [x] Exigir `ACCESS_FINE_LOCATION` (la ubicación aproximada no sirve para circuitos)
- [x] Corregir la fórmula de vueltas: `floor(305 / km) + 1`
- [x] Sacar la inserción de Monza en `MainActivity` (se duplica en cada arranque)
- [x] Sacar el botón que agrega Spa hardcodeado
- [x] Navegar al detalle por `id` en vez de por `name`
- [x] Validar que el nombre no esté vacío antes de guardar

#### TODO 3 — Identidad

- [x] Cambiar el package `com.example` por uno propio (Play Store rechaza `com.example`)

---

## 🏗️ FASE 5 — Arquitectura (hexagonal liviana)

#### TODO 4 — Estructura

- [x] Crear paquetes `domain`, `data`, `ui`, `di`
- [x] Crear `TrackMakerApp : Application` y un `AppContainer` (inyección manual)
- [x] Mover la creación de la base de datos al `AppContainer` (una sola instancia)

#### TODO 5 — Dominio, puertos y adaptadores

- [x] Modelo de dominio `Track` en Kotlin puro (sin anotaciones de Room)
- [x] `TrackEntity` en `data` + mappers `toDomain()` / `toEntity()`
- [x] Interfaz `TrackRepository` en `domain` (puerto)
- [x] `RoomTrackRepository` en `data` (adaptador)
- [x] Interfaz `LocationProvider` que exponga un `Flow<GpsPoint>`
- [x] `FusedLocationProvider` como implementación real (reemplaza a `GPSManager`, ya borrado)

#### TODO 6 — ViewModels

- [x] `HomeViewModel` (hoy `TrackListViewModel`), `DetailViewModel`, `RecordingViewModel`
- [x] Exponer el estado de cada pantalla con `StateFlow<UiState>`
- [x] Mover el formateo de `Track` a la UI
- [x] Sacar `rememberCoroutineScope` de `AppNavigation` (el borrado va al ViewModel)

#### TODO 7 — Tests

- [x] Primer test unitario de dominio (calculadora de vueltas FIA)

---

## 📍 FASE 6 — GPS confiable

#### TODO 8 — Mapa y datos en pantalla

- [x] Google Maps Compose con el trazado en vivo
- [x] Mostrar la precisión del GPS en vivo (con color según la calidad)
- [x] Descartar puntos con `accuracy` mayor a 20 m (`TrackRecorder.withPoint`, con tests)
- [x] Ignorar movimientos menores a 3 m (evita sumar distancia estando quieto)

#### TODO 9 — Foreground Service

- [x] Crear `RecordingService` con notificación persistente
- [x] Declarar `foregroundServiceType="location"` en el Manifest (Android 14+)
- [x] Pedir permiso `POST_NOTIFICATIONS` (Android 13+)
- [ ] Verificar en un teléfono que la grabación sobrevive a rotar, minimizar y apagar la pantalla

#### TODO 10 — Persistir la trayectoria

- [x] Entidad `TrackPointEntity(trackId, position, lat, lng)` con clave foránea a `track`
- [x] Guardar los puntos junto con el circuito en una transacción (`withTransaction`)
- [x] Campo `sportType` en `Track` (ciclismo por defecto)
- [x] Migración de Room a versión 2 (`MIGRATION_1_2`, esquema exportado en `app/schemas`)
- [ ] Test instrumentado de la migración con `MigrationTestHelper`

#### TODO 11 — GPS simulado

- [x] `SimulatedLocationProvider`: opción "GPS simulado" en Opciones
- [x] Al grabar: recorre un circuito de demo; en vuelta rápida: da vueltas sobre el circuito elegido
- [x] `FakeLocationProvider` para los tests del dominio

---

## 🗺️ FASE 7 — Crear circuito

#### TODO 12 — Menú principal

- [x] Pantalla menú: Mis circuitos / Crear circuito / Opciones
- [x] Pantalla Opciones (GPS simulado, distancia de carrera, deporte)

#### TODO 13 — Mapa

- [x] Dibujar el trazado en vivo mientras grabás
- [x] Mostrar el circuito en la pantalla de detalle (con sectores y meta)

#### TODO 14 — Cierre y limpieza del trazado

- [x] Detectar el cierre: volver a menos de 25 m del inicio tras recorrer 200 m y alejarse 50 m
- [x] Avisar "Circuito cerrado" y ofrecer terminar la grabación
- [x] Simplificar la línea con Douglas-Peucker (`simplifyPath`, tolerancia 2 m)

#### TODO 15 — Cálculos

- [x] `buildTrackLayout`: dividir el circuito en 3 sectores por distancia
- [x] Línea de meta y límites de sector como punto + dirección (se recalculan desde el trazado)
- [x] `fiaLapCount`: `floor(305 / km) + 1`
- [x] Tests unitarios

---

## ⏱️ FASE 8 — Modo vuelta rápida

#### TODO 16 — Modelos

- [x] `Lap`, `TimingColor` (dominio) + `SessionEntity`, `LapEntity` (Room)

#### TODO 17 — Detección de cruces

- [x] Detectar el cruce de meta (solo en sentido de carrera, dentro del ancho de la línea)
- [x] Detectar el cruce de cada límite de sector
- [x] Interpolar el tiempo exacto del cruce entre dos lecturas GPS
- [x] Tests con recorridos simulados (`LapTimingEngineTest`)

#### TODO 18 — Cronómetro

- [x] Pantalla de sesión: tiempo de vuelta en vivo y sector actual
- [x] Mostrar el tiempo de cada sector al cruzarlo
- [x] Colores: 🟣 mejor histórico, 🟢 mejor de la sesión, 🟡 más lento
- [x] Lista de vueltas de la sesión
- [x] Delta contra tu mejor vuelta

---

## 🎨 FASE 9 — UX real

- [x] LazyColumn en vez de Column (lista de circuitos y de vueltas)
- [x] Cards para cada circuito
- [x] TopAppBar
- [x] FloatingActionButton para crear circuito
- [x] Estados vacíos ("Todavía no creaste circuitos")
- [x] Loading states
- [x] Diálogo de confirmación antes de borrar
- [x] Tema visual estilo F1 (oscuro, colores de sectores y de cronometraje)
- [ ] Textos a `strings.xml`
- [ ] Grabación con mapa a pantalla completa y controles encima
- [ ] Aviso en el menú si hay una grabación o sesión en curso

---

## 📊 FASE 10 — Estadísticas y opciones

- [ ] Historial de sesiones por circuito
- [x] Mejor vuelta y mejores sectores
- [x] Vuelta ideal (suma de tus mejores sectores)
- [x] Opción: distancia objetivo para calcular vueltas (además de los 305 km FIA)
- [x] Opción: deporte (ciclismo / atletismo)
- [ ] Exportar circuito en GPX

---

## 🔮 FUTURO

- [ ] Soporte real para atletismo (velocidades y filtros propios)
- [ ] Mover sectores a mano
- [ ] Compartir circuitos
- [ ] Hilt para inyección de dependencias
- [ ] GitHub Actions: build y tests en cada push
- [ ] Publicar en Play Store

---

## 🧪 Cómo probar la demo

**Sin moverte (emulador o teléfono):**
1. Opciones → activar **GPS simulado**.
2. Crear circuito → Iniciar grabación → esperar ~2 minutos a que dé la vuelta → aparece "¡Circuito cerrado!" → Terminar → nombre → Guardar.
3. En el detalle: mapa con los 3 sectores → **Modo vuelta rápida** → Iniciar sesión.
4. Cada vuelta tarda ~1:50. Los sectores se pintan 🟣/🟢/🟡 y la vuelta queda guardada.

**En la calle:**
1. Opciones → GPS simulado **desactivado**.
2. Pararte donde querés la largada → Iniciar grabación → dar la vuelta → al volver al inicio, Terminar.
3. Modo vuelta rápida: arrancar un poco antes de la meta y cruzarla en el mismo sentido en que grabaste.
