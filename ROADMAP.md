# 🏎️ CircuitMakerF1 — Roadmap

> App Android (Kotlin + Jetpack Compose) para crear circuitos estilo F1 grabando un recorrido con GPS, calcular sus 3 sectores y la cantidad de vueltas según la regla FIA, y hacer vueltas rápidas cronometradas por sector.
> Enfoque inicial: ciclismo. A futuro: atletismo.

---

# 🏁 ESTADO ACTUAL

- [x] Fase 1 — Fundamentos de Compose
- [x] Fase 2 — Estado y recomposición
- [x] Fase 3 — Navegación básica
- [x] Fase 4 — Persistencia con Room
- [ ] GPS: permisos y distancia (funciona a medias, ver Fase 0)

#### Estás en etapa: transición de demo a app real.

---

## 🧹 FASE 0 — Git y limpieza

### Objetivo: tener el proyecto versionado y sin bugs conocidos antes de construir encima.

#### TODO 1 — Git

- [ ] Subir el proyecto a GitHub
- [ ] Revisar que `.gitignore` excluya `build/`, `.gradle/` y `local.properties`
- [ ] Agregar este roadmap en la raíz del repo
- [ ] Definir convención de commits (`feat:`, `fix:`, `refactor:`, `docs:`)

#### TODO 2 — Bugs actuales

- [ ] Arreglar la lógica invertida de permisos en `RecordingCircuitScreen`
- [ ] Exigir `ACCESS_FINE_LOCATION` (la ubicación aproximada no sirve para circuitos)
- [ ] Corregir la fórmula de vueltas: `floor(305 / km) + 1`
- [ ] Sacar la inserción de Monza en `MainActivity` (se duplica en cada arranque)
- [ ] Sacar el botón que agrega Spa hardcodeado
- [ ] Navegar al detalle por `id` en vez de por `name`
- [ ] Validar que el nombre no esté vacío antes de guardar

#### TODO 3 — Identidad

- [ ] Cambiar el package `com.example` por uno propio (Play Store rechaza `com.example`)

👉 Cada ítem es un commit chico. Buen momento para agarrarle la mano al flujo de Git.

---

## 🏗️ FASE 5 — Arquitectura (hexagonal liviana)

### Objetivo: dejar la base lista para la lógica compleja que viene.

#### TODO 4 — Estructura

- [ ] Crear paquetes `domain`, `data`, `ui`, `di`
- [ ] Crear `CircuitMakerApp : Application` y un `AppContainer` (inyección manual)
- [ ] Mover la creación de la base de datos al `AppContainer` (una sola instancia)

#### TODO 5 — Dominio, puertos y adaptadores

- [ ] Modelo de dominio `Circuit` en Kotlin puro (sin anotaciones de Room)
- [ ] `CircuitEntity` en `data` + mappers `toDomain()` / `toEntity()`
- [ ] Interfaz `CircuitRepository` en `domain` (puerto)
- [ ] `RoomCircuitRepository` en `data` (adaptador)
- [ ] Interfaz `LocationProvider` que exponga un `Flow<GpsPoint>`
- [ ] `FusedLocationProvider` como implementación real (reemplaza a `GPSManager`)

#### TODO 6 — ViewModels

- [ ] `HomeViewModel`, `DetailViewModel`, `RecordingViewModel`
- [ ] Exponer el estado de cada pantalla con `StateFlow<UiState>`
- [ ] Mover el formateo de `Circuit.toString()` a la UI
- [ ] Sacar `rememberCoroutineScope` de `AppNavigation` (el borrado va al ViewModel)

#### TODO 7 — Tests

- [ ] Primer test unitario de dominio (por ejemplo, la calculadora de vueltas FIA)

👉 Ahora la UI solo dibuja y la lógica se puede testear sin celular.

---

## 📍 FASE 6 — GPS confiable

### Objetivo: grabar un recorrido real, con la pantalla apagada, sin perder datos.

#### TODO 8 — Datos en pantalla

- [ ] Mostrar latitud, longitud y precisión en vivo
- [ ] Descartar puntos con `accuracy` mayor a 20 m
- [ ] Ignorar movimientos menores a 2–3 m (evita sumar distancia estando quieto)

#### TODO 9 — Foreground Service

- [ ] Crear `RecordingService` con notificación persistente
- [ ] Declarar `foregroundServiceType="location"` en el Manifest (Android 14+)
- [ ] Pedir permiso `POST_NOTIFICATIONS` (Android 13+)
- [ ] Verificar que la grabación sobrevive a rotar, minimizar y apagar la pantalla

#### TODO 10 — Persistir la trayectoria

- [ ] Entidad `TrackPointEntity(circuitId, order, lat, lng, timestamp)`
- [ ] Guardar los puntos junto con el circuito en una transacción
- [ ] Agregar campo `sportType` a `Circuit` (ciclismo por defecto)
- [ ] Migración de Room a versión 2 (o `fallbackToDestructiveMigration` mientras desarrollás)

#### TODO 11 — GPS simulado

- [ ] `FakeLocationProvider` que reproduzca una ruta predefinida
- [ ] Poder "pedalear" desde el escritorio para probar

👉 Te ahorra salir a dar vueltas en bici cada vez que querés probar algo.

---

## 🗺️ FASE 7 — Crear circuito

### Objetivo: el flujo completo Grabar → Cerrar → Circuito armado.

#### TODO 12 — Menú principal

- [ ] Pantalla menú: Mis circuitos / Crear circuito / Opciones
- [ ] Pantalla Opciones (vacía por ahora)

#### TODO 13 — Mapa

- [ ] Integrar Google Maps Compose (API key en `local.properties`, fuera de Git)
- [ ] Dibujar el trazado en vivo mientras grabás
- [ ] Mostrar el circuito en la pantalla de detalle

#### TODO 14 — Cierre y limpieza del trazado

- [ ] Detectar el cierre: volver a menos de 15–20 m del inicio tras recorrer una distancia mínima
- [ ] Avisar "Circuito cerrado" y ofrecer terminar la grabación
- [ ] Simplificar la línea con Douglas-Peucker

#### TODO 15 — Cálculos

- [ ] `SectorCalculator`: dividir el circuito en 3 sectores por distancia
- [ ] Guardar la línea de meta y los límites de sector (punto + dirección)
- [ ] `FiaLapCalculator`: `floor(305 / km) + 1`
- [ ] Tests unitarios de ambos

👉 Acá tu app ya hace la primera mitad de lo que imaginaste.

---

## ⏱️ FASE 8 — Modo vuelta rápida

### Objetivo: el corazón de la app.

#### TODO 16 — Modelos

- [ ] `Session`, `Lap`, `SectorTime` (dominio + entidades Room)

#### TODO 17 — Detección de cruces

- [ ] Detectar el cruce de meta con intersección de segmentos
- [ ] Detectar el cruce de cada límite de sector
- [ ] Interpolar el tiempo exacto del cruce entre dos lecturas GPS
- [ ] Tests con `FakeLocationProvider`

#### TODO 18 — Cronómetro

- [ ] Pantalla de sesión: tiempo de vuelta en vivo y sector actual
- [ ] Mostrar el tiempo de cada sector al cruzarlo
- [ ] Colores: 🟣 mejor histórico, 🟢 mejor de la sesión, 🟡 más lento
- [ ] Lista de vueltas de la sesión
- [ ] Delta contra tu mejor vuelta

👉 Ahora sí: F1 en bici.

---

## 🎨 FASE 9 — UX real

- [ ] LazyColumn en vez de Column
- [ ] Cards para cada circuito
- [ ] TopAppBar
- [ ] FloatingActionButton para crear circuito
- [ ] Estados vacíos ("Todavía no creaste circuitos")
- [ ] Loading states
- [ ] Diálogo de confirmación antes de borrar
- [ ] Tema visual estilo F1 (oscuro, tipografía, colores de sectores)

---

## 📊 FASE 10 — Estadísticas y opciones

- [ ] Historial de sesiones por circuito
- [ ] Mejor vuelta y mejores sectores
- [ ] Vuelta ideal (suma de tus mejores sectores)
- [ ] Opción: distancia objetivo para calcular vueltas (además de los 305 km FIA)
- [ ] Opción: deporte (ciclismo / atletismo)
- [ ] Exportar circuito en GPX

---

## 🏁 FASE FINAL — App funcional

Tu app debería poder:

- Crear un circuito grabándolo con GPS
- Mostrarlo en un mapa con sus 3 sectores
- Calcular las vueltas estilo FIA
- Cronometrar vueltas y sectores con colores F1
- Guardar sesiones y mostrar estadísticas
- Persistir todo localmente

---

## 🔮 FUTURO

- [ ] Soporte para atletismo
- [ ] Mover sectores a mano
- [ ] Compartir circuitos
- [ ] Hilt para inyección de dependencias
- [ ] GitHub Actions: build y tests en cada push
- [ ] Publicar en Play Store
