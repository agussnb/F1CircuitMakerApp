package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.Lap
import com.agussnb.circuitmakerf1.domain.model.TimingColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalBestsTest {

    @Test
    fun `la vuelta ideal suma los mejores sectores de vueltas distintas`() {
        val bests = personalBests(
            listOf(
                Lap(1, listOf(30_000, 31_000, 32_000)), // 93 s
                Lap(2, listOf(31_000, 29_000, 33_000)), // 93 s
                Lap(3, listOf(32_000, 32_000, 30_000)) // 94 s
            )
        )
        assertEquals(93_000L, bests.bestLapMillis)
        assertEquals(listOf(30_000L, 29_000L, 30_000L), bests.bestSectorsMillis)
        assertEquals(89_000L, bests.idealLapMillis)
        assertEquals(3, bests.totalLaps)
    }

    @Test
    fun `sin vueltas no hay records`() {
        val bests = personalBests(emptyList())
        assertNull(bests.bestLapMillis)
        assertNull(bests.idealLapMillis)
    }

    @Test
    fun `clasificacion de colores`() {
        assertEquals(TimingColor.PURPLE, classifyTime(100, sessionBestMillis = null, overallBestMillis = null))
        assertEquals(TimingColor.PURPLE, classifyTime(90, sessionBestMillis = 120, overallBestMillis = 95))
        assertEquals(TimingColor.GREEN, classifyTime(100, sessionBestMillis = 120, overallBestMillis = 95))
        assertEquals(TimingColor.GREEN, classifyTime(100, sessionBestMillis = null, overallBestMillis = 95))
        assertEquals(TimingColor.YELLOW, classifyTime(130, sessionBestMillis = 120, overallBestMillis = 95))
    }
}
