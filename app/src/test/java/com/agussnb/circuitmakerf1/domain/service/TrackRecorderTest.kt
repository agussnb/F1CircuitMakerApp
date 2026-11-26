package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.service.TestTracks.gps
import com.agussnb.circuitmakerf1.domain.service.TestTracks.square
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackRecorderTest {

    private fun record(points: List<com.agussnb.circuitmakerf1.domain.model.GpsPoint>): RecordingState =
        points.fold(RecordingState(isRecording = true)) { state, point -> state.withPoint(point) }

    @Test
    fun `descarta lecturas imprecisas`() {
        val state = record(
            listOf(
                gps(0.0, 0.0, 0),
                gps(0.0, 10.0, 1000, accuracy = 35f),
                gps(0.0, 20.0, 2000)
            )
        )
        assertEquals(2, state.points.size)
        assertEquals(1, state.discardedPoints)
        assertEquals(20.0, state.totalDistanceMeters, 0.1)
        assertEquals(5f, state.lastAccuracyMeters)
    }

    @Test
    fun `ignora movimientos muy chicos`() {
        val state = record(
            listOf(
                gps(0.0, 0.0, 0),
                gps(0.0, 1.0, 1000),
                gps(0.0, 2.0, 2000),
                gps(0.0, 10.0, 3000)
            )
        )
        assertEquals(2, state.points.size)
        assertEquals(10.0, state.totalDistanceMeters, 0.1)
    }

    @Test
    fun `se cierra al volver a la largada despues de dar la vuelta`() {
        val points = TestTracks.drive(square(100.0), laps = 1, startBeforeFinishMeters = 400.0)
            .take(80) // 80 lecturas a 5 m/s = una vuelta de 400 m
        val state = record(points)

        assertTrue(state.isLoopClosed)
        val closing = requireNotNull(state.closingPointIndex)
        // Se cierra recién al entrar en el radio de 25 m de la largada (≥ 375 m recorridos).
        assertTrue(state.points.take(closing + 1).size >= 75)
        assertEquals(closing + 1, state.trackPath().size)
    }

    @Test
    fun `no se cierra si nunca te alejaste de la largada`() {
        // Ida y vuelta de 20 m muchas veces: suma más de 200 m pero nunca se aleja 50 m.
        val points = (0 until 40).map { i ->
            gps(0.0, if (i % 2 == 0) 0.0 else 20.0, i * 1000L)
        }
        val state = record(points)
        assertTrue(state.totalDistanceMeters > MIN_LOOP_DISTANCE_METERS)
        assertFalse(state.isLoopClosed)
        assertNull(state.closingPointIndex)
    }

    @Test
    fun `el grabador junta los puntos que llegan del GPS`() {
        val points = listOf(gps(0.0, 0.0, 0), gps(0.0, 10.0, 1000), gps(0.0, 20.0, 2000))
        // Dispatchers.Unconfined ejecuta la corrutina en el acto: el test no necesita esperar.
        val recorder = TrackRecorder(FakeLocationProvider(points), CoroutineScope(Dispatchers.Unconfined))

        recorder.start()
        assertEquals(3, recorder.state.value.points.size)

        recorder.stop()
        assertFalse(recorder.state.value.isRecording)

        recorder.reset()
        assertEquals(RecordingState(), recorder.state.value)
    }
}
