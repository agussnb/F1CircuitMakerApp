package com.agussnb.circuitmakerf1.domain.service

import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import com.agussnb.circuitmakerf1.domain.model.Lap
import com.agussnb.circuitmakerf1.domain.port.LocationProvider
import com.agussnb.circuitmakerf1.domain.port.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf

/** Utilidades compartidas por los tests: circuitos inventados y recorridos simulados. */
object TestTracks {

    val ORIGIN = GeoPoint(-34.6, -58.4)

    /** Punto a [east] metros al este y [north] metros al norte del origen. */
    fun at(east: Double, north: Double): GeoPoint = ORIGIN.offsetMeters(east, north)

    fun gps(east: Double, north: Double, timeMillis: Long, accuracy: Float = 5f): GpsPoint {
        val p = at(east, north)
        return GpsPoint(p.latitude, p.longitude, accuracy, timeMillis)
    }

    /**
     * Cuadrado de [side] metros recorrido en sentido horario: sale del origen hacia el norte,
     * dobla al este, baja al sur y vuelve al oeste. Mide 4 × side.
     */
    fun square(side: Double = 100.0): List<GeoPoint> = listOf(
        at(0.0, 0.0),
        at(0.0, side),
        at(side, side),
        at(side, 0.0)
    )

    /**
     * Simula a alguien dando vueltas: una lectura por segundo, arrancando [startBeforeFinishMeters]
     * antes de la meta. [speedForLap] da la velocidad (m/s) según la vuelta: -1 es la vuelta
     * de salida, 0 la primera vuelta cronometrada, etc.
     */
    fun drive(
        path: List<GeoPoint>,
        laps: Int,
        startBeforeFinishMeters: Double = 20.0,
        speedForLap: (Int) -> Double = { 5.0 }
    ): List<GpsPoint> {
        val walker = PathWalker(path)
        val length = walker.lengthMeters
        var distance = length - startBeforeFinishMeters
        var time = 0L
        val points = mutableListOf<GpsPoint>()
        // Unos metros de más al final, para cruzar bien la última línea de meta.
        val end = length * (laps + 1) + 10.0

        while (distance <= end) {
            val position = walker.positionAt(distance)
            points += GpsPoint(position.latitude, position.longitude, 5f, time)
            val lapIndex = (distance / length).toInt() - 1
            distance += speedForLap(lapIndex)
            time += 1000
        }
        return points
    }
}

class FakeLocationProvider(private val points: List<GpsPoint>) : LocationProvider {
    override fun locationUpdates(): Flow<GpsPoint> = points.asFlow()
}

class EmptyLocationProvider : LocationProvider {
    override fun locationUpdates(): Flow<GpsPoint> = flowOf()
}

class FakeSessionRepository(private val historic: List<Lap> = emptyList()) : SessionRepository {
    val sessions = mutableListOf<Long>()
    val savedLaps = mutableListOf<Pair<Long, Lap>>()
    val deletedSessions = mutableListOf<Long>()

    override suspend fun startSession(trackId: Int, startedAtMillis: Long): Long {
        val id = sessions.size + 1L
        sessions += id
        return id
    }

    override suspend fun saveLap(sessionId: Long, lap: Lap) {
        savedLaps += sessionId to lap
    }

    override suspend fun deleteSession(sessionId: Long) {
        deletedSessions += sessionId
    }

    override suspend fun getLaps(trackId: Int): List<Lap> = historic

    override fun observeLaps(trackId: Int): Flow<List<Lap>> = flowOf(historic)
}
