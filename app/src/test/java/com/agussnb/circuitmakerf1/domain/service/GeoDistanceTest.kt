package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoDistanceTest {

    private fun point(lat: Double, lon: Double) = GpsPoint(lat, lon, 0f, 0L)

    @Test
    fun `un grado de latitud mide aproximadamente 111 km`() {
        val distance = distanceMeters(point(0.0, 0.0), point(1.0, 0.0))
        assertEquals(111_195.0, distance, 1.0)
    }

    @Test
    fun `el mismo punto da distancia cero`() {
        val distance = distanceMeters(point(-34.75, -58.39), point(-34.75, -58.39))
        assertEquals(0.0, distance, 0.001)
    }
}