package com.agussnb.circuitmakerf1.domain.service

import junit.framework.TestCase.assertEquals
import org.junit.Test

class FiaLapsTest {

    @Test
    fun `Monza da 53 vueltas`() {
        assertEquals(53, fiaLapCount(5.793))
    }

    @Test
    fun `Spa da 44 vueltas`() {
        assertEquals(44, fiaLapCount(7.004))
    }

    @Test
    fun `longitud cero da cero vueltas`() {
        assertEquals(0, fiaLapCount(0.0))
    }
}