package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.service.TestTracks.at
import org.junit.Assert.assertEquals
import org.junit.Test

class PathSimplifierTest {

    @Test
    fun `una recta con muchos puntos queda con los dos extremos`() {
        val line = (0..10).map { at(0.0, it * 10.0) }
        assertEquals(listOf(line.first(), line.last()), simplifyPath(line))
    }

    @Test
    fun `un desvio menor a la tolerancia se elimina`() {
        val line = listOf(at(0.0, 0.0), at(1.0, 50.0), at(0.0, 100.0))
        assertEquals(2, simplifyPath(line, toleranceMeters = 2.0).size)
    }

    @Test
    fun `una esquina se conserva`() {
        val corner = at(0.0, 100.0)
        val lShape = (0..10).map { at(0.0, it * 10.0) } + (1..10).map { at(it * 10.0, 100.0) }
        val simplified = simplifyPath(lShape)
        assertEquals(listOf(lShape.first(), corner, lShape.last()), simplified)
    }
}
