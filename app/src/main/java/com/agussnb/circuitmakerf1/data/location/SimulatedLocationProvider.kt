package com.agussnb.circuitmakerf1.data.location

import android.os.SystemClock
import com.agussnb.circuitmakerf1.domain.model.GeoPoint
import com.agussnb.circuitmakerf1.domain.model.GpsPoint
import com.agussnb.circuitmakerf1.domain.port.LocationProvider
import com.agussnb.circuitmakerf1.domain.port.RouteSimulator
import com.agussnb.circuitmakerf1.domain.service.PathWalker
import com.agussnb.circuitmakerf1.domain.service.SECTOR_COUNT
import com.agussnb.circuitmakerf1.domain.service.offsetMeters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * GPS falso que "pedalea" solo: emite una posición por segundo recorriendo un circuito
 * a velocidad de bici. En cada sector cambia un poco la velocidad, así los tiempos varían
 * y se pueden ver los colores violeta / verde / amarillo sin salir de casa.
 */
class SimulatedLocationProvider : LocationProvider, RouteSimulator {

    @Volatile
    private var route: List<GeoPoint>? = null

    override fun useRoute(route: List<GeoPoint>?) {
        this.route = route?.takeIf { it.size >= 2 }
    }

    override fun locationUpdates(): Flow<GpsPoint> = flow {
        val circuit = route
        val walker = PathWalker(circuit ?: demoRoute())

        // Con un circuito guardado arrancamos antes de la meta, para cruzarla enseguida.
        var distance = if (circuit != null) walker.lengthMeters - START_BEFORE_FINISH_METERS else 0.0
        var currentSector = -1
        var speed = BASE_SPEED_METERS_PER_SECOND

        while (true) {
            val position = walker.positionAt(distance)
            emit(
                GpsPoint(
                    latitude = position.latitude,
                    longitude = position.longitude,
                    accuracyMeters = SIMULATED_ACCURACY_METERS,
                    timestampMillis = SystemClock.elapsedRealtime()
                )
            )
            delay(UPDATE_INTERVAL_MILLIS)

            val lapFraction = (distance % walker.lengthMeters) / walker.lengthMeters
            val sector = (lapFraction * SECTOR_COUNT).toInt().coerceIn(0, SECTOR_COUNT - 1)
            if (sector != currentSector) {
                currentSector = sector
                speed = BASE_SPEED_METERS_PER_SECOND * Random.nextDouble(0.85, 1.15)
            }
            distance += speed * UPDATE_INTERVAL_MILLIS / 1000.0
        }
    }

    private companion object {
        const val UPDATE_INTERVAL_MILLIS = 1000L
        const val BASE_SPEED_METERS_PER_SECOND = 12.0 // ≈ 43 km/h
        const val SIMULATED_ACCURACY_METERS = 4f
        const val START_BEFORE_FINISH_METERS = 60.0

        /** Centro del recorrido de demo: Parque Tres de Febrero (Palermo, CABA). */
        val DEMO_CENTER = GeoPoint(-34.5715, -58.4170)

        /**
         * Un circuito de ~1,3 km con curvas de distinto radio. Es una figura matemática
         * (no sigue calles), pero alcanza para probar la app.
         */
        fun demoRoute(): List<GeoPoint> {
            val steps = 90
            return (0 until steps).map { i ->
                val angle = 2 * PI * i / steps
                val radius = 170.0 * (1 + 0.28 * cos(2 * angle) + 0.12 * sin(3 * angle))
                DEMO_CENTER.offsetMeters(
                    eastMeters = 1.25 * radius * cos(angle),
                    northMeters = radius * sin(angle)
                )
            }
        }
    }
}
