package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.TimingGate
import com.agussnb.circuitmakerf1.domain.service.TestTracks.at
import com.agussnb.circuitmakerf1.domain.service.TestTracks.gps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GateCrossingTest {

    /** Línea en el origen; se corre hacia el norte. */
    private val gate = TimingGate(at(0.0, 0.0), bearingDegrees = 0.0, halfWidthMeters = 25.0)

    @Test
    fun `cruzar hacia adelante interpola el instante exacto`() {
        // 3 m antes y 7 m después: el cruce ocurre al 30 % del tramo.
        val time = crossingTimeMillis(gps(0.0, -3.0, 10_000), gps(0.0, 7.0, 11_000), gate)
        assertEquals(10_300L, time)
    }

    @Test
    fun `cruzar en contra del sentido de carrera no cuenta`() {
        assertNull(crossingTimeMillis(gps(0.0, 5.0, 0), gps(0.0, -5.0, 1000), gate))
    }

    @Test
    fun `no cruzar no cuenta`() {
        assertNull(crossingTimeMillis(gps(0.0, -10.0, 0), gps(0.0, -2.0, 1000), gate))
    }

    @Test
    fun `pasar por fuera del ancho de la linea no cuenta`() {
        assertNull(crossingTimeMillis(gps(40.0, -5.0, 0), gps(40.0, 5.0, 1000), gate))
    }

    @Test
    fun `cruzar en diagonal dentro del ancho cuenta`() {
        val time = crossingTimeMillis(gps(-10.0, -5.0, 0), gps(10.0, 5.0, 1000), gate)
        assertEquals(500L, time)
    }
}
