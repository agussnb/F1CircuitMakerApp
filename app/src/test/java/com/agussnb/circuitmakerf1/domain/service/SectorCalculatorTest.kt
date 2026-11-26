package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.TimingGate
import com.agussnb.circuitmakerf1.domain.service.TestTracks.at
import com.agussnb.circuitmakerf1.domain.service.TestTracks.square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SectorCalculatorTest {

    private val layout = requireNotNull(buildTrackLayout(square(100.0)))

    @Test
    fun `la longitud incluye el tramo que cierra el circuito`() {
        assertEquals(400.0, layout.lengthMeters, 0.5)
    }

    @Test
    fun `hay tres lineas de cronometraje y tres sectores`() {
        assertEquals(3, layout.gates.size)
        assertEquals(3, layout.sectors.size)
    }

    @Test
    fun `la meta esta en el primer punto mirando en el sentido de carrera`() {
        val finish = layout.gates[0]
        assertEquals(0.0, distanceMeters(finish.position, at(0.0, 0.0)), 0.1)
        // La largada está en una esquina (se llega yendo al oeste y se sale hacia el norte):
        // la línea toma la bisectriz, 315° (noroeste), para poder cruzarla viniendo del oeste.
        assertEquals(315.0, finish.bearingDegrees, 0.5)
    }

    @Test
    fun `los sectores dividen el circuito en partes iguales`() {
        // 400 m / 3 = 133,3 m → 33,3 m después de la primera esquina (yendo al este).
        val endOfS1 = layout.gates[1]
        assertEquals(0.0, distanceMeters(endOfS1.position, at(33.33, 100.0)), 0.5)
        assertEquals(90.0, endOfS1.bearingDegrees, 0.1)

        // 266,7 m → 66,7 m después de la segunda esquina (yendo al sur).
        val endOfS2 = layout.gates[2]
        assertEquals(0.0, distanceMeters(endOfS2.position, at(100.0, 33.33)), 0.5)
        assertEquals(180.0, endOfS2.bearingDegrees, 0.1)
    }

    @Test
    fun `cada sector empieza donde termina el anterior`() {
        for (i in 0 until 3) {
            val end = layout.sectors[i].last()
            val nextStart = layout.sectors[(i + 1) % 3].first()
            assertEquals(0.0, distanceMeters(end, nextStart), 0.5)
        }
    }

    @Test
    fun `un trazado demasiado corto no forma un circuito`() {
        assertNull(buildTrackLayout(listOf(at(0.0, 0.0), at(0.0, 5.0))))
        assertNull(buildTrackLayout(listOf(at(0.0, 0.0))))
    }

    @Test
    fun `los extremos de la linea de meta son perpendiculares a la carrera`() {
        val gate = TimingGate(at(0.0, 0.0), bearingDegrees = 0.0, halfWidthMeters = GATE_HALF_WIDTH_METERS)
        val (left, right) = gate.endpoints()
        assertEquals(0.0, distanceMeters(left, at(-GATE_HALF_WIDTH_METERS, 0.0)), 0.1)
        assertEquals(0.0, distanceMeters(right, at(GATE_HALF_WIDTH_METERS, 0.0)), 0.1)
    }
}
