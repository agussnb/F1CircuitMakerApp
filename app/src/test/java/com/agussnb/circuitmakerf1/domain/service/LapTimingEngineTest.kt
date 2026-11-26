package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.Lap
import com.agussnb.circuitmakerf1.domain.model.TimingColor
import com.agussnb.circuitmakerf1.domain.service.TestTracks.gps
import com.agussnb.circuitmakerf1.domain.service.TestTracks.square
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LapTimingEngineTest {

    private val path = square(100.0) // 400 m
    private val layout = buildTrackLayout(path)!!

    private fun run(engine: LapTimingEngine, points: List<com.agussnb.circuitmakerf1.domain.model.GpsPoint>): List<Lap> =
        points.mapNotNull { engine.onPoint(it) }

    @Test
    fun `espera a cruzar la meta antes de empezar a cronometrar`() {
        val engine = LapTimingEngine(layout)
        assertEquals(TimingPhase.WAITING_FOR_START, engine.snapshot.phase)

        // 20 m antes de la meta a 5 m/s: la cruza a los 4 s.
        run(engine, TestTracks.drive(path, laps = 1).take(8))

        assertEquals(TimingPhase.RUNNING, engine.snapshot.phase)
        assertEquals(4000.0, engine.snapshot.lapStartMillis!!.toDouble(), 20.0)
    }

    @Test
    fun `mide la vuelta y los tres sectores`() {
        val engine = LapTimingEngine(layout)
        val laps = run(engine, TestTracks.drive(path, laps = 1))

        assertEquals(1, laps.size)
        val lap = laps.single()
        // 400 m a 5 m/s = 80 s; cada sector 133,3 m = 26,67 s.
        assertEquals(80_000.0, lap.lapTimeMillis.toDouble(), 100.0)
        lap.sectorTimesMillis.forEach { assertEquals(26_667.0, it.toDouble(), 100.0) }
    }

    @Test
    fun `colores F1 - violeta, amarillo y violeta otra vez`() {
        val engine = LapTimingEngine(layout)
        // Vuelta 1 a 5 m/s, vuelta 2 más lenta (4 m/s), vuelta 3 más rápida (6 m/s).
        val speeds = mapOf(0 to 5.0, 1 to 4.0, 2 to 6.0)
        run(engine, TestTracks.drive(path, laps = 3) { speeds[it] ?: 5.0 })

        val laps = engine.snapshot.completedLaps
        assertEquals(3, laps.size)
        assertEquals(TimingColor.PURPLE, laps[0].lapColor) // la primera siempre es récord
        assertTrue(laps[0].sectors.all { it.color == TimingColor.PURPLE })
        assertEquals(TimingColor.YELLOW, laps[1].lapColor)
        assertTrue(laps[1].sectors.all { it.color == TimingColor.YELLOW })
        assertEquals(TimingColor.PURPLE, laps[2].lapColor)
        assertTrue(laps[2].sectors.all { it.color == TimingColor.PURPLE })
        assertEquals(laps[2].lapTimeMillis, engine.snapshot.bestLapMillis)
    }

    @Test
    fun `verde si mejora la sesion pero no el historico`() {
        val historic = listOf(Lap(1, listOf(20_000L, 20_000L, 20_000L)))
        val engine = LapTimingEngine(layout, historic)
        val speeds = mapOf(0 to 4.0, 1 to 5.0)
        run(engine, TestTracks.drive(path, laps = 2) { speeds[it] ?: 5.0 })

        val laps = engine.snapshot.completedLaps
        assertEquals(TimingColor.GREEN, laps[0].lapColor) // primera de la sesión, peor que el récord
        assertEquals(TimingColor.GREEN, laps[1].lapColor) // mejora la sesión
        assertEquals(60_000L, engine.snapshot.bestLapMillis)
    }

    @Test
    fun `el delta compara contra la mejor vuelta`() {
        val engine = LapTimingEngine(layout)
        val speeds = mapOf(0 to 5.0, 1 to 4.0)
        run(engine, TestTracks.drive(path, laps = 2) { speeds[it] ?: 5.0 })

        val laps = engine.snapshot.completedLaps
        assertEquals(null, laps[0].deltaToBestMillis) // no había contra qué comparar
        // 400 m a 4 m/s = 100 s → +20 s contra los 80 s de la primera. La tolerancia de 0,5 s
        // es porque el recorrido de prueba cambia de velocidad recién en la lectura siguiente a la meta.
        assertEquals(20_000.0, laps[1].deltaToBestMillis!!.toDouble(), 500.0)
    }

    @Test
    fun `cruzar la meta sin pasar por los sectores invalida la vuelta`() {
        val engine = LapTimingEngine(layout)
        run(
            engine,
            listOf(
                gps(0.0, -10.0, 0),
                gps(0.0, 10.0, 1000), // cruza la meta: empieza la vuelta a los 500 ms
                gps(0.0, -10.0, 2000), // vuelve para atrás (en contra no cuenta)
                gps(0.0, 10.0, 3000) // cruza otra vez la meta sin haber hecho el sector 1
            )
        )
        assertTrue(engine.snapshot.completedLaps.isEmpty())
        assertEquals(2500L, engine.snapshot.lapStartMillis)
        assertEquals(0, engine.snapshot.currentSectorIndex)
    }

    @Test
    fun `ignora lecturas imprecisas y saltos del GPS`() {
        val engine = LapTimingEngine(layout)
        run(
            engine,
            listOf(
                gps(0.0, -10.0, 0),
                gps(0.0, 10.0, 1000, accuracy = 50f), // imprecisa: se ignora
                gps(0.0, 300.0, 2000) // salto de 310 m: se ignora
            )
        )
        assertEquals(TimingPhase.WAITING_FOR_START, engine.snapshot.phase)
    }

    @Test
    fun `LapTimer guarda cada vuelta y borra las sesiones vacias`() {
        val scope = CoroutineScope(Dispatchers.Unconfined)

        val repository = FakeSessionRepository()
        val timer = LapTimer(FakeLocationProvider(TestTracks.drive(path, laps = 2)), repository, scope)
        timer.start(trackId = 1, layout = layout)
        assertEquals(2, repository.savedLaps.size)
        timer.stop()
        assertTrue(repository.deletedSessions.isEmpty())

        val emptyRepository = FakeSessionRepository()
        val emptyTimer = LapTimer(EmptyLocationProvider(), emptyRepository, scope)
        emptyTimer.start(trackId = 1, layout = layout)
        emptyTimer.stop()
        assertEquals(listOf(1L), emptyRepository.deletedSessions)
    }
}
